package com.github.hinaser.gfma.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.markdown.MarkdownParsedListener;
import org.jetbrains.annotations.NotNull;


public class JCefGfmAPreview extends AbstractGfmAPreview {
    public JCefGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document){
        super(markdownFile, document);
        this.browser = new ChromiumBrowser();
    }

    @Override
    @NotNull
    protected MarkdownParsedListener getMarkdownParsedListener() {
        return new MarkdownParsedAdapter();
    }

    @Override
    public void dispose(){

    }

    @Override
    public @NotNull String getName() {
        return "GfmA Preview";
    }

    private class MarkdownParsedAdapter implements MarkdownParsedListener {
        @Override
        public void onMarkdownParseDone(String html) {
            browser.loadContent(html);
        }

        @Override
        public void onMarkdownParseFailed(String error) {

        }
    }
}
