package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";
    private static final String TEMPORARY_FILE_SUFFIX = ".tmp";

    private static final Logger logger = LogsCenter.getLogger(FileUtil.class);

    /**
     * Creates parent directories of file if it has a parent directory
     * @throws IOException if a directory cannot be created.
     */
    public static void createParentDirsOfFile(Path file) throws IOException {
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
     * Writes {@code file} through a new temporary file in the same folder: {@code writer} writes the content to the
     * temporary file, which then replaces {@code file}. A write that fails partway therefore never leaves a cut-off
     * {@code file}, and the temporary file is deleted, whatever exception the failure throws. On macOS and Linux,
     * a new temporary file can be opened only by the current user, and {@code file} keeps that.
     *
     * @param file File to write. Its folder must exist.
     * @param writer Writes the content of {@code file} to the temporary file.
     * @throws IOException if the temporary file cannot be created or written, or cannot replace {@code file}.
     */
    public static void writeThroughTemporaryFile(Path file, TemporaryFileWriter writer) throws IOException {
        requireNonNull(file);
        requireNonNull(writer);

        Path folder = file.toAbsolutePath().getParent();
        assert folder != null : "a file to write is always inside a folder";
        Path temporaryFile = Files.createTempFile(folder, file.getFileName() + ".", TEMPORARY_FILE_SUFFIX);

        boolean isReplaced = false;
        try {
            writer.write(temporaryFile);
            Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            isReplaced = true;
        } finally {
            if (!isReplaced) {
                deleteTemporaryFile(temporaryFile);
            }
        }
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

    /**
     * Writes the content of a file to the temporary file that {@code writeThroughTemporaryFile} creates.
     */
    @FunctionalInterface
    public interface TemporaryFileWriter {
        /**
         * Writes the content to {@code temporaryFile}, which exists and is empty.
         *
         * @param temporaryFile Temporary file to write to.
         * @throws IOException if the content cannot be written.
         */
        void write(Path temporaryFile) throws IOException;
    }
}
