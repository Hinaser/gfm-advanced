package com.github.hinaser.gfma.browser;

import com.github.hinaser.gfma.template.ErrorTemplate;
import com.github.hinaser.gfma.template.MarkdownTemplate;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class MarkdownParsedAdapter implements MarkdownParsedListener {
    protected IBrowser browser;
    protected String filename;
    protected boolean isHtmlLoadedOnce = false;

    public MarkdownParsedAdapter() { }

    public MarkdownParsedAdapter(IBrowser browser, String filename) {
        this.browser = browser;
        this.filename = filename;
    }

    public void setBrowser(IBrowser browser) {
        this.browser = browser;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void onMarkdownParseDone(String html) {
        try {
            // If any html content is not loaded into the browser, load it.
            // Otherwise, set parsed markdown html is set via javascript for faster loading.
            if(!isHtmlLoadedOnce) {
                MarkdownTemplate template = MarkdownTemplate.getInstance();
                String appliedHtml = template.getGithubFlavoredHtml(filename, html);

                /**
                 * When load string html directly like below, non-ascii multi byte string will be converted to '?'(u+003F)
                 * So instead of loadContent, create temporary html file encoded as UTF-8 and load that html.
                 */
                // browser.loadContent(appliedHtml);

                File file = File.createTempFile("markdown", ".html");
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
                writer.write(appliedHtml);
                writer.close();

                browser.loadFile(file);

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
        catch(Exception e) {
            onMarkdownParseFailed(e.getLocalizedMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onMarkdownParseFailed(String errorMessage, String stackTrace) {
        ErrorTemplate template = ErrorTemplate.getInstance();
        String errorHtml = template.getErrorHtml(errorMessage, stackTrace);
        browser.loadContent(errorHtml);
    }
}
