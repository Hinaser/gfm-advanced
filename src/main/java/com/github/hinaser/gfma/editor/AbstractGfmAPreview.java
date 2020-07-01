package com.github.hinaser.gfma.editor;

import com.github.hinaser.gfma.settings.ApplicationSettingsChangedListener;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.github.hinaser.gfma.browser.IBrowser;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.github.hinaser.gfma.markdown.MarkdownParsedListener;
import com.github.hinaser.gfma.markdown.MarkdownParser;
import com.github.hinaser.gfma.markdown.ThrottlePoolExecutor;
import com.github.hinaser.gfma.template.MarkdownTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public abstract class AbstractGfmAPreview extends UserDataHolderBase implements Disposable, FileEditor {
    protected final VirtualFile markdownFile;
    protected final ApplicationSettingsService appSettingsService;

    protected Document document;
    protected IBrowser browser;
    protected MarkdownParser markdownParser;
    protected ThrottlePoolExecutor throttlePoolExecutor = new ThrottlePoolExecutor(200);
    protected boolean isModifiedAndNotRendered = true;

    public AbstractGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document) {
        this.markdownFile = markdownFile;
        this.document = document;
        this.appSettingsService = ApplicationSettingsService.getInstance();

        String parentFolderPath = markdownFile.getParent().getCanonicalPath();
        this.markdownParser = MarkdownParser.createMarkdownParser(parentFolderPath, getMarkdownParsedListener());

        this.document.addDocumentListener(new DocumentChangeListener());

        this.appSettingsService.addApplicationSettingsChangedListener(new SettingsChangeListener(), this);
    }

    public void updatePreview() {
        queueMarkdownToHtmlTask(document.getText());
        isModifiedAndNotRendered = false;
    }

    public void queueMarkdownToHtmlTask(String markdown) {
        throttlePoolExecutor.queue(getMarkdownWorker(markdownFile.getName(), markdown));
    }

    public void queueMarkdownToHtmlTask(String markdown, long timeout) {
        throttlePoolExecutor.queue(getMarkdownWorker(markdownFile.getName(), markdown), timeout);
    }

    protected class DocumentChangeListener implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            isModifiedAndNotRendered = true;
        }
    }

    protected MarkdownWorker getMarkdownWorker(String filename, String markdown) {
        return new MarkdownWorker(filename, markdown);
    }

    protected class MarkdownWorker implements Runnable {
        protected String filename;
        protected String markdown;
        protected MarkdownParsedListener listener;

        public MarkdownWorker(String filename, String markdown) {
            this.filename = filename;
            this.markdown = markdown;
            this.listener = getMarkdownParsedListener();
        }

        @Override
        public void run() {
            var html = markdownParser.markdownToHtml(markdown);
            var template = MarkdownTemplate.getInstance();
            var appliedHtml = template.getGithubFlavoredHtml(filename, html);
            listener.onMarkdownParseDone(appliedHtml);
        }
    }

    protected class SettingsChangeListener implements ApplicationSettingsChangedListener {
        @Override
        public void onApplicationSettingsChanged(ApplicationSettingsService newApplicationSettings) {
            updatePreview();
        }
    }

    /**
     * Inherited class is required to implement this method.
     * Returned MarkdownParsedListener is called every time when queued markdown to html task is completed.
     * Generally, on markdown parsing done, parsed html string should be passed into browser to load for rendering for preview.
     *
     * @return MarkdownParsedListener instance.
     */
    @NotNull
    abstract protected MarkdownParsedListener getMarkdownParsedListener();

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
        return FileEditorState.INSTANCE;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return browser.getComponent();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return getComponent();
    }

    @Override
    public void selectNotify() {
        if(isModifiedAndNotRendered) {
            updatePreview();
        }
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }
}
