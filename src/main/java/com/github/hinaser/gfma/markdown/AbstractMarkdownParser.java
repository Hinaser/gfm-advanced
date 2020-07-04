package com.github.hinaser.gfma.markdown;

import com.github.hinaser.gfma.browser.MarkdownParsedListener;

public abstract class AbstractMarkdownParser {
    protected MarkdownParsedListener markdownParsedListener = null;

    protected AbstractMarkdownParser(MarkdownParsedListener listener) {
        markdownParsedListener = listener;
    }

    public abstract Runnable getMarkdownProcessor(String markdown);
}
