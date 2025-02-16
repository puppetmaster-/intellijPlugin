package ch.coop.intellij.plugins.vcs;


import ch.coop.intellij.plugins.CoopPluginSettings;
import ch.coop.intellij.plugins.helper.RepoHelper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MercurialHandler implements VcsHandler {
    private static final Logger LOG = Logger.getInstance(MercurialHandler.class);

    @Override
    public boolean isSupported(@NotNull Project project) {
        return isMercurialAvailable() && new File(project.getBasePath(), ".hg").exists();
    }

    @Override
    public List<String> getRepositoryPaths(@NotNull Project project) {
        return RepoHelper.getRepositoryPaths(project, ".hg");
    }

    @Override
    public void createBranch(@NotNull Project project, @NotNull String branchName, @NotNull String repositoryPath, boolean push) throws VcsCommandException {
        LOG.info("Creating branch '" + branchName + "' in repository '" + repositoryPath + "'.");

        try {
            // Branch erstellen
            int exitCode = RepoHelper.executeCommand(new String[]{"hg", "branch", branchName}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("hg branch " + branchName, "Exit code: " + exitCode);
            }

            // Branch pushen, wenn in den Einstellungen aktiviert
            CoopPluginSettings settings = CoopPluginSettings.getInstance();
            if (settings.getState().autoPush) {
                pushBranch(branchName, repositoryPath);
            }
        } catch (VcsCommandException e) {
            throw new VcsCommandException("Failed to execute Mercurial command: " + e.getMessage(), e);
        }
    }

    /**
     * Pusht den erstellten Branch in das Remote-Repository.
     *
     * @param branchName     Der Name des Branches, der gepusht werden soll.
     * @param repositoryPath Der Pfad zum Repository.
     * @throws VcsCommandException Wenn der Push fehlschlägt.
     */
    private void pushBranch(@NotNull String branchName, @NotNull String repositoryPath) throws VcsCommandException {
        LOG.info("Pushing branch '" + branchName + "' to remote repository.");
        try {
            int exitCode = RepoHelper.executeCommand(new String[]{"hg", "push", "--new-branch"}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("hg push --new-branch", "Exit code: " + exitCode);
            }
        } catch (VcsCommandException e) {
            throw new VcsCommandException("Failed to push branch: " + e.getMessage(), e);
        }
    }

    @Override
    public void undoVcsCommand(@NotNull Project project, @NotNull String branchName, @NotNull String repositoryPath) throws VcsCommandException {
        LOG.info("Deleting branch '" + branchName + "' in repository '" + repositoryPath + "'.");

        try {
            // Branch löschen
            int exitCode = RepoHelper.executeCommand(new String[]{"hg", "branch", "--close-branch", branchName}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("hg branch --close-branch " + branchName, "Exit code: " + exitCode);
            }
        } catch (VcsCommandException e) {
            throw new VcsCommandException("Failed to execute Mercurial command: " + e.getMessage(), e);
        }
    }

    @Override
    public @Nullable String getCurrentBranch(@NotNull Project project, @NotNull String repositoryPath) throws VcsCommandException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"hg", "branch"}, null, new File(repositoryPath));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorOutput = RepoHelper.readProcessOutput(process.getErrorStream());
                throw VcsCommandException.forCommandFailure("hg branch", errorOutput);
            }
            return RepoHelper.readProcessOutput(process.getInputStream()).trim();
        } catch (IOException | InterruptedException e) {
            throw new VcsCommandException("Failed to execute Mercurial command: " + e.getMessage(), e);
        }
    }

    private boolean isMercurialAvailable() {
        return RepoHelper.isVcsToolAvailable(new String[]{"hg", "--version"});
    }

}