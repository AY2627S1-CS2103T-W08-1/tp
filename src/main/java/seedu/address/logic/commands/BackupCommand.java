package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Saves a timestamped copy of all patient data to the backup folder.
 */
public class BackupCommand extends Command {

    public static final String COMMAND_WORD = "backup";

    public static final String MESSAGE_SUCCESS = "Backup saved to: %1$s";
    public static final String MESSAGE_FAILURE =
            "Could not save the backup to %1$s: %2$s. Your data has not been changed.";

    /** Folder that backups are saved to, relative to the folder MediConnect is launched from. */
    public static final Path DEFAULT_BACKUP_DIR = Paths.get("data", "backups");

    private static final String FILE_NAME_PREFIX = "mediconnect-";
    private static final String FILE_EXTENSION = ".json";
    private static final String SUFFIX_SEPARATOR = "-";
    // Locale.ROOT keeps file names the same whatever the computer's language settings are
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    private static final Logger logger = LogsCenter.getLogger(BackupCommand.class);

    private final Clock clock;
    private final Path backupDir;

    /**
     * Creates a BackupCommand that saves to {@code DEFAULT_BACKUP_DIR}, timestamped with the system clock.
     */
    public BackupCommand() {
        this(Clock.systemDefaultZone(), DEFAULT_BACKUP_DIR);
    }

    /**
     * Creates a BackupCommand that saves to {@code backupDir}, timestamped with {@code clock}.
     *
     * @param clock {@code Clock} that gives the date and time in the backup file name.
     * @param backupDir Folder to save backups to. It is created if it does not exist.
     */
    public BackupCommand(Clock clock, Path backupDir) {
        requireNonNull(clock);
        requireNonNull(backupDir);
        this.clock = clock;
        this.backupDir = backupDir;
    }

    /**
     * Saves all patient data in {@code model} to a new timestamped file in the backup folder.
     * The model is not changed, whether or not the backup succeeds.
     *
     * @param model {@code Model} whose patient data is backed up.
     * @return a result showing the full path of the backup file.
     * @throws CommandException if the backup file cannot be written.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Path backupFile = saveBackup(model.getAddressBook());
            return new CommandResult(String.format(MESSAGE_SUCCESS, backupFile.toAbsolutePath().normalize()));
        } catch (IOException ioe) {
            String reason = Messages.describeWriteFailure(ioe);
            throw new CommandException(
                    String.format(MESSAGE_FAILURE, backupDir.toAbsolutePath().normalize(), reason), ioe);
        }
    }

    /**
     * Saves {@code addressBook} to a new timestamped file in the backup folder and returns the file's path.
     * {@code RestoreCommand} and {@code ClearCommand} also use this before replacing or clearing the current data.
     * The data is written through {@code FileUtil#writeThroughTemporaryFile}, so a failed write never leaves a
     * cut-off backup, and on macOS and Linux, only the current user can open the backup.
     * No backup is overwritten, even by another backup made at the same time.
     *
     * @throws IOException if the backup folder or file cannot be written.
     */
    Path saveBackup(ReadOnlyAddressBook addressBook) throws IOException {
        requireNonNull(addressBook);
        try {
            Files.createDirectories(backupDir);
            Path backupFile = reserveBackupFile();
            writeBackup(backupFile, addressBook);
            logger.info("Saved backup to " + backupFile);
            return backupFile;
        } catch (IOException ioe) {
            logger.warning("Could not save a backup to " + backupDir + ": " + ioe.getClass().getSimpleName());
            throw ioe;
        }
    }

    /**
     * Creates an empty file under the next free backup name, named after the current time, and returns its
     * path. If a file with that name already exists, a suffix such as "-1" is added so that it is not
     * overwritten. Creating the file takes the name in one step, unlike checking first whether the name is
     * free, so that two backups made at the same time, e.g., in two MediConnect windows, never take the same
     * name.
     *
     * @throws IOException if the file cannot be created, e.g., because the folder is read-only.
     */
    private Path reserveBackupFile() throws IOException {
        String baseName = FILE_NAME_PREFIX + LocalDateTime.now(clock).format(TIMESTAMP_FORMAT);
        Path backupFile = backupDir.resolve(baseName + FILE_EXTENSION);

        for (int suffix = 1; ; suffix++) {
            try {
                return Files.createFile(backupFile);
            } catch (FileAlreadyExistsException faee) {
                // the name is taken, e.g., by a backup made in the same second, so the next one is tried
            } catch (AccessDeniedException ade) {
                // Windows also denies access when a folder has the name, so only a free name means that
                // MediConnect may not write to the backup folder
                if (!Files.exists(backupFile)) {
                    throw ade;
                }
            }
            backupFile = backupDir.resolve(baseName + SUFFIX_SEPARATOR + suffix + FILE_EXTENSION);
        }
    }

    /**
     * Writes {@code addressBook} to {@code backupFile}, the empty file reserved for it, through a temporary
     * file. If the write fails, {@code backupFile} is deleted, so that no empty backup is left behind.
     *
     * @throws IOException if the backup cannot be written.
     */
    private static void writeBackup(Path backupFile, ReadOnlyAddressBook addressBook) throws IOException {
        boolean isWritten = false;
        try {
            FileUtil.writeThroughTemporaryFile(backupFile,
                    temporaryFile -> new JsonAddressBookStorage(temporaryFile).saveAddressBook(addressBook));
            isWritten = true;
        } finally {
            if (!isWritten) {
                // the reserved file is left behind by the failed write, like the write's own temporary file
                FileUtil.deleteTemporaryFile(backupFile);
            }
        }
    }

    /**
     * Returns true if {@code other} is a {@code BackupCommand} with the same clock and backup folder.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof BackupCommand otherBackupCommand)) {
            return false;
        }

        return clock.equals(otherBackupCommand.clock)
                && backupDir.equals(otherBackupCommand.backupDir);
    }

    /**
     * Returns a string showing the clock and backup folder of this command.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("clock", clock)
                .add("backupDir", backupDir)
                .toString();
    }
}
