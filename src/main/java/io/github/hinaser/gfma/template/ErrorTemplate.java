package io.github.hinaser.gfma.template;

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

public class ErrorTemplate extends AbstractTemplate {
    private static class Singleton {
        private static final ErrorTemplate INSTANCE = new ErrorTemplate();
    }

    @Override
    protected void initializeTemplate() {
        loadTemplate(Resource.MARKDOWN_ERROR);
    }

    public ErrorTemplate getInstance() {
        return Singleton.INSTANCE;
    }

    public String getErrorHtml(String errorMessage, String stackTrace){
        return applyTemplate(errorMessage, stackTrace);
    }
}
