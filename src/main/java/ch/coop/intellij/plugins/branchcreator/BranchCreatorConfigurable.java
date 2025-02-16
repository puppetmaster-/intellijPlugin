package ch.coop.intellij.plugins.branchcreator;

import ch.coop.intellij.plugins.CoopPluginSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class BranchCreatorConfigurable implements Configurable {
    private JPanel mainPanel;
    private JTextField prefixField;
    private JTextField spaceReplacementField;
    private JCheckBox autoPushCheckBox;
    private JTextField jiraApiUrlField;
    private JPasswordField jiraApiTokenField;

    private final CoopPluginSettings settings;

    public BranchCreatorConfigurable() {
        settings = CoopPluginSettings.getInstance();
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Prefix
        prefixField = new JTextField();
        addLabelAndComponent(mainPanel, gbc, 0, "Prefix:", prefixField);

        // Space Replacement
        spaceReplacementField = new JTextField();
        addLabelAndComponent(mainPanel, gbc, 1, "Space Replacement:", spaceReplacementField);

        // Auto Push
        autoPushCheckBox = new JCheckBox();
        addLabelAndComponent(mainPanel, gbc, 2, "Auto Push:", autoPushCheckBox);

        // Jira API URL
        jiraApiUrlField = new JTextField();
        addLabelAndComponent(mainPanel, gbc, 3, "Jira API URL:", jiraApiUrlField);

        // Jira API Token
        jiraApiTokenField = new JPasswordField();
        addLabelAndComponent(mainPanel, gbc, 4, "Jira API Token:", jiraApiTokenField);

        // Laden Sie die aktuellen Einstellungen
        loadSettings();
    }

    /**
     * Fügt ein Label und eine Komponente zum Panel hinzu.
     *
     * @param panel      Das Panel, zu dem die Komponenten hinzugefügt werden sollen.
     * @param gbc        Die GridBagConstraints für das Layout.
     * @param gridy      Die Zeile, in der die Komponenten platziert werden sollen.
     * @param labelText  Der Text des Labels.
     * @param component  Die Komponente (z. B. JTextField, JCheckBox, JComboBox).
     */
    private void addLabelAndComponent(JPanel panel, GridBagConstraints gbc, int gridy, String labelText, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    /**
     * Lädt die aktuellen Einstellungen in die UI-Komponenten.
     */
    private void loadSettings() {
        prefixField.setText(settings.getState().prefix);
        spaceReplacementField.setText(settings.getState().spaceReplacement);
        autoPushCheckBox.setSelected(settings.getState().autoPush);
        jiraApiUrlField.setText(settings.getState().jiraApiUrl);
        jiraApiTokenField.setText(settings.getState().jiraApiToken);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Repo Branch Creator";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !prefixField.getText().equals(settings.getState().prefix) ||
                !spaceReplacementField.getText().equals(settings.getState().spaceReplacement) ||
                autoPushCheckBox.isSelected() != settings.getState().autoPush ||
                !jiraApiUrlField.getText().equals(settings.getState().jiraApiUrl) ||
                !new String(jiraApiTokenField.getPassword()).equals(settings.getState().jiraApiToken);
    }

    @Override
    public void apply() throws ConfigurationException {
        // Validierung der Jira-API-URL
        String jiraApiUrl = jiraApiUrlField.getText();
        if (!isValidUrl(jiraApiUrl)) {
            throw new ConfigurationException("Invalid Jira API URL. Please enter a valid URL.");
        }

        // Validierung des Jira-API-Tokens
        String jiraApiToken = new String(jiraApiTokenField.getPassword());
        if (jiraApiToken.isEmpty()) {
            throw new ConfigurationException("Jira API Token is required.");
        }

        // Einstellungen speichern
        settings.getState().prefix = prefixField.getText();
        settings.getState().spaceReplacement = spaceReplacementField.getText();
        settings.getState().autoPush = autoPushCheckBox.isSelected();
        settings.getState().jiraApiUrl = jiraApiUrl;
        settings.getState().jiraApiToken = jiraApiToken;
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        prefixField = null;
        spaceReplacementField = null;
        autoPushCheckBox = null;
        jiraApiUrlField = null;
        jiraApiTokenField = null;
    }
}