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
    private static final String USE_FULL_WIDTH_RENDERING = "useFullWidthRendering";

    private final Set<ApplicationSettingsChangedListener> listeners = new HashSet<ApplicationSettingsChangedListener>();
    private boolean useFullWidthRendering = false;

    public static ApplicationSettingsService getInstance() {
        return ServiceManager.getService(ApplicationSettingsService.class);
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
        element.setAttribute(USE_FULL_WIDTH_RENDERING, String.valueOf(useFullWidthRendering));
        return element;
    }

    @Override
    public void loadState(Element state) {
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
