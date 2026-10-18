package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     * Returns {@code path} relative to the working directory, and skips the calling test if there is no such
     * path, e.g., when {@code path} is on another drive on Windows.
     */
    public static Path relativeToWorkingDir(Path path) {
        Path workingDir = Paths.get("").toAbsolutePath();
        assumeTrue(workingDir.getRoot().equals(path.getRoot()));
        Path relativePath = workingDir.relativize(path);
        assertFalse(relativePath.isAbsolute());
        return relativePath;
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
