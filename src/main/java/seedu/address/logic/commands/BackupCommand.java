package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
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
     * {@code RestoreCommand} also uses this, to back up the current data before replacing it.
     *
     * @throws IOException if the backup file cannot be written.
     */
    Path saveBackup(ReadOnlyAddressBook addressBook) throws IOException {
        requireNonNull(addressBook);
        Path backupFile = getBackupFilePath();

        try {
            new JsonAddressBookStorage(backupFile).saveAddressBook(addressBook);
        } catch (IOException ioe) {
            logger.warning("Could not save backup to " + backupFile + ": " + ioe);
            throw ioe;
        }

        logger.info("Saved backup to " + backupFile);
        return backupFile;
    }

    /**
     * Returns the path of the next backup file, named after the current time.
     * If a backup with that name already exists, a suffix such as "-1" is added so it is not overwritten.
     */
    private Path getBackupFilePath() {
        String baseName = FILE_NAME_PREFIX + LocalDateTime.now(clock).format(TIMESTAMP_FORMAT);
        Path backupFile = backupDir.resolve(baseName + FILE_EXTENSION);

        for (int suffix = 1; Files.exists(backupFile); suffix++) {
            backupFile = backupDir.resolve(baseName + SUFFIX_SEPARATOR + suffix + FILE_EXTENSION);
        }

        return backupFile;
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
