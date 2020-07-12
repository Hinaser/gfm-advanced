package com.github.hinaser.gfma.template;

import com.github.hinaser.gfma.settings.ApplicationSettingsService;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MarkdownTemplate extends AbstractTemplate {
    protected ApplicationSettingsService settings = ApplicationSettingsService.getInstance();

    private static class Singleton {
        private static final MarkdownTemplate INSTANCE = new MarkdownTemplate();
    }

    @Override
    protected void initializeTemplate() {
        loadTemplate(Resource.MARKDOWN_GFM);
        loadResource();
    }

    protected void loadResource() {
        File outputFile = unzipAndDeployResource(Resource.ARCHIVED_STYLES.getResource());

        final HashMap<String, Object> markdownParams = new HashMap<String, Object>();
        URL githubCss;
        URL githubCss2;
        URL highlightGithubCss;
        URL highlightMinJs;

        try {
            githubCss = new File(outputFile, FileUtil.join("css", "github-fff66249e57e12b5b264967f6a4d21f8923d59247f86c4419d1e3092660fe54b.css")).toURI().toURL();
            githubCss2 = new File(outputFile, FileUtil.join("css", "github2-ade0148a562b52311cf36a8e5f019126eb5ef7054bf2a0463ea00c536a358d33.css")).toURI().toURL();
            highlightGithubCss = new File(outputFile, FileUtil.join("css", "highlightjs10_1_1", "github.css")).toURI().toURL();
            highlightMinJs = new File(outputFile, FileUtil.join("css", "highlightjs10_1_1", "highlight.min.js")).toURI().toURL();

            markdownParams.put("github.css", githubCss.toExternalForm());
            markdownParams.put("github2.css", githubCss2.toExternalForm());
            markdownParams.put("highlight.github.css", highlightGithubCss.toExternalForm());
            markdownParams.put("highlight.min.js", highlightMinJs.toExternalForm());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Object> param : markdownParams.entrySet()) {
            template = template.replace("{" + param.getKey() + "}", param.getValue().toString());
        }
    }

    public static MarkdownTemplate getInstance() {
        return Singleton.INSTANCE;
    }

    public String getGithubFlavoredHtml(String filename, String markdownHtml) {
        Map<String, String> params = new HashMap<>();
        params.put("width", settings.isUseFullWidthRendering() ?  "100%" : "980px");
        params.put("parser", settings.isUseGithubMarkdownAPI() ? "Github Markdown API" : "Flexmark-java");
        if(!settings.getGithubAccessToken().isEmpty()){
            params.put("verified", settings.isGithubAccessTokenValid() ? "verified" : "invalid");
        }
        else{
            params.put("verified", "not-set");
        }
        if(settings.isUseGithubMarkdownAPI()){
            String rateLimit =
                    "X-RateLimit-Limit = " + settings.getRateLimitLimit().toString() + "\n" +
                    "X-RateLimit-Remaining = " + settings.getRateLimitRemaining().toString() + "\n" +
                    "X-RateLimit-Reset = " + settings.getRateLimitReset().toString()
                    ;
            params.put("rateLimit", rateLimit);
        }
        else{
            params.put("rateLimit", "");
        }
        if(settings.isShowActiveParser()){
            params.put("showActiveParser", "true");
        }
        if(settings.isFallingBackToOfflineParser()){
            params.put("isFallBack", "true");
        }
        else{
            params.put("isFallBack", "false");
        }

        return applyTemplate(params, filename, markdownHtml);
    }

    /**
     * Helped by: https://github.com/shyykoserhiy/gfm/src/main/java/com/github/shyykoserhiy/gfm/resource/Resource.java
     * Unzips resources from zip file to plugin resources folder.
     *
     * @param zipResource reference of zip file
     * @return folder to which all is extracted
     */
    public File unzipAndDeployResource(String zipResource) {
        byte[] buffer = new byte[1024];
        File folder = new File(FileUtil.join(PathManager.getPluginsPath(), "gfm-advanced"));

        try {
            //create output directory is not exists
            if (!folder.exists()) {
                folder.mkdir();
            }
            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(getClass().getResourceAsStream(zipResource));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(folder + File.separator + fileName);
                //create all non exists folders
                new File(newFile.getParent()).mkdirs();
                if (!ze.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                } else {
                    newFile.mkdir();
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace(); //todo
        }
        return folder;
    }
}
