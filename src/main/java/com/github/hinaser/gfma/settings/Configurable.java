package com.github.hinaser.gfma.settings;

import com.github.hinaser.gfma.GfmABundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Configurable implements SearchableConfigurable {

    private final ApplicationSettingsService appSettings = ApplicationSettingsService.getInstance();
    private ApplicationSettingsComponent appSettingsComponent;

    @NotNull
    @Override
    public String getId() {
        return "gfmA";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return GfmABundle.message("gfmA.settings.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (appSettingsComponent == null){
            appSettingsComponent = new ApplicationSettingsComponent();
        }
        return appSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return appSettings.isUseFullWidthRendering() != appSettingsComponent.getUseFullWidthRenderingCheckBox().isSelected();
    }

    @Override
    public void apply() throws ConfigurationException {
        appSettings.setUseFullWidthRendering(appSettingsComponent.getUseFullWidthRenderingCheckBox().isSelected());
    }

    @Override
    public void reset() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        appSettingsComponent.getUseFullWidthRenderingCheckBox().setSelected(appSettings.isUseFullWidthRendering());
                    }
                });
            }
        });
    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(this.appSettingsComponent);
        this.appSettingsComponent = null;
    }
}
