package com.github.hinaser.gfma.settings;

import com.intellij.openapi.Disposable;

import javax.swing.*;

public class ApplicationSettingsComponent implements Disposable {
    private JPanel panel;
    private JCheckBox useGithubMarkdownAPICheckBox;
    private JPasswordField githubAccessTokenField;
    private JSpinner connectionTimeoutSpinner;
    private JSpinner socketTimeoutSpinner;
    private JCheckBox useFullWidthRenderingCheckBox;

    public JPanel getPanel() {
        return panel;
    }

    public JCheckBox getUseGithubMarkdownAPICheckBox() {
        return useGithubMarkdownAPICheckBox;
    }

    public JPasswordField getGithubAccessTokenField() {
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

    @Override
    public void dispose() {
    }
}
