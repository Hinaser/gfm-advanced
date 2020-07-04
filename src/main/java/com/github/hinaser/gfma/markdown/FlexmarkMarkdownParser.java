package com.github.hinaser.gfma.markdown;

import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexmarkMarkdownParser extends AbstractMarkdownParser {
    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)");
    protected String parentFolderPath; // Path to the folder which has the parsing markdown file.

    protected FlexmarkMarkdownParser(String parentFolderPath, MarkdownParsedListener listener) {
        super(listener);
        this.parentFolderPath = parentFolderPath;
        this.markdownParsedListener = listener;
    }

    public static FlexmarkMarkdownParser getInstance(String parentFolderPath, MarkdownParsedListener listener){
        return new FlexmarkMarkdownParser(parentFolderPath, listener);
    }

    /**
     * This method is almost imported from the patch to `gfm` created by bribin.zheng on 2017-01-24.
     * https://github.com/ShyykoSerhiy/gfm-plugin/commit/653d40316b3140768ebeac9490438e2f8ae52c0f#diff-70ccd81c99dd589ba04ea5380650522a
     */
    private String adjustLocalImagePathRelativeToMarkdownPath(String markdown) {
        Matcher matcher = IMG_PATTERN.matcher(markdown);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String newImg = "![" + matcher.group(1) + "](" + appendParentPath(parentFolderPath, matcher.group(2)) + ")";
            matcher.appendReplacement(buf, newImg);
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    private String appendParentPath(String parentFolderPath, String src) {
        if (src.indexOf("://") > 0) {
            return src;
        }

        src = "file://" + parentFolderPath + "/" + src;
        src = src.replaceAll("\\\\", "/");
        return src;
    }

    public String markdownToHtml(String markdown) {
        MutableDataHolder options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.GITHUB);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String adjustedMarkdown = adjustLocalImagePathRelativeToMarkdownPath(markdown);
        Node document = parser.parse(adjustedMarkdown);
        return renderer.render(document);
    }

    @Override
    public MarkdownProcessor getMarkdownProcessor(String markdown) {
        return new MarkdownProcessor(markdown);
    }

    private class MarkdownProcessor implements Runnable {
        private final String markdown;

        public MarkdownProcessor(String markdown) {
            this.markdown = markdown;
        }

        @Override
        public void run() {
            try {
                String html = markdownToHtml(markdown);
                markdownParsedListener.onMarkdownParseDone(html);
            }
            catch(Exception e) {
                markdownParsedListener.onMarkdownParseFailed(e.getLocalizedMessage(), ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
