package ch.coop.intellij.plugins.helper;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIHelper {

    /**
     * Erstellt einen klickbaren Textlink, der die Konfiguration öffnet.
     *
     * @param linkText       Der Text des Links.
     * @param configurableId Die ID der Konfiguration, die geöffnet werden soll.
     * @param project
     * @return Ein JLabel, das wie ein klickbarer Link aussieht.
     */
    public static JLabel createConfigLink(String linkText, String configurableId, @NotNull Project project) {
        JLabel link = new JLabel("<html><a href=''>" + linkText + "</a></html>");
        link.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Mauszeiger als Hand
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Öffne die Konfiguration
                ShowSettingsUtil.getInstance().showSettingsDialog(project, configurableId);
            }
        });
        return link;
    }
}
