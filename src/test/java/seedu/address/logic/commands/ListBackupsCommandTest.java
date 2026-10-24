package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ListBackupsCommandTest {

    private static final Instant EARLIER = Instant.parse("2026-10-20T08:30:15Z");
    private static final Instant LATER = Instant.parse("2026-10-21T09:00:00Z");
    /** First number in the names of numbered backups, so that every number up to 99 has two digits. */
    private static final int TWO_DIGIT_START = 10;

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullBackupDir_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListBackupsCommand(null));
    }

    @Test
    public void constructor_noArguments_usesDataBackupsFolder() {
        assertEquals(new ListBackupsCommand(BackupCommand.DEFAULT_BACKUP_DIR), new ListBackupsCommand());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListBackupsCommand(temporaryFolder).execute(null));
    }

    @Test
    public void execute_missingBackupFolder_saysNoBackups() {
        Path missingFolder = temporaryFolder.resolve("backups");

        assertCommandSuccess(new ListBackupsCommand(missingFolder), model,
                String.format(ListBackupsCommand.MESSAGE_NO_BACKUPS, missingFolder), expectedModel);
        assertFalse(Files.exists(missingFolder));
    }

    @Test
    public void execute_onlyOtherFilesInFolder_saysNoBackups() throws Exception {
        // temporary files of an unfinished backup, other files and folders are not backups
        Files.createFile(temporaryFolder.resolve("mediconnect-20261020-083015.json.123.tmp"));
        Files.createFile(temporaryFolder.resolve("notes.txt"));
        Files.createDirectory(temporaryFolder.resolve("old.json"));

        assertCommandSuccess(new ListBackupsCommand(temporaryFolder), model,
                String.format(ListBackupsCommand.MESSAGE_NO_BACKUPS, temporaryFolder), expectedModel);
    }

    @Test
    public void execute_backupsChangedAtDifferentTimes_listsNewestFirst() throws Exception {
        createBackup("mediconnect-20261021-090000.json", LATER);
        createBackup("older.JSON", EARLIER);

        assertCommandSuccess(new ListBackupsCommand(temporaryFolder), model,
                getExpectedSuccessMessage("mediconnect-20261021-090000.json\nolder.JSON"), expectedModel);
    }

    @Test
    public void execute_backupsChangedAtSameTime_listsLastNameFirstComparingNumbersByValue() throws Exception {
        // e.g., backups copied together to another computer, which all get the time of the copy
        createBackup("mediconnect-20261020-083015.json", EARLIER);
        createBackup("mediconnect-20261020-083015-1.json", EARLIER);
        createBackup("mediconnect-20261020-083015-2.json", EARLIER);
        createBackup("mediconnect-20261020-083015-9.json", EARLIER);
        createBackup("mediconnect-20261020-083015-10.json", EARLIER);
        createBackup("mediconnect-20261020-083015-copy.json", EARLIER);
        createBackup("notes.json", EARLIER);

        String expectedNames = String.join("\n", "notes.json", "mediconnect-20261020-083015-copy.json",
                "mediconnect-20261020-083015-10.json", "mediconnect-20261020-083015-9.json",
                "mediconnect-20261020-083015-2.json", "mediconnect-20261020-083015-1.json",
                "mediconnect-20261020-083015.json");
        assertCommandSuccess(new ListBackupsCommand(temporaryFolder), model,
                getExpectedSuccessMessage(expectedNames), expectedModel);
    }

    @Test
    public void execute_exactlyMaxListedBackups_listsAllWithoutOlderCount() throws Exception {
        // Boundary value analysis: MAX_LISTED_BACKUPS is the largest count that is listed in full
        List<String> newestNames = createBackupsNewestFirst(ListBackupsCommand.MAX_LISTED_BACKUPS);

        assertCommandSuccess(new ListBackupsCommand(temporaryFolder), model,
                getExpectedSuccessMessage(String.join("\n", newestNames)), expectedModel);
    }

    @Test
    public void execute_oneMoreThanMaxListedBackups_listsNewestAndCountsOneOlder() throws Exception {
        // Boundary value analysis: MAX_LISTED_BACKUPS + 1 is the smallest count that is cut short
        assertListsNewestAndCountsOlder(ListBackupsCommand.MAX_LISTED_BACKUPS + 1);
    }

    @Test
    public void execute_moreBackupsThanListed_listsNewestAndCountsOlder() throws Exception {
        // Equivalence partitioning: a count well inside the partition of counts that are cut short
        assertListsNewestAndCountsOlder(ListBackupsCommand.MAX_LISTED_BACKUPS + 2);
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flags used here")
    public void execute_unreadableBackupFolder_throwsCommandException() throws Exception {
        createBackup("mediconnect-20261020-083015.json", EARLIER);
        assertTrue(temporaryFolder.toFile().setReadable(false));
        try {
            // users with admin rights (e.g., root) can still read, so the test only applies to others
            assumeFalse(Files.isReadable(temporaryFolder));

            assertCommandFailure(new ListBackupsCommand(temporaryFolder), model,
                    String.format(ListBackupsCommand.MESSAGE_FAILURE, temporaryFolder));
        } finally {
            temporaryFolder.toFile().setReadable(true);
        }
    }

    @Test
    public void equals() {
        ListBackupsCommand listBackupsCommand = new ListBackupsCommand(temporaryFolder);

        // same object -> returns true
        assertTrue(listBackupsCommand.equals(listBackupsCommand));

        // same backup folder -> returns true
        assertTrue(listBackupsCommand.equals(new ListBackupsCommand(temporaryFolder)));

        // different types -> returns false
        assertFalse(listBackupsCommand.equals(1));

        // null -> returns false
        assertFalse(listBackupsCommand.equals(null));

        // different backup folder -> returns false
        assertFalse(listBackupsCommand.equals(new ListBackupsCommand(temporaryFolder.resolve("other"))));
    }

    @Test
    public void toStringMethod() {
        String expected = ListBackupsCommand.class.getCanonicalName() + "{backupDir=" + temporaryFolder + "}";
        assertEquals(expected, new ListBackupsCommand(temporaryFolder).toString());
    }

    /**
     * Creates an empty backup file named {@code name} in the temporary folder, last changed at {@code changedAt}.
     */
    private void createBackup(String name, Instant changedAt) throws Exception {
        Path backup = Files.createFile(temporaryFolder.resolve(name));
        Files.setLastModifiedTime(backup, FileTime.from(changedAt));
    }

    /**
     * Creates {@code count} backups, each changed a second after the one before, and returns their names, newest
     * first. Every number in the names has two digits, so the names also sort in the order the backups were made.
     */
    private List<String> createBackupsNewestFirst(int count) throws Exception {
        List<String> newestNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = "backup-" + (TWO_DIGIT_START + i) + ".json";
            createBackup(name, EARLIER.plusSeconds(i));
            newestNames.add(0, name);
        }
        return newestNames;
    }

    /**
     * Creates {@code backupCount} backups, which is more than {@code MAX_LISTED_BACKUPS}, and confirms that only the
     * newest {@code MAX_LISTED_BACKUPS} are listed, followed by how many older ones there are.
     */
    private void assertListsNewestAndCountsOlder(int backupCount) throws Exception {
        List<String> newestNames = createBackupsNewestFirst(backupCount);
        int olderCount = backupCount - ListBackupsCommand.MAX_LISTED_BACKUPS;

        String expectedNames = String.join("\n", newestNames.subList(0, ListBackupsCommand.MAX_LISTED_BACKUPS))
                + "\n" + String.format(ListBackupsCommand.MESSAGE_OLDER_BACKUPS, olderCount);
        assertCommandSuccess(new ListBackupsCommand(temporaryFolder), model,
                getExpectedSuccessMessage(expectedNames), expectedModel);
    }

    private String getExpectedSuccessMessage(String names) {
        return String.format(ListBackupsCommand.MESSAGE_SUCCESS, temporaryFolder, names);
    }
}
