package com.github.hinaser.gfma.markdown;

import com.github.hinaser.gfma.GfmABundle;
import com.github.hinaser.gfma.browser.MarkdownParsedListener;
import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubAPIMarkdownParser extends AbstractMarkdownParser {
    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)");
    private final CloseableHttpClient httpClient;
    private final ApplicationSettingsService appSettings;
    protected String parentFolderPath; // Path to the folder which has the parsing markdown file.
    protected boolean isInvalidToken = false;
    protected String lastUsedAccessToken = ""; // For detecting token updated. If invalid token is not updated, don't use it for API request.

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
    public Runnable getMarkdownProcessor(String markdown) {
        return new MarkdownProcessor(markdown);
    }

    public void fallbackToOfflineParser() {
        appSettings.setUseGithubMarkdownAPI(false);
    }

    private class MarkdownProcessor implements Runnable {
        private final String markdown;

        public MarkdownProcessor(String markdown) {
            this.markdown = markdown;
        }

        private String adjustLocalImagePathRelativeToMarkdownPath(String html) {
            Matcher matcher = IMG_PATTERN.matcher(markdown);
            while (matcher.find()) {
                String src = matcher.group(2);
                if(!src.isEmpty()){
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
            return "https://api.github.com/markdown";
        }

        private HttpPost getHttpPost(String url, boolean noAccessToken) {
            HttpPost httpPost = new HttpPost(url);
            ContentType contentType = ContentType.create("application/json", "UTF-8");
            JsonObject json = Json.createObjectBuilder()
                    .add("text", markdown)
                    .add("mode", "gfm")
                    .build();
            String body = json.toString();
            StringEntity stringEntity = new StringEntity(body, contentType);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(appSettings.getSocketTimeout())
                    .setConnectTimeout(appSettings.getConnectionTimeout())
                    .build();
            httpPost.setEntity(stringEntity);
            httpPost.setConfig(requestConfig);

            if(!noAccessToken){
                String authToken = appSettings.getGithubAccessToken();
                if(!authToken.isEmpty()){
                    httpPost.addHeader(HttpHeaders.AUTHORIZATION, "token " + authToken);
                }
            }

            httpPost.addHeader(HttpHeaders.USER_AGENT, "gfmA - An intellij plugin");

            return httpPost;
        }

        private void reportSuccess(String html) {
            String adjustedHtml = adjustLocalImagePathRelativeToMarkdownPath(html);
            markdownParsedListener.onMarkdownParseDone(adjustedHtml);
        }

        private void reportError(String errMsg, String stackTrace) {
            markdownParsedListener.onMarkdownParseFailed(errMsg, stackTrace);
        }

        private String getHeaderValue(CloseableHttpResponse response, String name) {
            Header header = response.getFirstHeader(name);
            if(header == null){
                return "";
            }

            String value = header.getValue();
            return value == null ? "" : value;
        }

        private void sendAPI(boolean noAccessToken) {
            String apiUrl = getApiUrl();
            HttpPost httpPost = getHttpPost(apiUrl, noAccessToken);

            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);
                int xRateLimitLimit = 0;
                int xRateLimitRemaining = 0;
                Date xRateLimitReset = null;

                try {
                    String v = getHeaderValue(response, "X-RateLimit-Limit");
                    xRateLimitLimit = Integer.parseInt(v);
                    appSettings.setRateLimitLimit(xRateLimitLimit);
                }
                catch(NumberFormatException ignored){ }

                try {
                    String v = getHeaderValue(response, "X-RateLimit-Remaining");
                    xRateLimitRemaining = Integer.parseInt(v);
                    appSettings.setRateLimitRemaining(xRateLimitRemaining);
                }
                catch(NumberFormatException ignored){ }

                try {
                    String v = getHeaderValue(response, "X-RateLimit-Reset");
                    xRateLimitReset = new Date(Long.parseLong(v) * 1000);
                    appSettings.setRateLimitReset(xRateLimitReset);
                }
                catch(Exception ignored){ }

                // https://developer.github.com/v3/#rate-limiting
                // As of 2020-07-08, if access token is verified, X-RateLimit-Limit is 5000.
                appSettings.setAccessTokenValid(xRateLimitLimit >= 5000);

                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == 200) {
                    if(!noAccessToken){
                        isInvalidToken = false;
                    }
                    reportSuccess(responseString);
                }
                else if(statusCode == 401){ // 401: Unauthorized
                    isInvalidToken = true;
                    // reportError(GfmABundle.message("gfmA.error.github-bad-credential"), responseString);

                    // Retry API without invalid access token to at least make parser work done.
                    sendAPI(true);
                }
                else if(statusCode == 403) { // 403: Forbidden
                    reportError(GfmABundle.message(
                            "gfmA.error.github-rate-limit",
                            xRateLimitLimit,
                            xRateLimitReset != null ? xRateLimitReset.toString() : "-"
                    ), responseString);
                }
                else {
                    reportError(GfmABundle.message("gfmA.error.github-unknown"), responseString);
                }
            }
            catch (org.apache.commons.httpclient.ConnectTimeoutException | org.apache.http.conn.ConnectTimeoutException e) {
                // reportError(GfmABundle.message("gfmA.error.request-timeout"), ExceptionUtils.getStackTrace(e));
                fallbackToOfflineParser();
            }
            catch (UnknownHostException e) {
                // reportError(GfmABundle.message("gfmA.error.github-unavailable"), ExceptionUtils.getStackTrace(e));
                fallbackToOfflineParser();
            }
            catch (IOException e) {
                // reportError(GfmABundle.message("gfmA.error.io-exception"), ExceptionUtils.getStackTrace(e)); // todo
                fallbackToOfflineParser();
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

        @Override
        public void run() {
            boolean isInvalidTokenStillUsed = isInvalidToken && appSettings.getGithubAccessToken().equals(lastUsedAccessToken);
            sendAPI(isInvalidTokenStillUsed);
        }
    }
}
