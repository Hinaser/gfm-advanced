package com.github.hinaser.gfma.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Disposer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@State(
        name = "GfmASettings",
        storages = @Storage("gfma.xml")
)
public class ApplicationSettingsService implements PersistentStateComponent<Element> {
    private static final String USE_GITHUB_MARKDOWN_API = "useGithubMarkdownAPI";
    private static final String GITHUB_ACCESS_TOKEN = "githubAccessToken";
    private static final String CONNECTION_TIMEOUT = "connectionTimeout";
    private static final String SOCKET_TIMEOUT = "socketTimeout";
    private static final String USE_FULL_WIDTH_RENDERING = "useFullWidthRendering";

    private final Set<ApplicationSettingsChangedListener> listeners = new HashSet<ApplicationSettingsChangedListener>();
    private boolean useGithubMarkdownAPI = false;
    private String githubAccessToken = "";
    private int connectionTimeout = 2000;
    private int socketTimeout = 2000;
    private boolean useFullWidthRendering = false;

    public static ApplicationSettingsService getInstance() {
        return ServiceManager.getService(ApplicationSettingsService.class);
    }

    public void setUseGithubMarkdownAPI(boolean useGithubMarkdownAPI) {
        if (this.useGithubMarkdownAPI != useGithubMarkdownAPI) {
            this.useGithubMarkdownAPI = useGithubMarkdownAPI;
            notifyListeners();
        }
    }

    public boolean isUseGithubMarkdownAPI() {
        return useGithubMarkdownAPI;
    }

    public String getGithubAccessToken() {
        return githubAccessToken;
    }

    public void setGithubAccessToken(String githubAccessToken) {
        if (!this.githubAccessToken.equals(githubAccessToken)) {
            this.githubAccessToken = githubAccessToken;
            notifyListeners();
        }
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        if (this.connectionTimeout != connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            notifyListeners();
        }
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        if (this.socketTimeout != socketTimeout) {
            this.socketTimeout = socketTimeout;
            notifyListeners();
        }
    }

    public boolean isUseFullWidthRendering() {
        return useFullWidthRendering;
    }

    public void setUseFullWidthRendering(boolean useFullWidthRendering) {
        if (this.useFullWidthRendering != useFullWidthRendering) {
            this.useFullWidthRendering = useFullWidthRendering;
            notifyListeners();
        }
    }

    public void notifyListeners() {
        for (ApplicationSettingsChangedListener listener : this.listeners) {
            if (listener != null) {
                listener.onApplicationSettingsChanged(this);
            }
        }
    }

    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("GfmASettings");
        element.setAttribute(USE_GITHUB_MARKDOWN_API, String.valueOf(useGithubMarkdownAPI));
        element.setAttribute(GITHUB_ACCESS_TOKEN, githubAccessToken);
        element.setAttribute(CONNECTION_TIMEOUT, String.valueOf(connectionTimeout));
        element.setAttribute(SOCKET_TIMEOUT, String.valueOf(socketTimeout));
        element.setAttribute(USE_FULL_WIDTH_RENDERING, String.valueOf(useFullWidthRendering));
        return element;
    }

    @Override
    public void loadState(Element state) {
        String githubAccessToken = state.getAttributeValue(GITHUB_ACCESS_TOKEN);
        if (githubAccessToken != null) {
            setGithubAccessToken(githubAccessToken);
        }
        String connectionTimeout = state.getAttributeValue(CONNECTION_TIMEOUT);
        if (connectionTimeout != null) {
            setConnectionTimeout(Integer.parseInt(connectionTimeout));
        }
        String socketTimeout = state.getAttributeValue(SOCKET_TIMEOUT);
        if (socketTimeout != null) {
            setSocketTimeout(Integer.parseInt(socketTimeout));
        }
        String useGithubMarkdownAPI = state.getAttributeValue(USE_GITHUB_MARKDOWN_API);
        if (useGithubMarkdownAPI != null) {
            setUseGithubMarkdownAPI(Boolean.parseBoolean(useGithubMarkdownAPI));
        }
        String useFullWidthRendering = state.getAttributeValue(USE_FULL_WIDTH_RENDERING);
        if (useFullWidthRendering != null) {
            setUseFullWidthRendering(Boolean.parseBoolean(useFullWidthRendering));
        }
        notifyListeners();
    }

    public void addApplicationSettingsChangedListener(ApplicationSettingsChangedListener appSettingsChangedListener, Disposable parent) {
        Disposer.register(parent, new DisposableApplicationSettingsChangedListener(appSettingsChangedListener));
        listeners.add(appSettingsChangedListener);
    }

    private class DisposableApplicationSettingsChangedListener implements Disposable {
        private final ApplicationSettingsChangedListener appSettingsChangedListener;

        public DisposableApplicationSettingsChangedListener(ApplicationSettingsChangedListener appSettingsChangedListener) {
            this.appSettingsChangedListener = appSettingsChangedListener;
        }

        @Override
        public void dispose() {
            listeners.remove(appSettingsChangedListener);
        }
    }
}
