package com.github.hinaser.gfma.template;

public enum Resource {
    MARKDOWN_GFM("/com/github/hinaser/gfma/html/markdown.html"),
    MARKDOWN_ERROR("/com/github/hinaser/gfma/html/error.html"),
    ARCHIVED_STYLES("/com/github/hinaser/gfma/html/css.zip")
    ;

    private final String resource;

    Resource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
