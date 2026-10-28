package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.function.Executable;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final Path SANDBOX_FOLDER = Paths.get("src", "test", "data", "sandbox");

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting path.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static Path getFilePathInSandboxFolder(String fileName) {
        try {
            Files.createDirectories(SANDBOX_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER.resolve(fileName);
    }

    /**
     * Writes a UTF-8 byte-order mark at the start of {@code file}, before its current content,
     * as an editor does when it saves the file as "UTF-8 with BOM".
     */
    public static void addUtf8ByteOrderMark(Path file) throws IOException {
        byte[] utf8ByteOrderMark = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = Files.readAllBytes(file);

        Files.write(file, utf8ByteOrderMark);
        Files.write(file, content, StandardOpenOption.APPEND);
    }

    /**
     * Replaces the first byte of {@code asciiText} in {@code file} with {@code 0xFF}, which is never part of
     * valid UTF-8, as editing the file in a byte editor or damage on the disk can.
     */
    public static void replaceWithInvalidUtf8Byte(Path file, String asciiText) throws IOException {
        byte[] content = Files.readAllBytes(file);
        // ISO-8859-1 maps each byte to one character, so the index of the text is also its byte index
        int textIndex = new String(content, StandardCharsets.ISO_8859_1).indexOf(asciiText);
        assertTrue(textIndex >= 0, asciiText + " is not in " + file);

        content[textIndex] = (byte) 0xFF;
        Files.write(file, content);
    }

    /**
     * Returns {@code path} relative to the working directory, and skips the calling test if there is no such
     * path, e.g., when {@code path} is on another drive on Windows.
     */
    public static Path toPathRelativeToWorkingDir(Path path) {
        Path workingDir = Paths.get("").toAbsolutePath();
        assumeTrue(workingDir.getRoot().equals(path.getRoot()));
        Path relativePath = workingDir.relativize(path);
        assertFalse(relativePath.isAbsolute());
        return relativePath;
    }

    /**
     * Makes {@code path} read-only, runs {@code test}, then makes {@code path} writable again.
     * Skips the calling test if {@code path} stays writable, e.g., for users with admin rights such as root.
     *
     * @param path File or folder to make read-only.
     * @param test Steps of the test to run while {@code path} is read-only.
     * @throws Throwable if {@code test} throws it.
     */
    public static void runWhileReadOnly(Path path, Executable test) throws Throwable {
        assertTrue(path.toFile().setWritable(false));
        try {
            assumeFalse(Files.isWritable(path));
            test.execute();
        } finally {
            path.toFile().setWritable(true);
        }
    }

    /**
     * Asserts that {@code message} is {@code expectedStart}, then a readable reason, then {@code expectedEnd}.
     * The reason comes from the operating system, so only the problems that {@code Messages#describeWriteFailure}
     * guards against are checked: it is not blank, does not end with a full stop of its own, and does not repeat
     * {@code path}, which the start of the message already shows.
     *
     * @param message Error message to check.
     * @param expectedStart Text expected before the reason.
     * @param expectedEnd Text expected after the reason.
     * @param path File or folder that the message is about.
     */
    public static void assertReadableReason(String message, String expectedStart, String expectedEnd, Path path) {
        assertTrue(message.startsWith(expectedStart), message);
        assertTrue(message.endsWith(expectedEnd), message);
        String reason = message.substring(expectedStart.length(), message.length() - expectedEnd.length());
        assertFalse(reason.isBlank(), message);
        assertFalse(reason.endsWith("."), message);
        assertFalse(reason.contains(path.toString()), message);
    }

    /**
     * Returns the middle index of the patient in the {@code model}'s patient list.
     */
    public static Index getMidIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPatientList().size() / 2);
    }

    /**
     * Returns the last index of the patient in the {@code model}'s patient list.
     */
    public static Index getLastIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPatientList().size());
    }

    /**
     * Returns the patient in the {@code model}'s patient list at {@code index}.
     */
    public static Patient getPatient(Model model, Index index) {
        return model.getFilteredPatientList().get(index.getZeroBased());
    }
}
