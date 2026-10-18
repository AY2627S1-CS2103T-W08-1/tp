package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

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
    public void execute_filteredPatientList_savesAllPatients() throws Exception {
        model.updateFilteredPatientList(patient -> false);

        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(getTypicalAddressBook(), readBackup(temporaryFolder.resolve(EXPECTED_FILE_NAME)));
    }

    @Test
    public void execute_specialCharactersInData_savedUnchanged() throws Exception {
        // Chinese characters, an en dash, an accented letter, quotes and a backslash
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
    public void execute_nonEnglishDefaultLocale_namesFileWithAsciiDigits() {
        Locale originalLocale = Locale.getDefault();
        try {
            for (String languageTag : List.of("ar-SA", "th-TH-u-nu-thai", "fa-IR", "ja-JP-u-ca-japanese")) {
                Locale.setDefault(Locale.forLanguageTag(languageTag));
                Path backupDir = temporaryFolder.resolve(languageTag);

                assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, backupDir),
                        backupDir.resolve(EXPECTED_FILE_NAME));
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
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
    public void execute_relativeBackupFolder_showsFullPath() {
        Path workingDir = Paths.get("").toAbsolutePath();
        // on Windows the temporary folder can be on another drive, where no relative path exists
        assumeTrue(workingDir.getRoot().equals(temporaryFolder.getRoot()));
        Path relativeBackupDir = workingDir.relativize(temporaryFolder);
        assertFalse(relativeBackupDir.isAbsolute());

        assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, relativeBackupDir),
                temporaryFolder.resolve(EXPECTED_FILE_NAME).toAbsolutePath());
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
        Path backupDir = blockingFile.resolve("backups");
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        BackupCommand backupCommand = new BackupCommand(FIXED_CLOCK, backupDir);

        CommandException exception =
                Assertions.assertThrows(CommandException.class, () -> backupCommand.execute(model));

        // the reason comes from the OS, so only the parts MediConnect adds are checked
        assertTrue(exception.getMessage().startsWith(
                "Could not save the backup to " + backupDir.toAbsolutePath() + ": "));
        assertTrue(exception.getMessage().endsWith(". Your data has not been changed."));
        assertFalse(exception.getMessage().contains("null"));
        assertCommandFailure(backupCommand, model, exception.getMessage());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS) // Windows ignores the read-only flag on folders
    public void execute_readOnlyBackupFolder_showsPermissionReasonAndLeavesNoFile() throws Exception {
        Path backupDir = Files.createDirectory(temporaryFolder.resolve("readonly"));
        assertTrue(backupDir.toFile().setWritable(false));
        try {
            // users with admin rights (e.g., root) can still write, so the test only applies to others
            assumeFalse(Files.isWritable(backupDir));
            String expectedMessage = String.format(BackupCommand.MESSAGE_FAILURE, backupDir.toAbsolutePath(),
                    BackupCommand.MESSAGE_NO_PERMISSION);

            assertCommandFailure(new BackupCommand(FIXED_CLOCK, backupDir), model, expectedMessage);
            try (Stream<Path> files = Files.list(backupDir)) {
                assertEquals(0, files.count());
            }
        } finally {
            backupDir.toFile().setWritable(true);
        }
    }

    @Test
    public void describeFailure_nullException_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> BackupCommand.describeFailure(null));
    }

    @Test
    public void describeFailure_accessDenied_returnsPermissionMessage() {
        assertEquals(BackupCommand.MESSAGE_NO_PERMISSION,
                BackupCommand.describeFailure(new AccessDeniedException("/data/backups/file.json")));
    }

    @Test
    public void describeFailure_fileSystemExceptionWithReason_returnsReasonWithoutPath() {
        IOException exception = new FileSystemException("/data/backups", null, "Not a directory");

        assertEquals("Not a directory", BackupCommand.describeFailure(exception));
    }

    @Test
    public void describeFailure_fileSystemExceptionWithoutReason_returnsGenericReasonWithoutPath() {
        IOException exception = new FileAlreadyExistsException("/data/backups");

        assertEquals(BackupCommand.MESSAGE_FILE_SYSTEM_ERROR, BackupCommand.describeFailure(exception));
    }

    @Test
    public void describeFailure_otherIoException_returnsMessage() {
        assertEquals("Disk full", BackupCommand.describeFailure(new IOException("Disk full")));
    }

    @Test
    public void describeFailure_ioExceptionWithoutMessage_returnsExceptionType() {
        assertEquals("IOException", BackupCommand.describeFailure(new IOException()));
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
