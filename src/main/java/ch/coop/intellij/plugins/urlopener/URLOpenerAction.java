package ch.coop.intellij.plugins.urlopener;

import ch.coop.intellij.plugins.CoopPluginSettings;
import ch.coop.intellij.plugins.helper.OpenSettingsAction;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.SearchTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class URLOpenerAction extends AnAction implements CustomComponentAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Diese Methode wird nicht verwendet, da die Logik in createCustomComponent implementiert ist.
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        SearchTextField searchTextField = new SearchTextField();
        searchTextField.setPreferredSize(new java.awt.Dimension(200, 30)); // Größe anpassen

        searchTextField.getTextEditor().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Reagiere auf die Enter-Taste
                    String input = searchTextField.getText();
                    if (input != null && !input.isEmpty()) {
                        handleInput(input);
                        searchTextField.setText("");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        mainPanel.add(searchTextField, BorderLayout.CENTER);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new OpenSettingsAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "URLOpenerToolbar",
                actionGroup,
                true
        );
        toolbar.setTargetComponent(mainPanel);

        mainPanel.add(toolbar.getComponent(), BorderLayout.EAST);

        return mainPanel;
    }

    private void handleInput(String input) {
        String[] parts = input.split(" ", 2);
        String shortcut = "";
        String searchTerm = "";

        if (parts.length == 1) {
            // Fall: Nur ein Suchbegriff (leerer Shortcut)
            searchTerm = parts[0];
        } else if (parts.length == 2) {
            // Fall: Shortcut und Suchbegriff
            shortcut = parts[0];
            searchTerm = parts[1];
        } else {
            Messages.showErrorDialog("Invalid input format. Use 'shortcut searchTerm' or just 'searchTerm'.", "Error");
            return;
        }

        List<SearchPattern> searchPatterns = CoopPluginSettings.getInstance().getSearchPatterns();

        // Fall: Leerer Shortcut (Standard-URL verwenden)
        if (shortcut.isEmpty()) {
            int defaultIndex = CoopPluginSettings.getInstance().getDefaultSearchPatternIndex();
            if (defaultIndex >= 0 && defaultIndex < searchPatterns.size()) {
                SearchPattern defaultPattern = searchPatterns.get(defaultIndex);
                if (isValidUrl(defaultPattern.getUrl())) {
                    String url = defaultPattern.getUrl().replace("%s", searchTerm);
                    BrowserUtil.browse(url);
                    return;
                } else {
                    Messages.showErrorDialog("The default URL is invalid. It must contain '%s'.", "Error");
                    return;
                }
            } else {
                Messages.showErrorDialog("No default URL is configured.", "Error");
                return;
            }
        }

        // Fall: Shortcut wurde angegeben
        for (SearchPattern pattern : searchPatterns) {
            if (pattern.getShortcut().equals(shortcut)) {
                if (!isValidUrl(pattern.getUrl())) {
                    Messages.showErrorDialog("The URL for shortcut '" + shortcut + "' is invalid. It must contain '%s'.", "Error");
                    return;
                }
                String url = pattern.getUrl().replace("%s", searchTerm);
                BrowserUtil.browse(url);
                return;
            }
        }

        // Shortcut nicht gefunden
        Messages.showErrorDialog("No URL found for shortcut: " + shortcut, "Error");
    }

    private boolean isValidUrl(String url) {
        return url != null && url.contains("%s");
    }
}