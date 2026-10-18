package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.relativeToWorkingDir;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.HOON;
import static seedu.address.testutil.TypicalPatients.IDA;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;

public class RestoreCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-21T09:00:00Z"), ZoneOffset.UTC);
    private static final String BACKUP_NAME = "mediconnect-20261020-083015.json";
    // the backup that restore makes of the current data, named after FIXED_CLOCK
    private static final String AUTOMATIC_BACKUP_NAME = "mediconnect-20261021-090000.json";

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullFileName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RestoreCommand(null, FIXED_CLOCK, temporaryFolder));
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RestoreCommand(BACKUP_NAME, null, temporaryFolder));
    }

    @Test
    public void constructor_nullBackupDir_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new RestoreCommand(BACKUP_NAME, FIXED_CLOCK, null));
    }

    @Test
    public void constructor_fileNameOnly_usesSystemClockAndDataBackupsFolder() {
        // the UG and help text tell users that restore looks in data/backups
        assertEquals(new RestoreCommand(BACKUP_NAME, Clock.systemDefaultZone(), Paths.get("data", "backups")),
                new RestoreCommand(BACKUP_NAME));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> restoreCommand().execute(null));
    }

    @Test
    public void execute_validBackup_replacesDataAndBacksUpCurrentData() throws Exception {
        AddressBook backupData = addressBookOf(HOON, IDA);
        writeBackup(backupData);
        Model expectedModel = new ModelManager(backupData, new UserPrefs());

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(2, "patients"), expectedModel);
        // the automatic backup holds the data from before the restore
        assertEquals(getTypicalAddressBook(), readAddressBook(temporaryFolder.resolve(AUTOMATIC_BACKUP_NAME)));
    }

    @Test
    public void execute_backupWithOnePatient_saysPatient() throws Exception {
        AddressBook backupData = addressBookOf(HOON);
        writeBackup(backupData);

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(1, "patient"),
                new ModelManager(backupData, new UserPrefs()));
    }

    @Test
    public void execute_emptyBackup_removesAllPatients() throws Exception {
        writeBackup(new AddressBook());

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(0, "patients"), new ModelManager());
    }

    @Test
    public void execute_filteredPatientList_showsAllRestoredPatients() throws Exception {
        model.updateFilteredPatientList(patient -> false);
        writeBackup(getTypicalAddressBook());

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(7, "patients"),
                new ModelManager(getTypicalAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_viewedPatientNotInBackup_clearsViewedPatient() throws Exception {
        model.setViewedPatient(ALICE);
        AddressBook backupData = addressBookOf(HOON, IDA);
        writeBackup(backupData);

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(2, "patients"),
                new ModelManager(backupData, new UserPrefs()));
    }

    @Test
    public void execute_viewedPatientInBackup_keepsViewedPatient() throws Exception {
        model.setViewedPatient(ALICE);
        writeBackup(getTypicalAddressBook());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setViewedPatient(ALICE);

        assertCommandSuccess(restoreCommand(), model, expectedSuccessMessage(7, "patients"), expectedModel);
    }

    @Test
    public void execute_relativeBackupFolder_showsFullPathOfAutomaticBackup() throws Exception {
        AddressBook backupData = addressBookOf(HOON);
        writeBackup(backupData);
        RestoreCommand restoreCommand = new RestoreCommand(BACKUP_NAME, FIXED_CLOCK,
                relativeToWorkingDir(temporaryFolder));

        assertCommandSuccess(restoreCommand, model, expectedSuccessMessage(1, "patient"),
                new ModelManager(backupData, new UserPrefs()));
    }

    @Test
    public void execute_fileMadeByBackupCommand_restoresBackedUpData() throws Exception {
        Clock earlierClock = Clock.fixed(Instant.parse("2026-10-20T08:30:15Z"), ZoneOffset.UTC);
        new BackupCommand(earlierClock, temporaryFolder).execute(model);
        model.deletePatient(ALICE);

        restoreCommand().execute(model);

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_automaticBackupRestored_undoesEarlierRestore() throws Exception {
        writeBackup(addressBookOf(HOON));
        restoreCommand().execute(model);

        new RestoreCommand(AUTOMATIC_BACKUP_NAME, FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        // the second restore backs up the data again, under a new name
        assertTrue(Files.exists(temporaryFolder.resolve("mediconnect-20261021-090000-1.json")));
    }

    @Test
    public void execute_missingBackup_throwsCommandException() {
        assertRestoreFailure(String.format(RestoreCommand.MESSAGE_NOT_FOUND, BACKUP_NAME, temporaryFolder));
    }

    @Test
    public void execute_missingBackupInRelativeFolder_showsFullFolderPath() {
        RestoreCommand restoreCommand = new RestoreCommand(BACKUP_NAME, FIXED_CLOCK,
                relativeToWorkingDir(temporaryFolder));

        assertCommandFailure(restoreCommand, model,
                String.format(RestoreCommand.MESSAGE_NOT_FOUND, BACKUP_NAME, temporaryFolder));
    }

    @Test
    public void execute_folderWithBackupName_throwsCommandException() throws Exception {
        Files.createDirectory(temporaryFolder.resolve(BACKUP_NAME));

        assertRestoreFailure(String.format(RestoreCommand.MESSAGE_NOT_FOUND, BACKUP_NAME, temporaryFolder));
    }

    @Test
    public void execute_damagedBackup_throwsCommandException() throws Exception {
        // an empty file, plain text, cut-off JSON, and JSON of the wrong shape
        for (String content : List.of("", "not json", "{\"persons\": [", "[]")) {
            Files.writeString(temporaryFolder.resolve(BACKUP_NAME), content);

            assertRestoreFailure(String.format(RestoreCommand.MESSAGE_UNREADABLE, BACKUP_NAME));
        }
    }

    @Test
    public void execute_invalidPatientInBackup_throwsCommandExceptionWithDetails() throws Exception {
        writeBackup(getTypicalAddressBook());
        Path backupFile = temporaryFolder.resolve(BACKUP_NAME);
        String content = Files.readString(backupFile);
        assertTrue(content.contains("\"" + ALICE.getNric().value + "\""));
        Files.writeString(backupFile, content.replace("\"" + ALICE.getNric().value + "\"", "\"BAD\""));

        assertRestoreFailure(String.format(RestoreCommand.MESSAGE_INVALID_DATA, BACKUP_NAME, Nric.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void execute_duplicatePatientsInBackup_throwsCommandExceptionWithDetails() throws Exception {
        Files.copy(Paths.get("src", "test", "data", "JsonSerializableAddressBookTest",
                "duplicatePatientAddressBook.json"), temporaryFolder.resolve(BACKUP_NAME));

        assertRestoreFailure(String.format(RestoreCommand.MESSAGE_INVALID_DATA, BACKUP_NAME,
                "Patients list contains duplicate patient(s)."));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flags used here")
    public void execute_unreadableBackup_throwsCommandException() throws Exception {
        writeBackup(addressBookOf(HOON));
        Path backupFile = temporaryFolder.resolve(BACKUP_NAME);
        assertTrue(backupFile.toFile().setReadable(false));
        try {
            // users with admin rights (e.g., root) can still read it, so the test only applies to others
            assumeFalse(Files.isReadable(backupFile));

            assertRestoreFailure(String.format(RestoreCommand.MESSAGE_NO_PERMISSION, BACKUP_NAME));
        } finally {
            backupFile.toFile().setReadable(true);
        }
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flag on folders")
    public void execute_currentDataCannotBeBackedUp_throwsCommandExceptionAndRestoresNothing() throws Exception {
        writeBackup(addressBookOf(HOON));
        assertTrue(temporaryFolder.toFile().setWritable(false));
        try {
            // users with admin rights (e.g., root) can still write, so the test only applies to others
            assumeFalse(Files.isWritable(temporaryFolder));
            String expectedMessage = String.format(RestoreCommand.MESSAGE_BACKUP_FAILED, temporaryFolder,
                    BackupCommand.MESSAGE_NO_PERMISSION);

            // a relative folder also checks that the message shows the full path
            RestoreCommand restoreCommand = new RestoreCommand(BACKUP_NAME, FIXED_CLOCK,
                    relativeToWorkingDir(temporaryFolder));

            assertCommandFailure(restoreCommand, model, expectedMessage);
            try (Stream<Path> files = Files.list(temporaryFolder)) {
                assertEquals(List.of(temporaryFolder.resolve(BACKUP_NAME)), files.toList());
            }
        } finally {
            temporaryFolder.toFile().setWritable(true);
        }
    }

    @Test
    public void equals() {
        Clock otherClock = Clock.fixed(Instant.parse("2026-10-22T09:00:00Z"), ZoneOffset.UTC);
        RestoreCommand restoreCommand = restoreCommand();

        // same object -> returns true
        assertTrue(restoreCommand.equals(restoreCommand));

        // same values -> returns true
        assertTrue(restoreCommand.equals(new RestoreCommand(BACKUP_NAME, FIXED_CLOCK, temporaryFolder)));

        // null -> returns false
        assertFalse(restoreCommand.equals(null));

        // different types -> returns false
        assertFalse(restoreCommand.equals(new BackupCommand(FIXED_CLOCK, temporaryFolder)));

        // different file name -> returns false
        assertFalse(restoreCommand.equals(new RestoreCommand("other.json", FIXED_CLOCK, temporaryFolder)));

        // different clock -> returns false
        assertFalse(restoreCommand.equals(new RestoreCommand(BACKUP_NAME, otherClock, temporaryFolder)));

        // different backup folder -> returns false
        assertFalse(restoreCommand.equals(new RestoreCommand(BACKUP_NAME, FIXED_CLOCK,
                temporaryFolder.resolve("other"))));
    }

    @Test
    public void toStringMethod() {
        String expected = RestoreCommand.class.getCanonicalName() + "{fileName=" + BACKUP_NAME
                + ", clock=" + FIXED_CLOCK + ", backupDir=" + temporaryFolder + "}";
        assertEquals(expected, restoreCommand().toString());
    }

    private RestoreCommand restoreCommand() {
        return new RestoreCommand(BACKUP_NAME, FIXED_CLOCK, temporaryFolder);
    }

    private String expectedSuccessMessage(int patientCount, String patientWord) {
        return String.format(RestoreCommand.MESSAGE_SUCCESS, patientCount, patientWord, BACKUP_NAME,
                temporaryFolder.resolve(AUTOMATIC_BACKUP_NAME));
    }

    /**
     * Saves {@code data} as the backup file that {@code restoreCommand()} restores.
     */
    private void writeBackup(ReadOnlyAddressBook data) throws IOException {
        new JsonAddressBookStorage(temporaryFolder.resolve(BACKUP_NAME)).saveAddressBook(data);
    }

    /**
     * Confirms that {@code restoreCommand()} fails with {@code expectedMessage}, leaves {@code model} unchanged,
     * and does not back up the current data.
     */
    private void assertRestoreFailure(String expectedMessage) {
        assertCommandFailure(restoreCommand(), model, expectedMessage);
        assertFalse(Files.exists(temporaryFolder.resolve(AUTOMATIC_BACKUP_NAME)));
    }

    private static AddressBook addressBookOf(Patient... patients) {
        AddressBook addressBook = new AddressBook();
        for (Patient patient : patients) {
            addressBook.addPatient(patient);
        }
        return addressBook;
    }

    private static AddressBook readAddressBook(Path file) throws Exception {
        return new AddressBook(new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }
}
