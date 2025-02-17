package ch.coop.intellij.plugins;

import ch.coop.intellij.plugins.urlopener.SearchPattern;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoopPluginsConfigurable implements Configurable {
    private JPanel mainPanel;
    private JBTable table;
    private DefaultTableModel tableModel;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Coop Plugins";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());

        // Titel hinzufügen (mit JBLabel)
        JBLabel titleLabel = new JBLabel("URLOpener Plugin Configuration");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Tabelle für Suchmuster
        tableModel = new DefaultTableModel(new Object[]{"Name", "Shortcut", "URL"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabelle nicht editierbar machen
            }
        };
        table = new JBTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ToolbarDecorator für die Tabelle
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
                .setAddAction(button -> {
                    // Erstelle ein neues, leeres Suchmuster
                    SearchPattern newPattern = new SearchPattern("", "", "");
                    // Öffne den Dialog zum Bearbeiten
                    if (editSearchPattern(newPattern)) {
                        addPatternToTable(newPattern); // Füge das neue Suchmuster zur Tabelle hinzu
                        table.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1); // Markiere das neue Suchmuster
                    }
                })
                .setRemoveAction(button -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        tableModel.removeRow(selectedRow); // Entferne das ausgewählte Suchmuster
                    }
                })
                .setEditAction(button -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        SearchPattern selectedPattern = getPatternFromTable(selectedRow);
                        if (editSearchPattern(selectedPattern)) {
                            updatePatternInTable(selectedRow, selectedPattern); // Aktualisiere das Suchmuster in der Tabelle
                        }
                    }
                });

        // Hauptlayout
        mainPanel.add(decorator.createPanel(), BorderLayout.CENTER);

        // Lade vorhandene Suchmuster
        loadSearchPatterns();

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        List<SearchPattern> currentPatterns = getPatternsFromTable();
        List<SearchPattern> savedPatterns = CoopPluginSettings.getInstance().getSearchPatterns();
        return !currentPatterns.equals(savedPatterns); // Nur true zurückgeben, wenn die Listen unterschiedlich sind
    }

    @Override
    public void apply() {
        // Überprüfe die URLs und doppelten Shortcuts vor dem Speichern
        List<SearchPattern> searchPatterns = getPatternsFromTable();

        // Überprüfe auf doppelte Shortcuts
        Set<String> shortcuts = new HashSet<>();
        for (SearchPattern pattern : searchPatterns) {
            String shortcut = pattern.getShortcut();
            if (shortcuts.contains(shortcut)) {
                // Zeige eine Fehlermeldung an, wenn ein doppelter Shortcut gefunden wird
                Messages.showErrorDialog(
                        mainPanel,
                        "Der Shortcut '" + shortcut + "' ist bereits vorhanden. Bitte verwende einen eindeutigen Shortcut.",
                        "Fehler"
                );
                return; // Breche den Speichervorgang ab
            }
            shortcuts.add(shortcut);
        }

        // Überprüfe die URLs
        for (SearchPattern pattern : searchPatterns) {
            if (!isValidUrl(pattern.getUrl())) {
                // Zeige eine Fehlermeldung an, wenn die URL ungültig ist
                Messages.showErrorDialog(
                        mainPanel,
                        "Die URL für das Suchmuster '" + pattern.getName() + "' ist ungültig. Sie muss '%s' enthalten.",
                        "Fehler"
                );
                return; // Breche den Speichervorgang ab
            }
        }

        // Speichere die Suchmuster, wenn alle Überprüfungen bestanden sind
        CoopPluginSettings.getInstance().setSearchPatterns(searchPatterns);
    }

    @Override
    public void reset() {
        // Lade die Suchmuster neu
        loadSearchPatterns();
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }

    private void loadSearchPatterns() {
        tableModel.setRowCount(0); // Tabelle leeren
        for (SearchPattern pattern : CoopPluginSettings.getInstance().getSearchPatterns()) {
            addPatternToTable(pattern); // Suchmuster zur Tabelle hinzufügen
        }
    }

    private void addPatternToTable(SearchPattern pattern) {
        tableModel.addRow(new Object[]{pattern.getName(), pattern.getShortcut(), pattern.getUrl()});
    }

    private void updatePatternInTable(int row, SearchPattern pattern) {
        tableModel.setValueAt(pattern.getName(), row, 0);
        tableModel.setValueAt(pattern.getShortcut(), row, 1);
        tableModel.setValueAt(pattern.getUrl(), row, 2);
    }

    private SearchPattern getPatternFromTable(int row) {
        return new SearchPattern(
                (String) tableModel.getValueAt(row, 0), // Name
                (String) tableModel.getValueAt(row, 1), // Shortcut
                (String) tableModel.getValueAt(row, 2)  // URL
        );
    }

    private List<SearchPattern> getPatternsFromTable() {
        List<SearchPattern> patterns = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            patterns.add(getPatternFromTable(i));
        }
        return patterns;
    }

    private boolean editSearchPattern(SearchPattern pattern) {
        // Dialogfenster erstellen
        JTextField nameField = new JTextField(pattern.getName());
        JTextField shortcutField = new JTextField(pattern.getShortcut());
        JTextField urlField = new JTextField(pattern.getUrl());

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JBLabel("Name:"));
        panel.add(nameField);
        panel.add(new JBLabel("Kürzel:"));
        panel.add(shortcutField);
        panel.add(new JBLabel("URL:"));
        panel.add(urlField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    mainPanel,
                    panel,
                    "Search Pattern bearbeiten",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String newShortcut = shortcutField.getText().trim();

                // Überprüfe, ob der Shortcut bereits vorhanden ist (außer beim Bearbeiten des aktuellen Musters)
                boolean isDuplicate = getPatternsFromTable().stream()
                        .filter(p -> !p.equals(pattern)) // Ignoriere das aktuelle Muster beim Bearbeiten
                        .anyMatch(p -> p.getShortcut().equals(newShortcut));

                if (isDuplicate) {
                    // Zeige eine Fehlermeldung an, wenn der Shortcut bereits vorhanden ist
                    Messages.showErrorDialog(
                            mainPanel,
                            "Der Shortcut '" + newShortcut + "' ist bereits vorhanden. Bitte verwende einen eindeutigen Shortcut.",
                            "Fehler"
                    );
                    continue; // Zeige den Dialog erneut an
                }

                // Überprüfe, ob die URL gültig ist
                if (!isValidUrl(urlField.getText())) {
                    Messages.showErrorDialog(
                            mainPanel,
                            "Die URL muss den Platzhalter '%s' enthalten.",
                            "Fehler"
                    );
                    continue; // Zeige den Dialog erneut an
                }

                // Speichere die Änderungen
                pattern.setName(nameField.getText());
                pattern.setShortcut(newShortcut);
                pattern.setUrl(urlField.getText());
                return true; // Dialog mit OK geschlossen
            }

            return false; // Dialog mit Abbrechen geschlossen
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && url.contains("%s"); // Überprüfe, ob die URL den Platzhalter '%s' enthält
    }
}