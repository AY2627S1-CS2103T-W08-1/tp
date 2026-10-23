package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    private static final Logger logger = LogsCenter.getLogger(FileUtil.class);

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
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

    /**
     * Deletes {@code temporaryFile}, which a failed write created and left behind, if it is still there.
     * A failure to delete it is only logged, so that it does not hide the failure that left the file behind.
     *
     * @param temporaryFile Temporary file to delete.
     */
    public static void deleteTemporaryFile(Path temporaryFile) {
        requireNonNull(temporaryFile);
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ioe) {
            logger.warning("Could not delete a temporary file in " + temporaryFile.getParent() + ": "
                    + ioe.getClass().getSimpleName());
        }
    }

}
