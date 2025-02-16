package ch.coop.intellij.plugins.vcs;

import ch.coop.intellij.plugins.helper.RepoHelper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitHandler implements VcsHandler {
    private static final Logger LOG = Logger.getInstance(GitHandler.class);

    @Override
    public boolean isSupported(@NotNull Project project) {
        return isGitAvailable() && new File(project.getBasePath(), ".git").exists();
    }

    @Override
    public List<String> getRepositoryPaths(@NotNull Project project) {
        return RepoHelper.getRepositoryPaths(project, ".git");
    }

    @Override
    public void createBranch(@NotNull Project project, @NotNull String branchName, @NotNull String repositoryPath, boolean push) throws VcsCommandException {
        LOG.info("Creating branch '" + branchName + "' in repository '" + repositoryPath + "'.");

        try {
            // Branch erstellen
            int exitCode = RepoHelper.executeCommand(new String[]{"git", "checkout", "-b", branchName}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("git checkout -b " + branchName, "Exit code: " + exitCode);
            }
            if (push) {
                pushBranch(branchName, repositoryPath);
            }
        } catch (VcsCommandException e) {
            throw new VcsCommandException("Failed to create branch: " + e.getMessage(), e);
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
            int exitCode = RepoHelper.executeCommand(new String[]{"git", "push", "origin", branchName}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("git push origin " + branchName, "Exit code: " + exitCode);
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
            int exitCode = RepoHelper.executeCommand(new String[]{"git", "branch", "-D", branchName}, repositoryPath);
            if (exitCode != 0) {
                throw VcsCommandException.forCommandFailure("git branch -D " + branchName, "Exit code: " + exitCode);
            }
        } catch (VcsCommandException e) {
            throw new VcsCommandException("Failed to execute Git command: " + e.getMessage(), e);
        }
    }

    @Override
    public @Nullable String getCurrentBranch(@NotNull Project project, @NotNull String repositoryPath) throws VcsCommandException {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"git", "branch", "--show-current"}, null, new File(repositoryPath));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorOutput = RepoHelper.readProcessOutput(process.getErrorStream());
                throw VcsCommandException.forCommandFailure("git branch --show-current", errorOutput);
            }
            return RepoHelper.readProcessOutput(process.getInputStream()).trim();
        } catch (IOException | InterruptedException e) {
            throw new VcsCommandException("Failed to execute Git command: " + e.getMessage(), e);
        }
    }

    private boolean isGitAvailable() {
        return RepoHelper.isVcsToolAvailable(new String[]{"git", "--version"});
    }

}