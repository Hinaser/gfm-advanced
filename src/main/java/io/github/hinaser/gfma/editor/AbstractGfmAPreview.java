package io.github.hinaser.gfma.editor;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import io.github.hinaser.gfma.browser.IBrowser;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.hinaser.gfma.markdown.MarkdownParsedListener;
import io.github.hinaser.gfma.markdown.MarkdownParser;
import io.github.hinaser.gfma.markdown.ThrottlePoolExecutor;
import io.github.hinaser.gfma.template.MarkdownTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public abstract class AbstractGfmAPreview extends UserDataHolderBase implements Disposable, FileEditor {
    protected Document document;
    protected final VirtualFile markdownFile;
    protected IBrowser browser;
    protected MarkdownParser markdownParser;
    protected ThrottlePoolExecutor throttlePoolExecutor = new ThrottlePoolExecutor(200);

    public AbstractGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document) {
        this.markdownFile = markdownFile;
        this.document = document;
        this.markdownParser = MarkdownParser.createMarkdownParser(getMarkdownParsedListener());

        // this.document.addDocumentListener(new DocumentChangeListener());
    }

    public void updatePreview() {
        queueMarkdownToHtmlTask(document.getText());
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
            new Thread(new MarkdownToHtmlTask(event)).start();
        }

        public class MarkdownToHtmlTask implements Runnable {
            private final String markdown;

            public MarkdownToHtmlTask(DocumentEvent event){
                markdown = event.getDocument().getText();
            }

            @Override
            public void run() {
                queueMarkdownToHtmlTask(markdown, 500);
            }
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
        updatePreview();
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
