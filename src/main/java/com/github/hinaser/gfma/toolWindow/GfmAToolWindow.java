package com.github.hinaser.gfma.toolWindow;

import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.browser.IBrowser;
import com.intellij.openapi.Disposable;

import javax.swing.*;

public class GfmAToolWindow extends JPanel implements Disposable {
    private final IBrowser browser;

    public GfmAToolWindow() {
        browser = new ChromiumBrowser();
    }

    @Override
    public void dispose() {
        browser.dispose();
    }
}
