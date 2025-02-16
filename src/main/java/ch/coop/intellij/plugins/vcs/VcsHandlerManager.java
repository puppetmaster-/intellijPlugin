package ch.coop.intellij.plugins.vcs;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VcsHandlerManager {
    private final List<VcsHandler> handlers = new ArrayList<>();

    public VcsHandlerManager() {
        // Registriere die unterstützten VCS-Handler
        handlers.add(new GitHandler());
        handlers.add(new MercurialHandler());
    }

    /**
     * Gibt eine Liste aller registrierten VCS-Handler zurück.
     *
     * @return Eine Liste aller VCS-Handler.
     */
    public List<VcsHandler> getHandlers() {
        return handlers;
    }

    /**
     * Gibt den unterstützten VCS-Handler für das Projekt zurück.
     *
     * @param project Das aktuelle Projekt.
     * @return Der unterstützte VCS-Handler oder null, wenn keiner gefunden wurde.
     */
    @Nullable
    public VcsHandler getSupportedHandler(@NotNull Project project) {
        for (VcsHandler handler : handlers) {
            if (handler.isSupported(project)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Gibt den Namen des erkannten VCS-Systems zurück.
     *
     * @param project Das aktuelle Projekt.
     * @return Der Name des VCS-Systems oder "No VCS detected", wenn keiner gefunden wurde.
     */
    @NotNull
    public String getDetectedVcsName(@NotNull Project project) {
        VcsHandler handler = getSupportedHandler(project);
        if (handler != null) {
            return handler.getClass().getSimpleName().replace("Handler", "");
        }
        return "No VCS detected";
    }

    /**
     * Gibt den aktuellen Branch des erkannten VCS-Systems zurück.
     *
     * @param project Das aktuelle Projekt.
     * @return Der Name des aktuellen Branches oder "N/A", wenn kein Branch gefunden wurde.
     */
    @NotNull
    public String getCurrentBranch(@NotNull Project project) {
        VcsHandler handler = getSupportedHandler(project);
        if (handler != null) {
            try {
                String branch = handler.getCurrentBranch(project, project.getBasePath());
                return branch != null ? branch : "N/A";
            } catch (VcsCommandException e) {
                return "Error: " + e.getMessage();
            }
        }
        return "N/A";
    }
}