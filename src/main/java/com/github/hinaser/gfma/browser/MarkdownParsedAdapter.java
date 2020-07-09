package com.github.hinaser.gfma.browser;

import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.github.hinaser.gfma.template.ErrorTemplate;
import com.github.hinaser.gfma.template.MarkdownTemplate;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class MarkdownParsedAdapter implements MarkdownParsedListener {
    protected ApplicationSettingsService settings;
    protected IBrowser browser;
    protected String filename;
    protected boolean fastHtmlLoadingReady = false;

    public MarkdownParsedAdapter(IBrowser browser, String filename) {
        this.settings = ApplicationSettingsService.getInstance();
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
            // Otherwise, set parsed markdown html via javascript for faster loading.
            if(!fastHtmlLoadingReady) {
                MarkdownTemplate template = MarkdownTemplate.getInstance();
                String appliedHtml = template.getGithubFlavoredHtml(filename, html);

                /*
                 * When load string html directly like below, non-ascii multi byte string will be converted to '?'(u+003F)
                 * So instead of loadContent, create temporary html file encoded as UTF-8 and load that file to browser.
                 */
                // browser.loadContent(appliedHtml);

                File file = File.createTempFile("markdown", ".html");
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                writer.write(appliedHtml);
                writer.close();

                browser.loadFile(file);

                fastHtmlLoadingReady = true;
            }
            else {
                String parser = settings.isUseGithubMarkdownAPI() ? "Github Markdown API" : "Flexmark-java";
                String accessTokenVerified = settings.getGithubAccessToken().isEmpty() ? "not-set"
                        : settings.isGithubAccessTokenValid() ? "verified" : "invalid";
                String rateLimit = !settings.isUseGithubMarkdownAPI() ? "" :
                        "X-RateLimit-Limit = " + settings.getRateLimitLimit().toString() + "\\n" +
                                "X-RateLimit-Remaining = " + settings.getRateLimitRemaining().toString() + "\\n" +
                                "X-RateLimit-Reset = " + settings.getRateLimitReset().toString()
                        ;
                String showActiveParser = settings.isShowActiveParser() ? "true" : "false";

                String escapedHtml = html
                        .replaceAll("[\"]", "\\\\\"")
                        .replaceAll("[\n]", "\\\\n");

                String javascript = ""
                        + "window.reloadHtml = function(){\n"
                        + "  document.getElementById('title').innerText = \"" + filename + "\";\n"
                        + "  document.querySelector('.markdown-body.entry-content').innerHTML = \"" + escapedHtml + "\";\n"
                        + "  document.querySelectorAll('pre code').forEach(function(block){\n"
                        + "    hljs.highlightBlock(block);\n"
                        + "  });\n"
                        + "  document.getElementById('gfmA-parser').innerText = \"" + parser + "\";\n"
                        + "  document.querySelectorAll('[data-gfmaparser]').forEach(function(el){\n"
                        + "    el.dataset.gfmaparser = \"" + parser + "\"\n;"
                        + "  });\n"
                        + "  document.querySelectorAll('[data-gfmaverified]').forEach(function(el){\n"
                        + "    el.dataset.gfmaverified = \"" + accessTokenVerified + "\"\n;"
                        + "  });\n"
                        + "  document.getElementById('gfmA-parser').title = \"" + rateLimit + "\";\n"
                        + "  document.getElementById('gfmA-show-active-parser').dataset.show = \"" + showActiveParser + "\";\n"
                        + "};\n"
                        + "reloadHtml();\n"
                        ;
                browser.executeJavaScript(javascript);
            }
        }
        catch(Exception e) {
            fastHtmlLoadingReady = false;
            onMarkdownParseFailed(e.getLocalizedMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onMarkdownParseFailed(String errorMessage, String stackTrace) {
        fastHtmlLoadingReady = false;

        ErrorTemplate template = ErrorTemplate.getInstance();
        String errorHtml = template.getErrorHtml(errorMessage, stackTrace);
        browser.loadContent(errorHtml);
    }
}
