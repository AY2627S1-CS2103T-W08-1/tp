package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;

public class BackupCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T08:30:15Z"), ZoneOffset.UTC);
    private static final String EXPECTED_FILE_NAME = "mediconnect-20261020-083015.json";

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(null, temporaryFolder));
        assertThrows(NullPointerException.class, () -> new BackupCommand(FIXED_CLOCK, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(null));
    }

    @Test
    public void execute_validModel_savesTimestampedBackup() throws Exception {
        Path expectedFile = temporaryFolder.resolve(EXPECTED_FILE_NAME);

        CommandResult result = new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(String.format(BackupCommand.MESSAGE_SUCCESS, expectedFile.toAbsolutePath()),
                result.getFeedbackToUser());
        assertEquals(model.getAddressBook(), readBackup(expectedFile));
    }

    @Test
    public void execute_emptyAddressBook_savesEmptyBackup() throws Exception {
        Model emptyModel = new ModelManager();
        Path expectedFile = temporaryFolder.resolve(EXPECTED_FILE_NAME);

        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(emptyModel);

        assertEquals(new AddressBook(), readBackup(expectedFile));
    }

    @Test
    public void execute_missingBackupFolder_createsFolder() throws Exception {
        Path backupDir = temporaryFolder.resolve("data").resolve("backups");
        assertFalse(Files.exists(backupDir));

        new BackupCommand(FIXED_CLOCK, backupDir).execute(model);

        assertTrue(Files.exists(backupDir.resolve(EXPECTED_FILE_NAME)));
    }

    @Test
    public void execute_backupInSameSecond_addsSuffixInsteadOfOverwriting() throws Exception {
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);

        backupCommand.execute(model);
        backupCommand.execute(model);
        backupCommand.execute(model);

        assertTrue(Files.exists(temporaryFolder.resolve(EXPECTED_FILE_NAME)));
        assertTrue(Files.exists(temporaryFolder.resolve("mediconnect-20261020-083015-1.json")));
        assertTrue(Files.exists(temporaryFolder.resolve("mediconnect-20261020-083015-2.json")));
    }

    @Test
    public void execute_unwritableBackupFolder_throwsCommandExceptionAndKeepsData() throws Exception {
        // A regular file where the backup folder should be makes creating the folder fail on every OS.
        Path blockedDir = temporaryFolder.resolve("blocked");
        Files.createFile(blockedDir);
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, blockedDir.resolve("backups"));

        CommandException exception =
                Assertions.assertThrows(CommandException.class, () -> backupCommand.execute(model));

        assertTrue(exception.getMessage().startsWith("Could not save the backup: "));
        assertTrue(exception.getMessage().endsWith("Your data has not been changed."));
        assertCommandFailure(backupCommand, model, exception.getMessage());
    }

    @Test
    public void equals() {
        Clock otherClock = Clock.fixed(Instant.parse("2026-10-21T09:00:00Z"), ZoneOffset.UTC);
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);

        // same object -> returns true
        assertTrue(backupCommand.equals(backupCommand));

        // same values -> returns true
        assertTrue(backupCommand.equals(new BackupCommand(FIXED_CLOCK, temporaryFolder)));

        // null -> returns false
        assertFalse(backupCommand.equals(null));

        // different types -> returns false
        assertFalse(backupCommand.equals(new ClearCommand()));

        // different clock -> returns false
        assertFalse(backupCommand.equals(new BackupCommand(otherClock, temporaryFolder)));

        // different folder -> returns false
        assertFalse(backupCommand.equals(new BackupCommand(FIXED_CLOCK, temporaryFolder.resolve("other"))));
    }

    @Test
    public void toStringMethod() {
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);
        String expected = BackupCommand.class.getCanonicalName() + "{clock=" + FIXED_CLOCK
                + ", backupDir=" + temporaryFolder + "}";
        assertEquals(expected, backupCommand.toString());
    }

    private static AddressBook readBackup(Path backupFile) throws Exception {
        return new AddressBook(new JsonAddressBookStorage(backupFile).readAddressBook().orElseThrow());
    }
}
