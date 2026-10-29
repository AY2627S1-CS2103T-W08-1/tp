package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MessagesTest {

    @TempDir
    private Path temporaryFolder;

    @Test
    public void describeWriteFailure_nullException_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Messages.describeWriteFailure(null));
    }

    @Test
    public void describeWriteFailure_accessDenied_returnsPermissionMessage() {
        assertEquals(Messages.MESSAGE_NO_WRITE_PERMISSION,
                Messages.describeWriteFailure(new AccessDeniedException("/data/backups/file.json")));
    }

    @Test
    public void describeWriteFailure_fileSystemExceptionWithReason_returnsReasonWithoutPath() {
        IOException exception = new FileSystemException("/data/backups", null, "Not a directory");

        assertEquals("Not a directory", Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_fileSystemExceptionWithoutReason_returnsGenericReasonWithoutPath() {
        IOException exception = new FileSystemException("/data/backups");

        assertEquals(Messages.MESSAGE_FILE_SYSTEM_ERROR, Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_fileInPlaceOfFolder_namesFile() {
        // Equivalence partitioning: the exception that creating a folder throws when a file has its name
        IOException exception = new FileAlreadyExistsException("/data/backups");

        assertEquals(String.format(Messages.MESSAGE_FILE_IN_PLACE_OF_FOLDER, "backups"),
                Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_fileInPlaceOfFolderAbove_namesFile() throws Exception {
        // Equivalence partitioning: the exception that creating a folder throws when a folder above it is a file
        Path dataFile = Files.createFile(temporaryFolder.resolve("data"));
        IOException exception = new FileSystemException(dataFile.resolve("backups").toString(), null,
                "Not a directory");

        assertEquals(String.format(Messages.MESSAGE_FILE_IN_PLACE_OF_FOLDER, "data"),
                Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_fileAlreadyExistsWithoutFile_returnsGenericReason() {
        // Boundary value: no file is given, so there is no name to show
        IOException exception = new FileAlreadyExistsException(null);

        assertEquals(Messages.MESSAGE_FILE_SYSTEM_ERROR, Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_otherIoException_returnsMessage() {
        assertEquals("Disk full", Messages.describeWriteFailure(new IOException("Disk full")));
    }

    @Test
    public void describeWriteFailure_ioExceptionWithoutMessage_returnsExceptionType() {
        assertEquals("IOException", Messages.describeWriteFailure(new IOException()));
    }

    @Test
    public void describeWriteFailure_reasonEndingWithFullStop_fullStopRemoved() {
        // Windows reasons end with a full stop, which MESSAGE_FAILURE already adds after the reason
        IOException exception = new FileSystemException("C:\\data\\backups", null, "The directory name is invalid.");

        assertEquals("The directory name is invalid", Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_blankReason_returnsGenericReason() {
        IOException exception = new FileSystemException("/data/backups", null, "  ");

        assertEquals(Messages.MESSAGE_FILE_SYSTEM_ERROR, Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_messageEndingWithFullStopAndSpace_trimmed() {
        IOException exception = new IOException("No space left on device. ");

        assertEquals("No space left on device", Messages.describeWriteFailure(exception));
    }

    @Test
    public void describeWriteFailure_longRunOfSpacesInsideMessage_spacesKept() {
        String message = "No space" + " ".repeat(60_000) + "left";

        String reason = assertTimeout(Duration.ofSeconds(1), () -> Messages.describeWriteFailure(
                new IOException(message)));

        assertEquals(message, reason);
    }

    @Test
    public void describeWriteFailure_blankMessage_returnsExceptionType() {
        assertEquals("IOException", Messages.describeWriteFailure(new IOException(" ")));
    }
}
