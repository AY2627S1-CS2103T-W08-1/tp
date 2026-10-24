package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.assertReadableReason;
import static seedu.address.testutil.TestUtil.runWhileReadOnly;
import static seedu.address.testutil.TestUtil.toPathRelativeToWorkingDir;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PatientBuilder;

public class BackupCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T08:30:15Z"), ZoneOffset.UTC);
    private static final String EXPECTED_FILE_NAME = "mediconnect-20261020-083015.json";

    @TempDir
    private Path temporaryFolder;

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
    public void constructor_noArguments_usesSystemClockAndDataBackupsFolder() {
        // the UG and help text tell users that backups go to data/backups
        assertEquals(new BackupCommand(Clock.systemDefaultZone(), Paths.get("data", "backups")),
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
    public void execute_filteredPatientList_savesAllPatients() throws Exception {
        model.updateFilteredPatientList(patient -> false);

        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(getTypicalAddressBook(), readBackup(temporaryFolder.resolve(EXPECTED_FILE_NAME)));
    }

    @Test
    public void execute_specialCharactersInData_savedUnchanged() throws Exception {
        // non-ASCII text (Chinese characters, an en dash and an accented letter), quotes and a backslash
        String address = "\u65b0\u52a0\u5761 Blk 8 \u2013 caf\u00e9 \"quoted\" back\\slash";
        Patient patient = new PatientBuilder().withAddress(address)
                .withMedications("Paracetamol 500mg \ud83d\udc8a").withAllergens("{\"not\": \"json\"}")
                .withCareInstructions("Line one\nLine two").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPatient(patient);

        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(new ModelManager(addressBook, new UserPrefs()));

        assertEquals(addressBook, readBackup(temporaryFolder.resolve(EXPECTED_FILE_NAME)));
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
        // 08:30:15 UTC is 16:30:15 in Singapore (UTC+8)
        Clock singaporeClock = FIXED_CLOCK.withZone(ZoneId.of("Asia/Singapore"));

        assertCommandSucceedsAndCreates(new BackupCommand(singaporeClock, temporaryFolder),
                temporaryFolder.resolve("mediconnect-20261020-163015.json"));
    }

    @Test
    public void execute_relativeBackupFolder_showsFullPath() {
        Path relativeBackupDir = toPathRelativeToWorkingDir(temporaryFolder);

        assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, relativeBackupDir),
                temporaryFolder.resolve(EXPECTED_FILE_NAME));
    }

    @Test
    public void execute_relativeBackupFolderCannotBeCreated_showsFullPathInError() throws Exception {
        Path backupDir = Files.createFile(temporaryFolder.resolve("blocked")).resolve("backups");
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, toPathRelativeToWorkingDir(backupDir));

        String message = Assertions.assertThrows(CommandException.class, () -> backupCommand.execute(model))
                .getMessage();

        assertTrue(message.startsWith("Could not save the backup to " + backupDir + ": "));
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
        // a regular file where the backup folder's parent should be makes creating the folder fail on every OS
        Path blockingFile = temporaryFolder.resolve("blocked");
        Files.createFile(blockingFile);
        Path backupDir = blockingFile.resolve("backups");
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, backupDir);
        String expectedStart = "Could not save the backup to " + backupDir.toAbsolutePath() + ": ";
        String expectedEnd = ". Your data has not been changed.";

        String message = Assertions.assertThrows(CommandException.class, () -> backupCommand.execute(model))
                .getMessage();

        assertReadableReason(message, expectedStart, expectedEnd, backupDir);
        assertCommandFailure(backupCommand, model, message);
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flag on folders")
    public void execute_readOnlyBackupFolder_showsPermissionReasonAndLeavesNoFile() throws Throwable {
        Path backupDir = Files.createDirectory(temporaryFolder.resolve("readonly"));
        String expectedMessage = String.format(BackupCommand.MESSAGE_FAILURE, backupDir.toAbsolutePath(),
                Messages.MESSAGE_NO_WRITE_PERMISSION);

        runWhileReadOnly(backupDir, () -> {
            assertCommandFailure(new BackupCommand(FIXED_CLOCK, backupDir), model, expectedMessage);
            try (Stream<Path> files = Files.list(backupDir)) {
                assertEquals(0, files.count());
            }
        });
    }

    @Test
    public void saveBackup_nullAddressBook_throwsNullPointerException() {
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);

        assertThrows(NullPointerException.class, () -> backupCommand.saveBackup(null));
    }

    @Test
    public void saveBackup_addressBook_savesFileAndReturnsItsPath() throws Exception {
        Path backupFile = new BackupCommand(FIXED_CLOCK, temporaryFolder).saveBackup(getTypicalAddressBook());

        assertEquals(temporaryFolder.resolve(EXPECTED_FILE_NAME), backupFile);
        assertEquals(getTypicalAddressBook(), readBackup(backupFile));
    }

    @Test
    public void saveBackup_folderCannotBeCreated_throwsIoException() throws Exception {
        Path backupDir = Files.createFile(temporaryFolder.resolve("blocked")).resolve("backups");

        assertThrows(IOException.class, () -> new BackupCommand(FIXED_CLOCK, backupDir).saveBackup(new AddressBook()));
    }

    @Test
    public void equals_sameObjectOrSameValues_returnsTrue() {
        // Equivalence partitioning: the two partitions that are equal, the same object and an equal copy
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);

        assertTrue(backupCommand.equals(backupCommand));
        assertTrue(backupCommand.equals(new BackupCommand(FIXED_CLOCK, temporaryFolder)));
    }

    @Test
    public void equals_differentTypeOrValue_returnsFalse() {
        // Equivalence partitioning: null, another type, then one field different at a time,
        // so that each test input has no more than one invalid value
        Clock otherClock = Clock.fixed(Instant.parse("2026-10-21T09:00:00Z"), ZoneOffset.UTC);
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, temporaryFolder);

        assertFalse(backupCommand.equals(null));
        assertFalse(backupCommand.equals(new ClearCommand()));
        assertFalse(backupCommand.equals(new BackupCommand(otherClock, temporaryFolder)));
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
