package com.github.hinaser.gfma.settings;

import com.intellij.openapi.Disposable;

import javax.swing.*;

public class ApplicationSettingsComponent implements Disposable {
    private JPanel panel;
    private JLabel useFullWidthRenderingLabel;
    private JCheckBox useFullWidthRenderingCheckBox;

    public JPanel getPanel() {
        return panel;
    }

    public JCheckBox getUseFullWidthRenderingCheckBox() {
        return useFullWidthRenderingCheckBox;
    }

    @Override
    public void dispose() {
    }
}
