package seedu.address.commons.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes the given string to a file, creating it if needed.
     * Replaces the saved file only after the complete content has been written successfully.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        try (InputStream bytes = new ByteArrayInputStream(content.getBytes(CHARSET))) {
            writeToFile(file, bytes);
        }
    }

    /**
     * Writes to a sibling temporary file before replacing the saved file atomically.
     * Leaves the saved file untouched if writing or replacement fails, and does not close the caller's stream.
     */
    static void writeToFile(Path file, InputStream content) throws IOException {
        Path target = file.toAbsolutePath();
        if (Files.exists(target) && !Files.isWritable(target)) {
            throw new AccessDeniedException(target.toString());
        }
        createParentDirsOfFile(target);
        Path temporaryFile = Files.createTempFile(target.getParent(), ".tutorflow-", ".tmp");
        try {
            Files.copy(content, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

}
