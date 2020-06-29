package io.github.hinaser.gfma.markdown;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownParser {
    protected MarkdownParsedListener markdownParsedListener = null;

    private MarkdownParser(MarkdownParsedListener listener) {
        markdownParsedListener = listener;
    }

    public static MarkdownParser createMarkdownParser(MarkdownParsedListener listener){
        return new MarkdownParser(listener);
    }

    public String markdownToHtml(String markdown) {
        MutableDataHolder options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.GITHUB);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
