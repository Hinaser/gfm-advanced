package io.github.hinaser.gfma.markdown;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {
    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)");
    protected String parentFolderPath; // Path to the folder which has the parsing markdown file.
    protected MarkdownParsedListener markdownParsedListener = null;

    private MarkdownParser(String parentFolderPath, MarkdownParsedListener listener) {
        this.parentFolderPath = parentFolderPath;
        markdownParsedListener = listener;
    }

    public static MarkdownParser createMarkdownParser(String parentFolderPath, MarkdownParsedListener listener){
        return new MarkdownParser(parentFolderPath, listener);
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

    public String adjustLocalImagePathRelativeToMarkdownPath(String markdown) {
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
}
