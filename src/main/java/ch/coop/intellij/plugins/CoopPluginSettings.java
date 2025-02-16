package ch.coop.intellij.plugins;

import ch.coop.intellij.plugins.urlopener.SearchPattern;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent settings for the Branch Creator plugin.
 */
@State(
        name = "CoopPluginSettings",
        storages = {@Storage("CoopPluginSettings.xml")}
)
public final class CoopPluginSettings implements PersistentStateComponent<CoopPluginSettings.State> {

    /**
     * Inner class representing the state of the settings.
     */
    public static class State {
        public static final String DEFAULT_PREFIX = "feature/";
        public static final String DEFAULT_SPACE_REPLACEMENT = "_";
        public static final boolean DEFAULT_AUTO_PUSH = false;
        public static final String DEFAULT_JIRA_API_URL = "";
        public static final String DEFAULT_JIRA_API_TOKEN = "";

        @Nullable
        public String prefix = DEFAULT_PREFIX;
        public String spaceReplacement = DEFAULT_SPACE_REPLACEMENT;
        public boolean autoPush = DEFAULT_AUTO_PUSH;
        public String jiraApiUrl = DEFAULT_JIRA_API_URL;
        public String jiraApiToken = DEFAULT_JIRA_API_TOKEN;

        // Liste der Suchmuster
        public List<SearchPattern> searchPatterns = new ArrayList<>();
        public int defaultSearchPatternIndex = -1; // Index der Standard-URL
    }

    private volatile State myState = new State();

    /**
     * Returns the singleton instance of CoopPluginSettings.
     *
     * @return The singleton instance.
     */
    public static CoopPluginSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(CoopPluginSettings.class);
    }

    @Override
    public @NotNull State getState() {
        return myState;
    }

    @Override
    public synchronized void loadState(@NotNull State state) {
        myState = state;
    }

    /**
     * Validates the settings.
     *
     * @throws ConfigurationException If the settings are invalid.
     */
    public void validate() throws ConfigurationException {
        if (myState.prefix == null || myState.prefix.trim().isEmpty()) {
            throw new ConfigurationException("Prefix cannot be empty.");
        }
        if (myState.spaceReplacement == null || myState.spaceReplacement.trim().isEmpty()) {
            throw new ConfigurationException("Space replacement cannot be empty.");
        }
        if (myState.jiraApiUrl != null && !myState.jiraApiUrl.isEmpty() && !isValidUrl(myState.jiraApiUrl)) {
            throw new ConfigurationException("Invalid Jira API URL.");
        }
        if (myState.jiraApiToken == null || myState.jiraApiToken.trim().isEmpty()) {
            throw new ConfigurationException("Jira API Token cannot be empty.");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

    // Getter und Setter für die Suchmuster
    public List<SearchPattern> getSearchPatterns() {
        return myState.searchPatterns;
    }

    public void setSearchPatterns(List<SearchPattern> searchPatterns) {
        myState.searchPatterns = searchPatterns;
    }

    // Getter und Setter für den Standard-Index
    public int getDefaultSearchPatternIndex() {
        return myState.defaultSearchPatternIndex;
    }

    public void setDefaultSearchPatternIndex(int defaultSearchPatternIndex) {
        myState.defaultSearchPatternIndex = defaultSearchPatternIndex;
    }
}