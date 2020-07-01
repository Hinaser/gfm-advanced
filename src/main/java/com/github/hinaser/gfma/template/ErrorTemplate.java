package com.github.hinaser.gfma.template;

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
