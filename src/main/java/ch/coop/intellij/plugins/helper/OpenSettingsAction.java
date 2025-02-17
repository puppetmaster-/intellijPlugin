package ch.coop.intellij.plugins.helper;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

public class OpenSettingsAction extends AnAction {

    public OpenSettingsAction() {
        super("Open Settings", "Open URLOpener Plugin Settings", AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), "Coop Plugins");
        });
    }
}
