package ch.coop.intellij.plugins;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CoopPluginsConfigurable implements Configurable {
    private JTextField baseUrlField;
    private JPanel mainPanel;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Coop Plugins";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // Erstelle ein Panel mit einem Eingabefeld für die Basis-URL
        mainPanel = new JPanel(new BorderLayout());

        JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        urlPanel.add(new JLabel("Base URL:"));
        baseUrlField = new JTextField(20);
        baseUrlField.setText(CoopPluginSettings.getInstance().getState().baseUrl); // Verwende CoopPluginSettings
        urlPanel.add(baseUrlField);

        mainPanel.add(urlPanel, BorderLayout.NORTH);
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        // Überprüfe, ob die Basis-URL geändert wurde
        return !baseUrlField.getText().equals(CoopPluginSettings.getInstance().getState().baseUrl);
    }

    @Override
    public void apply() {
        // Speichere die neue Basis-URL in den Einstellungen
        CoopPluginSettings.getInstance().getState().baseUrl = baseUrlField.getText();
    }

    @Override
    public void reset() {
        // Setze das Eingabefeld auf den aktuellen Wert der Basis-URL zurück
        baseUrlField.setText(CoopPluginSettings.getInstance().getState().baseUrl);
    }

    @Override
    public void disposeUIResources() {
        // Bereinige Ressourcen, falls nötig
        mainPanel = null;
        baseUrlField = null;
    }
}