package com.github.hinaser.gfma.editor;

import com.github.hinaser.gfma.browser.IBrowser;
import com.github.hinaser.gfma.browser.MarkdownParsedAdapter;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class JCefGfmAPreview extends AbstractGfmAPreview {
    private final IBrowser browser = new ChromiumBrowser();
    private MarkdownParsedAdapter markdownParsedAdapter;

    public JCefGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document){
        super(markdownFile, document);
        this.markdownParsedAdapter = new MarkdownParsedAdapter(this.browser, markdownFile.getName());
        initialize();
    }

    @Override
    @NotNull
    protected MarkdownParsedListener getMarkdownParsedListener() {
        markdownParsedAdapter.setFilename(markdownFile.getName());
        return markdownParsedAdapter;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return browser.getComponent();
    }

    @Override
    public void dispose(){

    }

    @Override
    public @NotNull String getName() {
        return "GfmA Preview";
    }
}
