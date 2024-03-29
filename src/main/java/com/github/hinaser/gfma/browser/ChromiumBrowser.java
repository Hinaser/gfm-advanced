package com.github.hinaser.gfma.browser;

import com.intellij.openapi.Disposable;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.ui.jcef.JBCefJSQuery.Response;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefStringVisitor;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ChromiumBrowser implements IBrowser, Disposable {
    private final JBCefBrowser browser;
    private final JBCefJSQuery jsQuery;
    private final Map<Long, Function<String, Response>> queryHandlers = new HashMap<>();
    private boolean isReadyToExecuteJavaScript;
    private String sourceHtml;

    public ChromiumBrowser() {
        browser = new JBCefBrowser();
        jsQuery = JBCefJSQuery.create((JBCefBrowserBase)browser);
        isReadyToExecuteJavaScript = false;
        addLoadHandler();
    }

    public CefBrowser getBrowser(){
        return browser.getCefBrowser();
    }

    protected void addLoadHandler(){
        var browser = getBrowser();
        var client = browser.getClient();
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
                if(!browser.hasDocument()){
                    return;
                }
                isReadyToExecuteJavaScript = !isLoading;
                if(!isLoading){
                    browser.getSource(new CefStringVisitor() {
                        @Override
                        public void visit(String s) {
                            sourceHtml = s;
                        }
                    });
                }
                else{
                    sourceHtml = "";
                }
            }
        });
    }

    @Override
    public boolean isJsReady(){
        return isReadyToExecuteJavaScript;
    }

    @Override
    public synchronized void loadUrl(@NotNull final String url) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                isReadyToExecuteJavaScript = false;
                browser.loadURL(url);
            }
        });
    }

    @Override
    public synchronized void loadFile(@NotNull final File file) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                isReadyToExecuteJavaScript = false;
                browser.loadURL("file:" + file.getAbsolutePath());
            }
        });
    }

    @Override
    public synchronized void loadContent(@NotNull final String content) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                isReadyToExecuteJavaScript = false;
                browser.loadHTML(content);
            }
        });
    }

    @NotNull
    @Override
    public synchronized JComponent getComponent() {
        return browser.getComponent();
    }

    @Override
    public synchronized void dispose() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                queryHandlers.forEach((k, v) -> {
                    jsQuery.removeHandler(v);
                });
                queryHandlers.clear();

                browser.dispose();
            }
        });
    }

    @Override
    public boolean executeJavaScript(String script){
        if(!isReadyToExecuteJavaScript){
            return false;
        }
        getBrowser().executeJavaScript(script, getBrowser().getURL(), 0);
        return true;
    }

    @Override
    public boolean executeJavaScriptAndReturnValue(String script, Function<String, Response> callback){
        if(!isReadyToExecuteJavaScript){
            return false;
        }
        final Long lIndex = ThreadLocalRandom.current().nextLong();
        final Function<String, Response> handler = (result) -> {
            callback.apply(result);
            Function<String, Response> savedHandler = queryHandlers.get(lIndex);
            jsQuery.removeHandler(savedHandler);
            queryHandlers.remove(lIndex);
            return null;
        };
        jsQuery.addHandler(handler);
        queryHandlers.put(lIndex, handler);

        getBrowser().executeJavaScript(jsQuery.inject("(function(){" + script + "})()"), getBrowser().getURL(), 0);
        return true;
    }

    @Override
    public void goBack() {
        getBrowser().goBack();
    }

    @Override
    public void goForward() {
        getBrowser().goForward();
    }

    @Override
    public void reload() {
        getBrowser().reload();
    }

    @Override
    public void stop() {
        getBrowser().stopLoad();
    }

    @Override
    public String getUrl() {
        return getBrowser().getURL();
    }

    @Override
    public String getHtml() {
        if(getBrowser().isLoading()){
            return "";
        }
        return sourceHtml;
    }

    @Override
    public void getHtmlAsync(@NotNull Function<String, Void> onGetHtml) {
        final CefBrowser browser = getBrowser();
        if(browser.isLoading()){
            onGetHtml.apply("");
            return;
        }

        AtomicBoolean updated = new AtomicBoolean(false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                browser.getSource(new CefStringVisitor() {
                    @Override
                    public void visit(String s) {
                        if(updated.get()){
                            return;
                        }
                        onGetHtml.apply(s);
                        updated.set(true);
                    }
                });
            }
        });
        t.start();
    }

    @Override
    public boolean canGoForward() {
        return getBrowser().canGoForward();
    }

    @Override
    public boolean canGoBack() {
        return getBrowser().canGoBack();
    }

}
