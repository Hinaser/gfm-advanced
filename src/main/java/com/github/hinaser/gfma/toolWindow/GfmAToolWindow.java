package com.github.hinaser.gfma.toolWindow;

import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.browser.IBrowser;
import com.github.hinaser.gfma.browser.MarkdownParsedAdapter;
import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;

public class GfmAToolWindow extends JPanel implements Disposable {
    private final MarkdownParsedAdapter markdownParsedAdapter;
    private final JPanel container;
    private final IBrowser browser;

    public GfmAToolWindow() {
        browser = new ChromiumBrowser();
        container = new JPanel(new BorderLayout());
        this.markdownParsedAdapter = new MarkdownParsedAdapter(this.browser, "");

        container.add(browser.getComponent(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        browser.dispose();
    }

    public MarkdownParsedAdapter getMarkdownParsedListener() {
        return markdownParsedAdapter;
    }
}
