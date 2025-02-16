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
        // Diese Methode wird nicht verwendet, da wir eine benutzerdefinierte Komponente haben.
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // Hauptpanel für das Suchfeld und das Zahnrad-Symbol
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Erstelle ein SearchTextField
        SearchTextField searchTextField = new SearchTextField();
        searchTextField.setPreferredSize(new java.awt.Dimension(200, 30)); // Größe anpassen

        // Füge einen KeyListener hinzu, um auf die Enter-Taste zu reagieren
        searchTextField.getTextEditor().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Nicht benötigt
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Reagiere auf die Enter-Taste
                    String input = searchTextField.getText();
                    if (input != null && !input.isEmpty()) {
                        handleInput(input);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nicht benötigt
            }
        });

        // Füge das Suchfeld zum Hauptpanel hinzu
        mainPanel.add(searchTextField, BorderLayout.CENTER);

        // Erstelle eine ActionToolbar für das Zahnrad-Symbol
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new OpenSettingsAction()); // Füge die Zahnrad-Action hinzu

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "URLOpenerToolbar",
                actionGroup,
                true
        );
        toolbar.setTargetComponent(mainPanel);

        // Füge die Toolbar zum Hauptpanel hinzu
        mainPanel.add(toolbar.getComponent(), BorderLayout.EAST);

        return mainPanel;
    }

    private void handleInput(String input) {
        // Teile die Eingabe in Kürzel und Suchbegriff auf
        String[] parts = input.split(" ", 2);
        if (parts.length == 2) {
            String shortcut = parts[0];
            String searchTerm = parts[1];

            // Hole die Suchmuster aus den Einstellungen
            List<SearchPattern> searchPatterns = CoopPluginSettings.getInstance().getSearchPatterns();

            // Finde das passende Suchmuster
            for (SearchPattern pattern : searchPatterns) {
                if (pattern.getShortcut().equals(shortcut)) {
                    // Ersetze %s im URL-Muster mit dem Suchbegriff
                    String url = pattern.getUrl().replace("%s", searchTerm);
                    // Öffne die URL im Browser
                    BrowserUtil.browse(url);
                    return;
                }
            }

            // Kein passendes Suchmuster gefunden
            Messages.showErrorDialog("No URL found for shortcut: " + shortcut, "Error");
        } else {
            // Ungültiges Eingabeformat
            Messages.showErrorDialog("Invalid input format. Use 'shortcut searchTerm'.", "Error");
        }
    }
}