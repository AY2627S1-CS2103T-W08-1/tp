package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
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
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Saves a timestamped copy of all patient data to the backup folder.
 */
public class BackupCommand extends Command {

    public static final String COMMAND_WORD = "backup";

    public static final String MESSAGE_SUCCESS = "Backup saved to: %1$s";
    public static final String MESSAGE_FAILURE =
            "Could not save the backup to %1$s: %2$s. Your data has not been changed.";

    /** Reason shown in {@code MESSAGE_FAILURE} when MediConnect is not allowed to write to the backup folder. */
    public static final String MESSAGE_NO_PERMISSION = "MediConnect does not have permission to write there";

    /** Reason shown in {@code MESSAGE_FAILURE} when the file system does not give a reason of its own. */
    public static final String MESSAGE_FILE_SYSTEM_ERROR = "the folder or file could not be created";

    /** Folder that backups are saved to, relative to the folder MediConnect is launched from. */
    public static final Path DEFAULT_BACKUP_DIR = Paths.get("data", "backups");

    private static final String FILE_NAME_PREFIX = "mediconnect-";
    private static final String FILE_EXTENSION = ".json";
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
        Path backupFile = getBackupFilePath();

        try {
            new JsonAddressBookStorage(backupFile).saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            logger.warning("Could not save backup to " + backupFile + ": " + ioe);
            throw new CommandException(
                    String.format(MESSAGE_FAILURE, backupDir.toAbsolutePath().normalize(), describeFailure(ioe)), ioe);
        }

        logger.info("Saved backup to " + backupFile);
        return new CommandResult(String.format(MESSAGE_SUCCESS, backupFile.toAbsolutePath().normalize()));
    }

    /**
     * Returns the path of the next backup file, named after the current time.
     * If a backup with that name already exists, a suffix such as "-1" is added so it is not overwritten.
     */
    private Path getBackupFilePath() {
        String baseName = FILE_NAME_PREFIX + LocalDateTime.now(clock).format(TIMESTAMP_FORMAT);
        Path backupFile = backupDir.resolve(baseName + FILE_EXTENSION);

        for (int suffix = 1; Files.exists(backupFile); suffix++) {
            backupFile = backupDir.resolve(baseName + "-" + suffix + FILE_EXTENSION);
        }

        return backupFile;
    }

    /**
     * Returns a short, readable reason for {@code ioe}, suitable for showing to the user.
     * File system exceptions use the file path as their message, so their reason is used instead.
     * Trailing full stops are removed, as {@code MESSAGE_FAILURE} adds its own after the reason.
     * Package-private so that tests can cover failures that are hard to cause on a real file system.
     */
    static String describeFailure(IOException ioe) {
        requireNonNull(ioe);
        if (ioe instanceof AccessDeniedException) {
            return MESSAGE_NO_PERMISSION;
        }

        String reason;
        String fallback;
        if (ioe instanceof FileSystemException fileSystemException) {
            reason = fileSystemException.getReason();
            fallback = MESSAGE_FILE_SYSTEM_ERROR;
        } else {
            reason = ioe.getMessage();
            fallback = ioe.getClass().getSimpleName();
        }

        String trimmedReason = reason == null ? "" : reason.replaceAll("[\\s.]+$", "").strip();
        return trimmedReason.isEmpty() ? fallback : trimmedReason;
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
