package com.github.hinaser.gfma.helper;

import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import com.github.hinaser.gfma.markdown.AbstractMarkdownParser;
import com.github.hinaser.gfma.markdown.FlexmarkMarkdownParser;
import com.github.hinaser.gfma.markdown.GitHubAPIMarkdownParser;
import com.github.hinaser.gfma.markdown.MarkdownFile;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class Util {
    public static AbstractMarkdownParser getMarkdownParser(VirtualFile markdownFile, ApplicationSettingsService settings, MarkdownParsedListener l) {
        String parentFolderPath = markdownFile.getParent().getCanonicalPath();
        if(settings.isUseGitHubMarkdownAPI()){
            return GitHubAPIMarkdownParser.getInstance(parentFolderPath, l);
        }
        else{
            return FlexmarkMarkdownParser.getInstance(parentFolderPath, l);
        }
    }

    public static boolean isMarkdownFile(@Nullable VirtualFile virtualFile){
        if(virtualFile == null){
            return false;
        }
        String extension = virtualFile.getExtension();
        return MarkdownFile.isMarkdown(extension);
    }
}
