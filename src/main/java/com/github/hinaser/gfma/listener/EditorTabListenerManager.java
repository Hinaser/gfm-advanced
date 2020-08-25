package com.github.hinaser.gfma.listener;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class EditorTabListenerManager implements FileEditorManagerListener {
    private static VirtualFile selectedFile = null;
    private final static HashMap<String, FileEditorManagerListener> listeners = new HashMap<String, FileEditorManagerListener>();

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        EditorTabListenerManager.selectedFile = event.getNewFile();

        for(FileEditorManagerListener l : listeners.values()) {
            l.selectionChanged(event);
        }
    }

    @Nullable
    public static VirtualFile getSelectedFile() {
        return EditorTabListenerManager.selectedFile;
    }

    public static String addListener(FileEditorManagerListener l) {
        String uuid = UUID.randomUUID().toString();
        listeners.put(uuid, l);
        return uuid;
    }

    public static void removeListener(String id) {
        listeners.remove(id);
    }
}
