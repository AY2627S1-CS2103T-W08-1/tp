package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    private Path temporaryFolder;

    @Test
    public void writeThroughTemporaryFile_nullArguments_throwsNullPointerException() {
        Path file = temporaryFolder.resolve("record.txt");

        assertThrows(NullPointerException.class, () -> FileUtil.writeThroughTemporaryFile(null, temporaryFile -> {}));
        assertThrows(NullPointerException.class, () -> FileUtil.writeThroughTemporaryFile(file, null));
    }

    @Test
    public void writeThroughTemporaryFile_newFile_writesContentAndLeavesNoTemporaryFile() throws Exception {
        Path file = temporaryFolder.resolve("record.txt");

        FileUtil.writeThroughTemporaryFile(file, temporaryFile -> Files.write(temporaryFile, List.of("new")));

        assertEquals(List.of("new"), Files.readAllLines(file));
        assertEquals(List.of(file), listFolder());
    }

    @Test
    public void writeThroughTemporaryFile_existingFile_replacesFile() throws Exception {
        Path file = Files.write(temporaryFolder.resolve("record.txt"), List.of("earlier"));

        FileUtil.writeThroughTemporaryFile(file, temporaryFile -> Files.write(temporaryFile, List.of("later")));

        assertEquals(List.of("later"), Files.readAllLines(file));
        assertEquals(List.of(file), listFolder());
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows has no POSIX file permissions")
    public void writeThroughTemporaryFile_newFile_readableOnlyByOwner() throws Exception {
        Path file = temporaryFolder.resolve("record.txt");

        FileUtil.writeThroughTemporaryFile(file, temporaryFile -> Files.write(temporaryFile, List.of("new")));

        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                Files.getPosixFilePermissions(file));
    }

    @Test
    public void writeThroughTemporaryFile_readOnlyFile_throwsAccessDeniedExceptionAndKeepsFile() throws Exception {
        // on macOS and Linux, the temporary file could otherwise replace a read-only file in a writable folder
        Path file = Files.write(temporaryFolder.resolve("record.txt"), List.of("earlier"));
        file.toFile().setWritable(false);
        assumeFalse(Files.isWritable(file), "the file cannot be made read-only, e.g., when run as root");

        try {
            assertThrows(AccessDeniedException.class, () -> FileUtil.writeThroughTemporaryFile(file,
                    temporaryFile -> Files.write(temporaryFile, List.of("later"))));
            assertEquals(List.of("earlier"), Files.readAllLines(file));
            assertEquals(List.of(file), listFolder());
        } finally {
            file.toFile().setWritable(true);
        }
    }

    @Test
    public void writeThroughTemporaryFile_writeFails_keepsFileAndDeletesTemporaryFile() throws Exception {
        // Equivalence partitioning: a writer failing partway with an IOException, e.g., when the disk is full
        IOException failure = new IOException("disk full");

        assertFailedWriteKeepsFile(failure, temporaryFile -> {
            writeCutContent(temporaryFile);
            throw failure;
        });
    }

    @Test
    public void writeThroughTemporaryFile_replaceFails_deletesTemporaryFile() throws Exception {
        // a folder that is not empty cannot be replaced by a file
        Path file = Files.createDirectory(temporaryFolder.resolve("record.txt"));
        Path insideFile = Files.write(file.resolve("inside.txt"), List.of("inside"));

        Assertions.assertThrows(IOException.class, () ->
                FileUtil.writeThroughTemporaryFile(file, temporaryFile -> Files.write(temporaryFile, List.of("new"))));

        assertEquals(List.of("inside"), Files.readAllLines(insideFile));
        assertEquals(List.of(file), listFolder());
    }

    @Test
    public void writeThroughTemporaryFile_writerThrowsUncheckedException_keepsFileAndDeletesTemporaryFile()
            throws Exception {
        // Equivalence partitioning: a writer failing with an unchecked exception, a separate partition from
        // the IOException that writeThroughTemporaryFile_writeFails_keepsFileAndDeletesTemporaryFile covers
        IllegalStateException failure = new IllegalStateException("bug in writer");

        assertFailedWriteKeepsFile(failure, temporaryFile -> {
            writeCutContent(temporaryFile);
            throw failure;
        });
    }

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

    /**
     * Confirms that writing {@code record.txt}, which already holds "earlier", with {@code failingWriter} rethrows
     * {@code failure}, which the writer throws, keeps the earlier content and leaves no temporary file behind.
     */
    private void assertFailedWriteKeepsFile(Exception failure, FileUtil.TemporaryFileWriter failingWriter)
            throws Exception {
        Path file = Files.write(temporaryFolder.resolve("record.txt"), List.of("earlier"));

        Exception thrown = Assertions.assertThrows(failure.getClass(), () ->
                FileUtil.writeThroughTemporaryFile(file, failingWriter));

        assertSame(failure, thrown);
        assertEquals(List.of("earlier"), Files.readAllLines(file));
        assertEquals(List.of(file), listFolder());
    }

    /**
     * Writes part of some content to {@code temporaryFile}, as a write that fails partway would.
     */
    private static void writeCutContent(Path temporaryFile) throws IOException {
        Files.write(temporaryFile, List.of("cut"));
    }

    /**
     * Returns the files and folders directly in the temporary folder.
     */
    private List<Path> listFolder() throws IOException {
        try (Stream<Path> files = Files.list(temporaryFolder)) {
            return files.toList();
        }
    }
}
