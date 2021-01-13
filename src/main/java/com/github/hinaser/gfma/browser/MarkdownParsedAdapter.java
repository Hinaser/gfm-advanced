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
            if(!browser.isJsReady()) {
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
                String isFallbackToOfflineParser = settings.isFallingBackToOfflineParser() ? "true" : "false";

                // Escape emojis, quotations, new lines
                // @See https://github.com/Hinaser/gfm-advanced/issues/5
                String escapedHtml = getEscapedHtmlString(html);

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
                        + "  document.getElementById('gfmA-fallback').dataset.gfmafallback = \"" + isFallbackToOfflineParser + "\";\n"
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

    public String getEscapedHtmlString(String html){
        html = html.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");

        StringBuilder escapedHtml = new StringBuilder();
        for(int i=0; i<html.length(); i++){
            int cp = html.codePointAt(i);

            // Handle emoji or character represented by surrogate pair.
            if(cp > 65535){
                StringBuilder sb = new StringBuilder();
                sb.append("&#");
                sb.append(cp);
                sb.append(";");
                escapedHtml.append(sb);
                i++; // It seems low surrogate value is included in `html.codePointAt(high_surrogate_index)`, so skip it.
            }
            else{
                escapedHtml.appendCodePoint(cp);
            }
        }

        return escapedHtml.toString();
    }

    @Override
    public void onMarkdownParseFailed(String errorMessage, String stackTrace) {
        ErrorTemplate template = ErrorTemplate.getInstance();
        String errorHtml = template.getErrorHtml(errorMessage, stackTrace);
        browser.loadContent(errorHtml);
    }
}
