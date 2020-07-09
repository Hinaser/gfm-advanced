package com.github.hinaser.gfma.browser;

import com.intellij.openapi.Disposable;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.function.Function;

public interface IBrowser extends Disposable {
    void loadUrl(@NotNull String url);

    void loadFile(@NotNull File file);

    void loadContent(@NotNull String content);

    @NotNull
    JComponent getComponent();

    boolean executeJavaScript(String script);

    boolean executeJavaScriptAndReturnValue(String script, Function<String, JBCefJSQuery.Response> callback);

    void goBack();

    void goForward();

    void reload();

    void stop();

    String getUrl();

    String getHtml();

    void getHtmlAsync(@NotNull Function<String, Void> onGetHtml);

    boolean canGoForward();

    boolean canGoBack();

    interface LoadListener {
        void onStartLoadingFrame();

        void onProvisionalLoadingFrame();

        void onFinishLoadingFrame();
    }
}
