package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(null, temporaryFolder));
    }

    @Test
    public void constructor_nullBackupDir_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(FIXED_CLOCK, null));
    }

    @Test
    public void constructor_noArguments_usesSystemClockAndDefaultFolder() {
        assertEquals(new BackupCommand(Clock.systemDefaultZone(), BackupCommand.DEFAULT_BACKUP_DIR),
                new BackupCommand());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(null));
    }

    @Test
    public void execute_typicalAddressBook_savesAllPatientsToTimestampedFile() throws Exception {
        Path expectedFile = temporaryFolder.resolve(EXPECTED_FILE_NAME);

        CommandResult result = new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(String.format(BackupCommand.MESSAGE_SUCCESS, expectedFile.toAbsolutePath()),
                result.getFeedbackToUser());
        assertEquals(getTypicalAddressBook(), readBackup(expectedFile));
    }

    @Test
    public void execute_emptyAddressBook_savesEmptyBackup() throws Exception {
        Path expectedFile = temporaryFolder.resolve(EXPECTED_FILE_NAME);

        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(new ModelManager());

        assertEquals(new AddressBook(), readBackup(expectedFile));
    }

    @Test
    public void execute_viewedPatient_modelUnchanged() {
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setViewedPatient(expectedModel.getFilteredPatientList().get(0));
        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS,
                temporaryFolder.resolve(EXPECTED_FILE_NAME).toAbsolutePath());

        assertCommandSuccess(new BackupCommand(FIXED_CLOCK, temporaryFolder), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clockInOtherTimeZone_namesFileWithLocalTime() {
        // 08:30:15 UTC is 16:30:15 in Singapore (UTC+8).
        Clock singaporeClock = FIXED_CLOCK.withZone(ZoneId.of("Asia/Singapore"));

        assertCommandSucceedsAndCreates(new BackupCommand(singaporeClock, temporaryFolder),
                temporaryFolder.resolve("mediconnect-20261020-163015.json"));
    }

    @Test
    public void execute_missingBackupFolder_createsFolder() {
        Path backupDir = temporaryFolder.resolve("data").resolve("backups");
        assertFalse(Files.exists(backupDir));

        assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, backupDir),
                backupDir.resolve(EXPECTED_FILE_NAME));
    }

    @Test
    public void execute_olderBackupInFolder_noSuffixAdded() throws Exception {
        Files.createFile(temporaryFolder.resolve("mediconnect-20261019-083015.json"));

        assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, temporaryFolder),
                temporaryFolder.resolve(EXPECTED_FILE_NAME));
    }

    @Test
    public void execute_backupsInSameSecond_addsIncreasingSuffix() throws Exception {
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);
        Path firstFile = temporaryFolder.resolve(EXPECTED_FILE_NAME);
        backupCommand.execute(model);
        String firstFileContent = Files.readString(firstFile);

        assertCommandSucceedsAndCreates(backupCommand, temporaryFolder.resolve("mediconnect-20261020-083015-1.json"));
        assertCommandSucceedsAndCreates(backupCommand, temporaryFolder.resolve("mediconnect-20261020-083015-2.json"));

        // the first backup is not overwritten
        assertEquals(firstFileContent, Files.readString(firstFile));
    }

    @Test
    public void execute_backupFolderCannotBeCreated_throwsCommandExceptionAndModelUnchanged() throws Exception {
        // A regular file where the backup folder's parent should be makes creating the folder fail on every OS.
        Path blockingFile = temporaryFolder.resolve("blocked");
        Files.createFile(blockingFile);
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, blockingFile.resolve("backups"));

        CommandException exception =
                Assertions.assertThrows(CommandException.class, () -> backupCommand.execute(model));

        // the reason comes from the OS, so only the parts MediConnect adds are checked
        assertTrue(exception.getMessage().startsWith("Could not save the backup: "));
        assertTrue(exception.getMessage().endsWith(". Your data has not been changed."));
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

        // different backup folder -> returns false
        assertFalse(backupCommand.equals(new BackupCommand(FIXED_CLOCK, temporaryFolder.resolve("other"))));
    }

    @Test
    public void toStringMethod() {
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);
        String expected = BackupCommand.class.getCanonicalName() + "{clock=" + FIXED_CLOCK
                + ", backupDir=" + temporaryFolder + "}";
        assertEquals(expected, backupCommand.toString());
    }

    /**
     * Executes {@code backupCommand} on {@code model}, confirms that the success message names
     * {@code expectedFile}, that the file now exists, and that {@code model} is unchanged.
     */
    private void assertCommandSucceedsAndCreates(BackupCommand backupCommand, Path expectedFile) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS, expectedFile.toAbsolutePath());

        assertCommandSuccess(backupCommand, model, expectedMessage, expectedModel);
        assertTrue(Files.exists(expectedFile));
    }

    /**
     * Reads the backup at {@code backupFile} using the app's JSON storage, as a later restore would.
     */
    private static AddressBook readBackup(Path backupFile) throws Exception {
        return new AddressBook(new JsonAddressBookStorage(backupFile).readAddressBook().orElseThrow());
    }
}
