package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Optional;
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
    public void findFileInPlaceOfFolder_fileAboveMissingFolder_returnsFile() throws Exception {
        // Equivalence partitioning: a file where a folder above the path should be, e.g., data for data/backups
        Path dataFile = Files.createFile(temporaryFolder.resolve("data"));

        assertEquals(Optional.of(dataFile),
                FileUtil.findFileInPlaceOfFolder(dataFile.resolve("backups").resolve("backup.json")));
    }

    @Test
    public void findFileInPlaceOfFolder_onlyFoldersAbove_returnsEmpty() {
        // Equivalence partitioning: every folder above that exists is a folder, and the rest are missing
        Path missingFolder = temporaryFolder.resolve("data").resolve("backups");

        assertEquals(Optional.empty(), FileUtil.findFileInPlaceOfFolder(missingFolder));
    }

    @Test
    public void findFileInPlaceOfFolder_pathItselfIsFile_returnsEmpty() throws Exception {
        // Boundary value: only the folders above the path are checked, not the path itself
        Path file = Files.createFile(temporaryFolder.resolve("backups"));

        assertEquals(Optional.empty(), FileUtil.findFileInPlaceOfFolder(file));
    }

    @Test
    public void readFromFile_multiByteUtf8_returnsText() throws Exception {
        // Equivalence partitioning: valid UTF-8 with characters of two and three bytes, e.g., accented and
        // Chinese names
        Path file = temporaryFolder.resolve("record.txt");
        FileUtil.writeToFile(file, "Zoë Tan 陈");

        assertEquals("Zoë Tan 陈", FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_textSavedInAnotherEncoding_throwsCharacterCodingException() throws Exception {
        // Equivalence partitioning: text saved by an editor in Windows-1252 ("ANSI"), which writes "é" as
        // the single byte E9, a byte that cannot appear on its own in UTF-8
        byte[] content = {'C', 'a', 'f', (byte) 0xE9, '!'};
        Path file = Files.write(temporaryFolder.resolve("record.txt"), content);

        assertThrows(CharacterCodingException.class, () -> FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_characterCutOffAtEnd_throwsCharacterCodingException() throws Exception {
        // Boundary value analysis: the first byte of a two-byte character ("é" is C3 A9) as the file's last
        // byte
        byte[] content = {'C', 'a', 'f', (byte) 0xC3};
        Path file = Files.write(temporaryFolder.resolve("record.txt"), content);

        assertThrows(CharacterCodingException.class, () -> FileUtil.readFromFile(file));
    }

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
    public void writeThroughTemporaryFile_folderInPlaceOfFile_throwsWithoutTemporaryFile() throws Exception {
        // a file cannot replace a folder, so the folder is reported before any temporary file is made
        Path file = Files.createDirectory(temporaryFolder.resolve("record.txt"));
        Path insideFile = Files.write(file.resolve("inside.txt"), List.of("inside"));

        FileSystemException exception = Assertions.assertThrows(FileSystemException.class, () ->
                FileUtil.writeThroughTemporaryFile(file, temporaryFile -> Files.write(temporaryFile, List.of("new"))));

        assertEquals(String.format(FileUtil.MESSAGE_FOLDER_IN_PLACE_OF_FILE, "record.txt"),
                exception.getReason());

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
