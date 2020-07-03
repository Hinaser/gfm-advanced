package com.github.hinaser.gfma.editor;

import com.github.hinaser.gfma.template.ErrorTemplate;
import com.github.hinaser.gfma.template.MarkdownTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.github.hinaser.gfma.browser.ChromiumBrowser;
import com.github.hinaser.gfma.markdown.MarkdownParsedListener;
import org.jetbrains.annotations.NotNull;


public class JCefGfmAPreview extends AbstractGfmAPreview {
    private boolean isHtmlLoadedOnce = false;

    public JCefGfmAPreview(@NotNull VirtualFile markdownFile, @NotNull Document document){
        super(markdownFile, document);
        this.browser = new ChromiumBrowser();
    }

    @Override
    @NotNull
    protected MarkdownParsedListener getMarkdownParsedListener() {
        return new MarkdownParsedAdapter();
    }

    @Override
    public void dispose(){

    }

    @Override
    public @NotNull String getName() {
        return "GfmA Preview";
    }

    private class MarkdownParsedAdapter implements MarkdownParsedListener {
        @Override
        public void onMarkdownParseDone(String html) {
            String filename = markdownFile.getName();

            // If any html content is not loaded into the browser, load it.
            // Otherwise, set parsed markdown html is set via javascript for faster loading.
            if(!isHtmlLoadedOnce) {
                MarkdownTemplate template = MarkdownTemplate.getInstance();
                String appliedHtml = template.getGithubFlavoredHtml(filename, html);
                browser.loadContent(appliedHtml);
                isHtmlLoadedOnce = true;
            }
            else {
                String escapedHtml = html
                        .replaceAll("[\"]", "\\\\\"")
                        .replaceAll("[\n]", "\\\\n");
                String javascript = ""
                        + "window.reloadHtml = function(){\n"
                        + "  document.getElementById('title').innerHTML = \"" + filename + "\";\n"
                        + "  document.querySelector('.markdown-body.entry-content').innerHTML = \"" + escapedHtml + "\";\n"
                        + "  document.querySelectorAll('pre code').forEach(function(block){\n"
                        + "    hljs.highlightBlock(block);\n"
                        + "  });\n"
                        + "};\n"
                        + "reloadHtml();\n"
                        ;
                browser.executeJavaScript(javascript);
            }
        }

        @Override
        public void onMarkdownParseFailed(String errorMessage, String stackTrace) {
            ErrorTemplate template = ErrorTemplate.getInstance();
            String errorHtml = template.getErrorHtml(errorMessage, stackTrace);
            browser.loadContent(errorHtml);
        }
    }
}
