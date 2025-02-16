package ch.coop.intellij.plugins.branchcreator;

import ch.coop.intellij.plugins.CoopPluginSettings;
import ch.coop.intellij.plugins.vcs.VcsHandler;
import ch.coop.intellij.plugins.vcs.VcsHandlerManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BranchCreatorAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(BranchCreatorAction.class);

    private final VcsHandlerManager vcsHandlerManager = new VcsHandlerManager();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (!isProjectValid(project)) {
            Messages.showErrorDialog(project, "No valid project found. Please open a project with a supported VCS.", "Error");
            return;
        }

        // Dialog mit VCS-Informationen erstellen
        BranchCreatorDialog dialog = new BranchCreatorDialog(project);
        if (dialog.showAndGet()) {
            String branchName = dialog.getNewBranchName();
            String repositoryPath = dialog.getSelectedRepositoryPath();
            CoopPluginSettings settings = CoopPluginSettings.getInstance();
            boolean push = settings.getState().autoPush;
            VcsHandler handler = vcsHandlerManager.getSupportedHandler(project);
            if (handler != null) {
                try {
                    handler.createBranch(project, branchName, repositoryPath, push);
                } catch (Exception ex) {
                    Messages.showErrorDialog(project, "Failed to create branch: " + ex.getMessage(), "Error");
                }
            } else {
                Messages.showErrorDialog(project, "No supported VCS repository found. Please ensure your project is under version control.", "Error");
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(isProjectValid(project));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private boolean isProjectValid(@Nullable Project project) {
        if (project == null) {
            return false;
        }

        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
        if (!vcsManager.hasActiveVcss()) {
            return false;
        }

        // Überprüfe, ob ein unterstütztes VCS vorhanden ist
        return vcsHandlerManager.getSupportedHandler(project) != null;
    }
}