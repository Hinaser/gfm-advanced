package com.github.hinaser.gfma.markdown;

public abstract class AbstractMarkdownParser {
    protected MarkdownParsedListener markdownParsedListener = null;

    protected AbstractMarkdownParser(MarkdownParsedListener listener) {
        markdownParsedListener = listener;
    }

    public abstract Runnable getMarkdownProcessor(String filename, String markdown);
}
