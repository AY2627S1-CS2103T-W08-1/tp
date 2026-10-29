package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
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
     * Returns the content of {@code file}, which must exist, read as UTF-8 text.
     * Bytes that are not valid UTF-8 make the read fail rather than being replaced by the replacement
     * character, so that text in a file saved in another encoding or damaged is never silently changed.
     *
     * @param file File to read.
     * @return the text in the file.
     * @throws CharacterCodingException if the file is not valid UTF-8.
     * @throws IOException if the file cannot be read.
     */
    public static String readFromFile(Path file) throws IOException {
        return Files.readString(file, CHARSET);
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
     * An existing {@code file} that cannot be written to, e.g., because it is read-only, is not replaced, although
     * on macOS and Linux its folder may let a new file take its place.
     *
     * @param file File to write. Its folder must exist.
     * @param writer Writes the content of {@code file} to the temporary file.
     * @throws AccessDeniedException if {@code file} exists and cannot be written to.
     * @throws IOException if the temporary file cannot be created or written, or cannot replace {@code file}.
     */
    public static void writeThroughTemporaryFile(Path file, TemporaryFileWriter writer) throws IOException {
        requireNonNull(file);
        requireNonNull(writer);
        checkWritableIfExists(file);

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
     * Throws an {@code AccessDeniedException} if {@code file} exists but cannot be written to, e.g., because
     * it is read-only, so that a temporary file does not replace it although the user made it read-only.
     *
     * @throws AccessDeniedException if {@code file} exists and cannot be written to.
     */
    private static void checkWritableIfExists(Path file) throws AccessDeniedException {
        if (Files.exists(file) && !Files.isWritable(file)) {
            throw new AccessDeniedException(file.toString());
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
