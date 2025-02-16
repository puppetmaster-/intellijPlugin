package ch.coop.intellij.plugins;

import ch.coop.intellij.plugins.urlopener.SearchPattern;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoopPluginsConfigurable implements Configurable {
    private JPanel mainPanel;
    private JBList<SearchPattern> jbList;
    private CollectionListModel<SearchPattern> listModel;

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

        // Liste für Suchmuster
        listModel = new CollectionListModel<>();
        jbList = new JBList<>(listModel);
        jbList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer für die Liste
        jbList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SearchPattern) {
                    SearchPattern pattern = (SearchPattern) value;
                    setText(pattern.getName() + " (" + pattern.getShortcut() + ") - " + pattern.getUrl());
                }
                return this;
            }
        });

        // ToolbarDecorator für die Liste
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jbList)
                .setAddAction(button -> {
                    // Erstelle ein neues, leeres Suchmuster
                    SearchPattern newPattern = new SearchPattern("", "", "");
                    // Öffne den Dialog zum Bearbeiten
                    if (editSearchPattern(newPattern)) {
                        listModel.add(newPattern); // Füge das neue Suchmuster hinzu
                        jbList.setSelectedValue(newPattern, true); // Markiere das neue Suchmuster
                    }
                })
                .setRemoveAction(button -> {
                    int selectedIndex = jbList.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        listModel.remove(selectedIndex);
                    }
                })
                .setEditAction(button -> {
                    int selectedIndex = jbList.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        SearchPattern selectedPattern = listModel.getElementAt(selectedIndex);
                        editSearchPattern(selectedPattern); // Öffne den Dialog zum Bearbeiten
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
        return true; // Immer als geändert markieren, da wir keine direkte Überprüfung implementieren
    }

    @Override
    public void apply() {
        // Speichere die Suchmuster
        List<SearchPattern> searchPatterns = new ArrayList<>(listModel.getItems());
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
        listModel.removeAll();
        listModel.addAll(0,CoopPluginSettings.getInstance().getSearchPatterns());
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

        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                panel,
                "Search Pattern bearbeiten",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            pattern.setName(nameField.getText());
            pattern.setShortcut(shortcutField.getText());
            pattern.setUrl(urlField.getText());
            return true; // Dialog mit OK geschlossen
        }

        return false; // Dialog mit Abbrechen geschlossen
    }
}