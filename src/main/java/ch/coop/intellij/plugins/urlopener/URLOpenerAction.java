package ch.coop.intellij.plugins.urlopener;

import ch.coop.intellij.plugins.CoopPluginSettings;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.ui.SearchTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class URLOpenerAction extends AnAction implements CustomComponentAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Diese Methode wird nicht verwendet, da wir eine benutzerdefinierte Komponente haben.
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        // Erstelle ein SearchTextField
        SearchTextField searchTextField = new SearchTextField();
        searchTextField.setPreferredSize(new java.awt.Dimension(200, 30)); // Größe anpassen

        // Füge einen DocumentListener hinzu, um auf Änderungen im Textfeld zu reagieren
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
                        String baseUrl = CoopPluginSettings.getInstance().getState().baseUrl;
                        String fullUrl = baseUrl + input;
                        // Öffne die URL im Browser
                        BrowserUtil.browse(fullUrl);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nicht benötigt
            }
        });

        return searchTextField;
    }
}