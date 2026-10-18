package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

import org.junit.jupiter.api.Test;

public class MessagesTest {

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
        IOException exception = new FileAlreadyExistsException("/data/backups");

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
    public void describeWriteFailure_blankMessage_returnsExceptionType() {
        assertEquals("IOException", Messages.describeWriteFailure(new IOException(" ")));
    }
}
