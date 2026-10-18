package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Saves a copy of all patient data to the data/backups folder.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Backup saved to: %1$s";
    public static final String MESSAGE_FAILURE = "Could not save the backup: %1$s. Your data has not been changed.";

    public static final Path DEFAULT_BACKUP_DIR = Paths.get("data", "backups");

    private static final String FILE_NAME_PREFIX = "mediconnect-";
    private static final String FILE_EXTENSION = ".json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

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
            throw new CommandException(String.format(MESSAGE_FAILURE, ioe.getMessage()), ioe);
        }

        logger.info("Saved backup to " + backupFile);
        return new CommandResult(String.format(MESSAGE_SUCCESS, backupFile.toAbsolutePath()));
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

        assert !Files.exists(backupFile) : "Backup file should not overwrite an existing file";
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
