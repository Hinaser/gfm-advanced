package com.github.hinaser.gfma.browser;

public interface MarkdownParsedListener {
    void onMarkdownParseDone(String html);

    void onMarkdownParseFailed(String errorMessage, String stackTrace);
}
