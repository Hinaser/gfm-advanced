package io.github.hinaser.gfma.markdown;

public interface MarkdownParsedListener {
    void onMarkdownParseDone(String html);

    void onMarkdownParseFailed(String error);
}
