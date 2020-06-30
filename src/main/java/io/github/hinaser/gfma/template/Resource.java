package io.github.hinaser.gfma.template;

public enum Resource {
    MARKDOWN_GFM("/io/github/hinaser/gfma/html/markdown.html"),
    MARKDOWN_ERROR("/io/github/hinaser/gfma/html/error.html"),
    ARCHIVED_STYLES("/io/github/hinaser/gfma/html/css.zip")
    ;

    private final String resource;

    Resource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
