package ch.coop.intellij.plugins.vcs;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VcsHandler {
    /**
     * Überprüft, ob das VCS-System im Projekt unterstützt wird.
     *
     * @param project Das aktuelle Projekt.
     * @return true, wenn das VCS-System unterstützt wird, sonst false.
     */
    boolean isSupported(@NotNull Project project);

    /**
     * Gibt eine Liste der Repository-Verzeichnisse zurück, die von diesem VCS-Handler unterstützt werden.
     *
     * @param project Das aktuelle Projekt.
     * @return Eine Liste der Repository-Verzeichnisse.
     */
    List<String> getRepositoryPaths(@NotNull Project project);

    /**
     * Führt einen VCS-Befehl aus, um einen neuen Branch zu erstellen.
     *
     * @param project        Das aktuelle Projekt.
     * @param branchName     Der Name des neuen Branches.
     * @param repositoryPath Der Pfad zum Repository.
     * @throws VcsCommandException Wenn der Befehl fehlschlägt.
     */
    void createBranch(@NotNull Project project, @NotNull String branchName, @NotNull String repositoryPath, boolean push) throws VcsCommandException;

    /**
     * Führt einen VCS-Befehl aus, um einen Branch zu löschen oder rückgängig zu machen.
     *
     * @param project        Das aktuelle Projekt.
     * @param branchName     Der Name des Branches, der gelöscht werden soll.
     * @param repositoryPath Der Pfad zum Repository.
     * @throws VcsCommandException Wenn der Befehl fehlschlägt.
     */
    void undoVcsCommand(@NotNull Project project, @NotNull String branchName, @NotNull String repositoryPath) throws VcsCommandException;

    /**
     * Gibt den Namen des aktuellen Branches zurück.
     *
     * @param project        Das aktuelle Projekt.
     * @param repositoryPath Der Pfad zum Repository.
     * @return Der Name des aktuellen Branches oder null, wenn kein Branch gefunden wurde.
     * @throws VcsCommandException Wenn der Befehl fehlschlägt.
     */
    @Nullable
    String getCurrentBranch(@NotNull Project project, @NotNull String repositoryPath) throws VcsCommandException;
}