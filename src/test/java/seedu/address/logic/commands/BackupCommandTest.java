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
import java.nio.file.attribute.PosixFilePermission;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PatientBuilder;

public class BackupCommandTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T08:30:15Z"), ZoneOffset.UTC);
    private static final String EXPECTED_FILE_NAME = "mediconnect-20261020-083015.json";
    /** A day before {@code FIXED_CLOCK}, so that backups made with it never take a name that it wants. */
    private static final Clock EARLIER_CLOCK =
            Clock.fixed(Instant.parse("2026-10-19T08:30:15Z"), ZoneOffset.UTC);
    /** Number of commands run at the same moment, which is enough to make names collide without the fix. */
    private static final int SIMULTANEOUS_COMMANDS = 30;
    private static final int TIMEOUT_SECONDS = 30;

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
    public void execute_fileInPlaceOfBackupFolder_namesFileInError() throws Exception {
        // Equivalence partitioning: a file with the backup folder's name, which stops the folder being made
        Path backupDir = Files.createFile(temporaryFolder.resolve("backups"));
        String expectedMessage = String.format(BackupCommand.MESSAGE_FAILURE, backupDir,
                String.format(Messages.MESSAGE_FILE_IN_PLACE_OF_FOLDER, "backups"));

        assertCommandFailure(new BackupCommand(FIXED_CLOCK, backupDir), model, expectedMessage);
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
    public void execute_folderWithBackupName_takesNextName() throws Exception {
        // Equivalence partitioning: the name is taken by a folder rather than a file
        Files.createDirectory(temporaryFolder.resolve(EXPECTED_FILE_NAME));

        assertCommandSucceedsAndCreates(new BackupCommand(FIXED_CLOCK, temporaryFolder),
                temporaryFolder.resolve("mediconnect-20261020-083015-1.json"));
    }

    @Test
    public void saveBackup_manyBackupsAtSameTime_eachSavedToOwnFile() throws Exception {
        // Equivalence partitioning: backups made at the same moment, e.g., in two MediConnect windows, all
        // wanting the same name, since the clock is fixed
        AddressBook data = getTypicalAddressBook();
        List<Callable<Path>> backups = new ArrayList<>();
        for (int i = 0; i < SIMULTANEOUS_COMMANDS; i++) {
            backups.add(() -> new BackupCommand(FIXED_CLOCK, temporaryFolder).saveBackup(data));
        }

        List<Path> backupFiles = runAtSameTime(backups);

        assertEquals(SIMULTANEOUS_COMMANDS, new HashSet<>(backupFiles).size());
        assertEquals(new HashSet<>(backupFiles), new HashSet<>(listBackups()));
        for (Path backupFile : backupFiles) {
            assertEquals(data, readBackup(backupFile));
        }
    }

    @Test
    public void saveBackup_backupRestoreAndClearAtSameTime_eachSavedToOwnFile() throws Exception {
        // Equivalence partitioning: restore and clear confirm also back up the current data first, through
        // saveBackup, so the folder must end with one backup per command besides the backup restored
        String restoredName = new BackupCommand(EARLIER_CLOCK, temporaryFolder)
                .saveBackup(getTypicalAddressBook()).getFileName().toString();
        List<Callable<CommandResult>> commands = new ArrayList<>();
        for (int i = 0; i < SIMULTANEOUS_COMMANDS; i++) {
            Command command = createCommandThatBacksUp(i, restoredName);
            commands.add(() -> command.execute(new ModelManager(getTypicalAddressBook(), new UserPrefs())));
        }

        runAtSameTime(commands);

        List<Path> backups = listBackups();
        assertEquals(SIMULTANEOUS_COMMANDS + 1, backups.size());
        for (Path backupFile : backups) {
            assertEquals(getTypicalAddressBook(), readBackup(backupFile));
        }
    }

    @Test
    public void saveBackup_writeFails_leavesNoBackupOrTemporaryFile() throws Exception {
        // Equivalence partitioning: the data cannot be written once the lock is held and the name chosen
        ReadOnlyAddressBook unreadableData = () -> {
            throw new IllegalStateException("the patient list cannot be read");
        };

        assertThrows(IllegalStateException.class, () ->
                new BackupCommand(FIXED_CLOCK, temporaryFolder).saveBackup(unreadableData));

        assertEquals(List.of(), listBackups());
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
            assertEquals(List.of(), listFiles(backupDir));
        });
    }

    @Test
    public void execute_success_leavesOnlyBackupAndLockFile() throws Exception {
        // Equivalence partitioning: a successful backup, which leaves no temporary file behind, only the
        // backup and the empty lock file that backups take turns on
        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        Set<Path> expectedFiles = Set.of(temporaryFolder.resolve(EXPECTED_FILE_NAME),
                temporaryFolder.resolve(BackupCommand.LOCK_FILE_NAME));
        assertEquals(expectedFiles, new HashSet<>(listFiles(temporaryFolder)));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows has no POSIX file permissions")
    public void execute_success_backupReadableOnlyByOwner() throws Exception {
        new BackupCommand(FIXED_CLOCK, temporaryFolder).execute(model);

        assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
                Files.getPosixFilePermissions(temporaryFolder.resolve(EXPECTED_FILE_NAME)));
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

    /**
     * Returns the command at {@code position} in a mix of commands that each back up the current data to the
     * temporary folder first: {@code backup}, {@code restore} of the backup named {@code restoredName}, and
     * {@code clear confirm}, in turn.
     */
    private Command createCommandThatBacksUp(int position, String restoredName) {
        return switch (position % 3) {
            case 0 -> new BackupCommand(FIXED_CLOCK, temporaryFolder);
            case 1 -> new RestoreCommand(restoredName, FIXED_CLOCK, temporaryFolder);
            default -> new ClearCommand(FIXED_CLOCK, temporaryFolder);
        };
    }

    /**
     * Returns the files and folders in {@code folder}.
     */
    private static List<Path> listFiles(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.toList();
        }
    }

    /**
     * Returns the files and folders in the temporary folder other than the lock file, which every backup
     * leaves.
     */
    private List<Path> listBackups() throws IOException {
        return listFiles(temporaryFolder).stream()
                .filter(file -> !file.getFileName().toString().equals(BackupCommand.LOCK_FILE_NAME))
                .toList();
    }

    /**
     * Runs {@code tasks} at the same moment, each on its own thread, and returns their results in order.
     * A task that throws makes this throw an {@code ExecutionException} with that exception as its cause.
     */
    private static <T> List<T> runAtSameTime(List<Callable<T>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CyclicBarrier start = new CyclicBarrier(tasks.size());
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    // every thread waits here until all are ready, so that the tasks start together
                    start.await();
                    return task.call();
                }));
            }
            return getResults(futures);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Returns the results of {@code futures} in order, waiting at most {@code TIMEOUT_SECONDS} for each.
     */
    private static <T> List<T> getResults(List<Future<T>> futures) throws Exception {
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            results.add(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }
        return results;
    }
}
