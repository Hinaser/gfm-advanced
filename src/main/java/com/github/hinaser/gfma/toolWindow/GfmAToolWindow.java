package com.github.hinaser.gfma.toolWindow;

import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.browser.IBrowser;
import com.github.hinaser.gfma.browser.MarkdownParsedAdapter;
import com.github.hinaser.gfma.helper.Util;
import com.github.hinaser.gfma.listener.EditorTabListenerManager;
import com.github.hinaser.gfma.markdown.*;
import com.github.hinaser.gfma.settings.ApplicationSettingsChangedListener;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class GfmAToolWindow extends JPanel implements Disposable {
    private static GfmAToolWindow instance = null;

    private final ApplicationSettingsService appSettings = ApplicationSettingsService.getInstance();
    private IBrowser browser;
    private final ThrottlePoolExecutor rateLimiterForToolWindow = new ThrottlePoolExecutor(1000);
    private MarkdownParsedAdapter markdownParsedAdapter;
    private String listenerId;

    public static GfmAToolWindow getInstance(){
        if(instance == null){
            instance = new GfmAToolWindow();
        }
        return instance;
    }

    private GfmAToolWindow() {
        this.appSettings.addApplicationSettingsChangedListener(new SettingsChangeListener(), this);

        JPanel container = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);

        /*
         * I found that on IDE's startup, `new ChromiumBrowser()` possibly blocks thread somehow.
         * In order to avoid that, here trying to defer browser instance initialization.
         */
        new Thread(() -> {
            this.browser = new ChromiumBrowser();
            this.markdownParsedAdapter = new MarkdownParsedAdapter(this.browser, "");
            this.listenerId = EditorTabListenerManager.addListener(new TabSelectedListener());
            container.add(browser.getComponent(), BorderLayout.CENTER);
        }).start();
    }

    public void render(String markdown) {
        rateLimiterForToolWindow.queue(new Runnable() {
            @Override
            public void run() {
                var markdownFile = EditorTabListenerManager.getSelectedFile();
                if(!Util.isMarkdownFile(markdownFile)){
                    return;
                }

                MarkdownParsedAdapter onParsedListener = getMarkdownParsedListener();
                onParsedListener.setFilename(markdownFile.getName());
                AbstractMarkdownParser parser = Util.getMarkdownParser(markdownFile, appSettings, onParsedListener);
                parser.getMarkdownProcessor(markdown).run();
            }
        });
    }

    @Override
    public void dispose() {
        this.browser.dispose();
        EditorTabListenerManager.removeListener(this.listenerId);
    }

    public MarkdownParsedAdapter getMarkdownParsedListener() {
        return markdownParsedAdapter;
    }

    private class SettingsChangeListener implements ApplicationSettingsChangedListener {
        @Override
        public void onApplicationSettingsChanged(ApplicationSettingsService newApplicationSettings) {
            var document = tryToGetMarkdownDocument();
            if(document == null){
                return;
            }
            render(document.getText());
        }
    }

    private class TabSelectedListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            var virtualFile = event.getNewFile();
            this.updateToolWindow(virtualFile);
        }

        private void updateToolWindow(VirtualFile virtualFile){
            if(!Util.isMarkdownFile(virtualFile)){
                return;
            }

            var document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if(document == null){
                return;
            }

            GfmAToolWindow.this.render(document.getText());
        }
    }

    @Nullable
    private Document tryToGetMarkdownDocument(){
        var virtualFile = EditorTabListenerManager.getSelectedFile();
        if(!Util.isMarkdownFile(virtualFile)){
            return null;
        }
        return FileDocumentManager.getInstance().getDocument(virtualFile);
    }
}
