package ch.coop.intellij.plugins.helper;

import ch.coop.intellij.plugins.vcs.VcsCommandException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RepoHelper {

    /**
     * Führt einen VCS-Befehl aus und gibt den Exit-Code zurück.
     *
     * @param command Der auszuführende Befehl (z. B. ["git", "branch", "--show-current"]).
     * @param workingDirectory Das Arbeitsverzeichnis, in dem der Befehl ausgeführt wird.
     * @return Der Exit-Code des Befehls.
     * @throws VcsCommandException Wenn ein Fehler auftritt.
     */
    public static int executeCommand(String[] command, String workingDirectory) throws VcsCommandException {
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(workingDirectory));
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new VcsCommandException("Failed to execute command: " + e.getMessage(), e);
        }
    }

    /**
     * Liest die Ausgabe eines Prozesses (stdout oder stderr).
     *
     * @param inputStream Der InputStream des Prozesses.
     * @return Die gelesene Ausgabe als String.
     * @throws IOException Wenn ein Fehler beim Lesen auftritt.
     */
    public static String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    /**
     * Überprüft, ob ein VCS-Tool verfügbar ist.
     *
     * @param command Der Befehl, um die Version des Tools zu überprüfen (z. B. ["git", "--version"]).
     * @return true, wenn das Tool verfügbar ist, sonst false.
     */
    public static boolean isVcsToolAvailable(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Durchsucht ein Verzeichnis rekursiv nach Repositorys eines bestimmten Typs.
     *
     * @param directory Das Verzeichnis, das durchsucht werden soll.
     * @param repositoryPaths Die Liste, in die die gefundenen Repository-Pfade gespeichert werden.
     * @param repositoryDirName Der Name des Repository-Verzeichnisses (z. B. ".git" oder ".hg").
     */
    public static void findRepositories(File directory, List<String> repositoryPaths, String repositoryDirName) {
        if (directory.isDirectory()) {
            File repositoryDir = new File(directory, repositoryDirName);
            if (repositoryDir.exists()) {
                repositoryPaths.add(directory.getAbsolutePath());
            }

            File[] subDirs = directory.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    findRepositories(subDir, repositoryPaths, repositoryDirName);
                }
            }
        }
    }

    /**
     * Gibt eine Liste der Repository-Verzeichnisse für ein bestimmtes VCS-System zurück.
     *
     * @param project Das aktuelle Projekt.
     * @param repositoryDirName Der Name des Repository-Verzeichnisses (z. B. ".git" oder ".hg").
     * @return Eine Liste der Repository-Verzeichnisse.
     */
    public static List<String> getRepositoryPaths(@NotNull Project project, String repositoryDirName) {
        List<String> repositoryPaths = new ArrayList<>();
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
            findRepositories(new File(baseDir.getPath()), repositoryPaths, repositoryDirName);
        }
        return repositoryPaths;
    }
}