package com.github.hinaser.gfma.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public abstract class AbstractTemplate {
    protected String template = "";

    protected AbstractTemplate() {
        initializeTemplate();
    }

    protected abstract void initializeTemplate();

    protected void loadTemplate(Resource r) {
        String resource = r.getResource();
        InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream(resource));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int character = inputStreamReader.read();
            while (character != -1) {
                stringBuilder.append((char) character);
                character = inputStreamReader.read();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read template : " + resource);
        }

        template = stringBuilder.toString();
    }

    public String applyTemplate(Map<String, String> params) {
        return applyForTemplate(template, params);
    }

    public String applyTemplate(Object... params) {
        return applyForTemplate(template, params);
    }

    public String applyTemplate(Map<String, String> keyValueParams, Object... params) {
        String template = this.template;
        template = applyForTemplate(template, keyValueParams);
        template = applyForTemplate(template, params);
        return template;
    }

    private String applyForTemplate(String template, Object key, Object value) {
        return template.replace("{" + key.toString() + "}", value.toString());
    }

    private String applyForTemplate(String template, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            template = applyForTemplate(template, entry.getKey(), entry.getValue());
        }
        return template;
    }

    private String applyForTemplate(String template, Object... params) {
        for (int i = 0; i < params.length; i++) {
            template = applyForTemplate(template, i, params[i]);
        }
        return template;
    }
}
