package io.github.hinaser.gfma.editor;

import com.intellij.ui.jcef.JBCefApp;
import io.github.hinaser.gfma.file.MarkdownFile;
import io.github.hinaser.gfma.GfmABundle;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class GfmAProvider implements FileEditorProvider {
    private static final String EDITOR_TYPE_ID = GfmABundle.message("gfm.editor.type");
    private static final boolean isJCEFSupported = JBCefApp.isSupported();

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if(!isJCEFSupported){
            return false;
        }
        String extension = virtualFile.getExtension();
        return MarkdownFile.isMarkdown(extension);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        return new JCefGfmAPreview(virtualFile, document);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {
        fileEditor.dispose();
    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {
        //nothing to do here. Preview is stateless.
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
