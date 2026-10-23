package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void deleteTemporaryFile_nullFile_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.deleteTemporaryFile(null));
    }

    @Test
    public void deleteTemporaryFile_existingFile_deletesFile() throws Exception {
        Path temporaryFile = Files.createTempFile(temporaryFolder, "export.", ".tmp");

        FileUtil.deleteTemporaryFile(temporaryFile);

        assertFalse(Files.exists(temporaryFile));
    }

    @Test
    public void deleteTemporaryFile_missingFile_doesNotThrow() {
        Path missingFile = temporaryFolder.resolve("missing.tmp");

        FileUtil.deleteTemporaryFile(missingFile);

        assertFalse(Files.exists(missingFile));
    }

    @Test
    public void deleteTemporaryFile_fileCannotBeDeleted_doesNotThrow() throws Exception {
        Path nonEmptyFolder = Files.createDirectory(temporaryFolder.resolve("stuck"));
        Path insideFile = Files.write(nonEmptyFolder.resolve("inside.txt"), List.of("inside"));

        // deleting a folder that is not empty fails, which must only be logged
        FileUtil.deleteTemporaryFile(nonEmptyFolder);

        assertTrue(Files.exists(insideFile));
    }
}
