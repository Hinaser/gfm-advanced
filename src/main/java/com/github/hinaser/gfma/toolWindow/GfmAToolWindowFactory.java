package com.github.hinaser.gfma.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class GfmAToolWindowFactory implements ToolWindowFactory {
    private static final GfmAToolWindow tw = new GfmAToolWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final ContentManager contentManager = toolWindow.getContentManager();
        final Content content = contentManager.getFactory().createContent(tw, null, false);
        contentManager.addContent(content);
    }

    public GfmAToolWindow getToolWindow() {
        return tw;
    }
}
