package ch.coop.intellij.plugins.vcs;

/**
 * Exception thrown when a VCS command fails.
 */
public class VcsCommandException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new VcsCommandException with the specified detail message.
     *
     * @param message The detail message.
     */
    public VcsCommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new VcsCommandException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public VcsCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a VcsCommandException for a failed command.
     *
     * @param command The failed command.
     * @param errorOutput The error output of the command.
     * @return A VcsCommandException with a user-friendly error message.
     */
    public static VcsCommandException forCommandFailure(String command, String errorOutput) {
        return new VcsCommandException("Failed to execute command '" + command + "': " + errorOutput);
    }

    /**
     * Creates a VcsCommandException for an unavailable path.
     *
     * @param path The unavailable path.
     * @return A VcsCommandException with a user-friendly error message.
     */
    public static VcsCommandException forPathNotAvailable(String path) {
        return new VcsCommandException("Path '" + path + "' is not available.");
    }
}