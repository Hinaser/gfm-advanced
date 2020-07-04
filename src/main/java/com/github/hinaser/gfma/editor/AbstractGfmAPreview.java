package com.github.hinaser.gfma.editor;

import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import com.github.hinaser.gfma.markdown.*;
import com.github.hinaser.gfma.settings.ApplicationSettingsChangedListener;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public abstract class AbstractGfmAPreview extends UserDataHolderBase implements Disposable, FileEditor {
    protected VirtualFile markdownFile;
    protected ApplicationSettingsService appSettings;

    protected Document document;
    protected AbstractMarkdownParser markdownParser;
    protected ThrottlePoolExecutor throttlePoolExecutor = new ThrottlePoolExecutor(200);
    protected boolean isModifiedAndNotRendered = true;

    protected AbstractGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document) {
        this.markdownFile = markdownFile;
        this.document = document;
        this.appSettings = ApplicationSettingsService.getInstance();
    }

    // This must be invoked in inherited class constructor
    public void initialize() {
        updateMarkdownParser(this.appSettings);
        this.document.addDocumentListener(new DocumentChangeListener());
        this.appSettings.addApplicationSettingsChangedListener(new SettingsChangeListener(), this);
    }

    public void updateMarkdownParser(ApplicationSettingsService settings) {
        String parentFolderPath = markdownFile.getParent().getCanonicalPath();
        if(settings.isUseGithubMarkdownAPI()){
            this.markdownParser = GithubAPIMarkdownParser.getInstance(parentFolderPath, getMarkdownParsedListener());
        }
        else{
            this.markdownParser = FlexmarkMarkdownParser.getInstance(parentFolderPath, getMarkdownParsedListener());
        }
    }

    public void updatePreview() {
        queueMarkdownToHtmlTask(document.getText());
        isModifiedAndNotRendered = false;
    }

    public void queueMarkdownToHtmlTask(String markdown) {
        throttlePoolExecutor.queue(markdownParser.getMarkdownProcessor(markdown));
    }

    public void queueMarkdownToHtmlTask(String markdown, long timeout) {
        throttlePoolExecutor.queue(markdownParser.getMarkdownProcessor(markdown), timeout);
    }

    protected class DocumentChangeListener implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            isModifiedAndNotRendered = true;
        }
    }

    protected class SettingsChangeListener implements ApplicationSettingsChangedListener {
        @Override
        public void onApplicationSettingsChanged(ApplicationSettingsService newApplicationSettings) {
            updateMarkdownParser(newApplicationSettings);
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
        return true;
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
