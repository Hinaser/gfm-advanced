package com.github.hinaser.gfma.settings;

import com.intellij.openapi.Disposable;

import javax.swing.*;

public class ApplicationSettingsComponent implements Disposable {
    private JPanel panel;
    private JCheckBox useGitHubMarkdownAPICheckBox;
    private JPasswordField githubAccessTokenField;
    private JSpinner connectionTimeoutSpinner;
    private JSpinner socketTimeoutSpinner;
    private JCheckBox useFullWidthRenderingCheckBox;
    private JCheckBox showActiveParserCheckBox;

    public JPanel getPanel() {
        return panel;
    }

    public JCheckBox getUseGitHubMarkdownAPICheckBox() {
        return useGitHubMarkdownAPICheckBox;
    }

    public JPasswordField getGitHubAccessTokenField() {
        return githubAccessTokenField;
    }

    public JSpinner getConnectionTimeoutSpinner() {
        return connectionTimeoutSpinner;
    }

    public JSpinner getSocketTimeoutSpinner() {
        return socketTimeoutSpinner;
    }

    public JCheckBox getUseFullWidthRenderingCheckBox() {
        return useFullWidthRenderingCheckBox;
    }

    public JCheckBox getShowActiveParserCheckBox() {
        return showActiveParserCheckBox;
    }

    @Override
    public void dispose() {
    }
}
