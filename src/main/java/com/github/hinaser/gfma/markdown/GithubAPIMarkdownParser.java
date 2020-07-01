package com.github.hinaser.gfma.markdown;

import com.github.hinaser.gfma.GfmABundle;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.github.hinaser.gfma.template.ErrorTemplate;
import com.github.hinaser.gfma.template.MarkdownTemplate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubAPIMarkdownParser extends AbstractMarkdownParser {
    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)");
    private final CloseableHttpClient httpClient;
    private final ApplicationSettingsService appSettings;
    protected String parentFolderPath; // Path to the folder which has the parsing markdown file.

    protected GithubAPIMarkdownParser(String parentFolderPath, MarkdownParsedListener listener) {
        super(listener);
        this.parentFolderPath = parentFolderPath;
        this.markdownParsedListener = listener;
        this.httpClient = HttpClients.createDefault();
        this.appSettings = ApplicationSettingsService.getInstance();
    }

    public static GithubAPIMarkdownParser getInstance(String parentFolderPath, MarkdownParsedListener listener){
        return new GithubAPIMarkdownParser(parentFolderPath, listener);
    }

    @Override
    public Runnable getMarkdownProcessor(String filename, String markdown) {
        return new MarkdownProcessor(filename, markdown);
    }

    private class MarkdownProcessor implements Runnable {
        private final String filename;
        private final String markdown;

        public MarkdownProcessor(String filename, String markdown) {
            this.filename = filename;
            this.markdown = markdown;
        }

        private String adjustLocalImagePathRelativeToMarkdownPath(String html) {
            Matcher matcher = IMG_PATTERN.matcher(markdown);
            while (matcher.find()) {
                var src = matcher.group(2);
                if(!src.isBlank()){
                    html = html.replaceAll("href=[\"']" + src + "[\"']", "onclick=\"false\"");
                    html = html.replaceAll("src=[\"']" + src + "[\"']", "src=\"" + appendParentPath(parentFolderPath, src) + "\"");
                }
            }
            return html;
        }

        private String appendParentPath(String parentFolderPath, String src) {
            if (src.indexOf("://") > 0) {
                return src;
            }

            src = "file://" + parentFolderPath + "/" + src;
            src = src.replaceAll("\\\\", "/");
            return src;
        }

        private String getApiUrl() {
            String githubToken = appSettings.getGithubAccessToken();
            String tokenOption = (githubToken != null && !githubToken.isEmpty()) ? "?access_token=" + githubToken : "";
            return "https://api.github.com/markdown/raw" + tokenOption;
        }

        private HttpPost getHttpPost(String url) {
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(markdown, ContentType.create("text/plain", "UTF-8"));
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(appSettings.getSocketTimeout())
                    .setConnectTimeout(appSettings.getConnectionTimeout())
                    .build();
            httpPost.setEntity(stringEntity);
            httpPost.setConfig(requestConfig);

            return httpPost;
        }

        private void reportSuccess(String html) {
            var adjustedHtml = adjustLocalImagePathRelativeToMarkdownPath(html);
            var template = MarkdownTemplate.getInstance();
            var appliedHtml = template.getGithubFlavoredHtml(filename, adjustedHtml);
            markdownParsedListener.onMarkdownParseDone(appliedHtml);
        }

        private void reportError(String errMsg, String stackTrace) {
            var template = ErrorTemplate.getInstance();
            markdownParsedListener.onMarkdownParseFailed(template.getErrorHtml(errMsg, stackTrace));
        }

        @Override
        public void run() {
            String apiUrl = getApiUrl();
            HttpPost httpPost = getHttpPost(apiUrl);

            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);

                var statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == 200) {
                    reportSuccess(responseString);
                }
                else if(statusCode == 403) {
                    reportError(GfmABundle.message("gfmA.error.github-rate-limit"), responseString);
                }
                else {
                    reportError(GfmABundle.message("gfmA.error.github-unknown"), responseString);
                }
            }
            catch (org.apache.commons.httpclient.ConnectTimeoutException | org.apache.http.conn.ConnectTimeoutException e) {
                reportError(GfmABundle.message("gfmA.error.request-timeout"), ExceptionUtils.getStackTrace(e));
            }
            catch (UnknownHostException e) {
                reportError(GfmABundle.message("gfmA.error.github-unavailable"), ExceptionUtils.getStackTrace(e));
            }
            catch (IOException e) {
                reportError(GfmABundle.message("gfmA.error.io-exception"), ExceptionUtils.getStackTrace(e)); // todo
            }
            finally {
                if (response != null) {
                    try {
                        response.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
