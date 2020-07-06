package com.github.hinaser.gfma.editor;

import com.github.hinaser.gfma.browser.MarkdownParsedAdapter;
import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import com.github.hinaser.gfma.listener.EditorTabListenerManager;
import com.github.hinaser.gfma.markdown.*;
import com.github.hinaser.gfma.settings.ApplicationSettingsChangedListener;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.github.hinaser.gfma.toolWindow.GfmAToolWindowFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
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
    protected ThrottlePoolExecutor rateLimiter = new ThrottlePoolExecutor(200);
    protected boolean isModifiedAndNotRendered = true;
    protected String listenerId;

    protected AbstractGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document) {
        this.markdownFile = markdownFile;
        this.document = document;
        this.appSettings = ApplicationSettingsService.getInstance();
    }

    // This must be invoked in inherited class constructor
    public void initialize() {
        changeMarkdownParser(this.appSettings);
        this.document.addDocumentListener(new DocumentChangeListener());
        this.appSettings.addApplicationSettingsChangedListener(new SettingsChangeListener(), this);
        this.listenerId = EditorTabListenerManager.addListener(new TabSelectedListener());

        String markdown = document.getText();
        updatePreview(markdown);

        if(markdownFile.equals(EditorTabListenerManager.getSelectedFile())){
            updateToolWindow(markdown);
        }
    }

    @Override
    public void dispose() {
        EditorTabListenerManager.removeListener(listenerId);
    }

    public static AbstractMarkdownParser getMarkdownParser(VirtualFile markdownFile, ApplicationSettingsService settings, MarkdownParsedListener l) {
        String parentFolderPath = markdownFile.getParent().getCanonicalPath();
        if(settings.isUseGithubMarkdownAPI()){
            return GithubAPIMarkdownParser.getInstance(parentFolderPath, l);
        }
        else{
            return FlexmarkMarkdownParser.getInstance(parentFolderPath, l);
        }
    }

    public void changeMarkdownParser(ApplicationSettingsService settings) {
        this.markdownParser = getMarkdownParser(markdownFile, settings, getMarkdownParsedListener());
    }

    public void updatePreview(String markdown) {
        queueMarkdownToHtmlTask(markdown);
        isModifiedAndNotRendered = false;
    }

    public void queueMarkdownToHtmlTask(String markdown) {
        rateLimiter.queue(markdownParser.getMarkdownProcessor(markdown));
    }

    public void queueMarkdownToHtmlTask(String markdown, long timeout) {
        rateLimiter.queue(markdownParser.getMarkdownProcessor(markdown), timeout);
    }

    protected ThrottlePoolExecutor rateLimiterForToolWindow = new ThrottlePoolExecutor(400);

    protected void updateToolWindow(String markdown) {
        rateLimiterForToolWindow.queue(new Runnable() {
            @Override
            public void run() {
                MarkdownParsedAdapter l = GfmAToolWindowFactory.getToolWindow().getMarkdownParsedListener();
                AbstractMarkdownParser p = getMarkdownParser(markdownFile, appSettings, l);
                p.getMarkdownProcessor(markdown).run();
            }
        });
    }

    protected class TabSelectedListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            if(markdownFile.equals(EditorTabListenerManager.getSelectedFile())){
                updateToolWindow(document.getText());
            }
        }
    }

    protected class DocumentChangeListener implements DocumentListener {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            isModifiedAndNotRendered = true;
            updateToolWindow(event.getDocument().getText());
        }
    }

    protected class SettingsChangeListener implements ApplicationSettingsChangedListener {
        @Override
        public void onApplicationSettingsChanged(ApplicationSettingsService newApplicationSettings) {
            changeMarkdownParser(newApplicationSettings);
            updatePreview(document.getText());
            updateToolWindow(document.getText());
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
            updatePreview(document.getText());
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
