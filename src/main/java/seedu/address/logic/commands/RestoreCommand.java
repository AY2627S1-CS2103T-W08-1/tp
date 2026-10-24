package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PATIENTS;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Replaces all patient data with the data in a backup file, after backing up the current data.
 */
public class RestoreCommand extends Command {

    public static final String COMMAND_WORD = "restore";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Replaces all patient data with a backup from the data/backups folder, "
            + "after backing up the current data.\n"
            + "Parameters: FILENAME (the name of a backup file in data/backups)\n"
            + "Example: " + COMMAND_WORD + " mediconnect-20261020-083015.json";

    public static final String MESSAGE_INVALID_FILE_NAME = "Give only the name of a backup file in data/backups, "
            + "not a path, e.g., " + COMMAND_WORD + " mediconnect-20261020-083015.json";

    public static final String MESSAGE_SUCCESS =
            "Restored %1$d %2$s from %3$s. Your previous data was backed up to: %4$s";
    public static final String MESSAGE_NOT_FOUND =
            "No backup named %1$s was found in %2$s. Your data has not been changed.";
    public static final String MESSAGE_NO_READ_PERMISSION =
            "MediConnect does not have permission to read %1$s. Your data has not been changed.";
    public static final String MESSAGE_UNREADABLE = "The backup %1$s could not be read. It may be damaged, "
            + "or not a MediConnect data file. Your data has not been changed.";
    public static final String MESSAGE_INVALID_DATA = "The backup %1$s has invalid data, so it was not restored. "
            + "Your data has not been changed.\nDetails: %2$s";
    public static final String MESSAGE_BACKUP_FAILED = "Could not back up the current data to %1$s: %2$s. "
            + "Nothing was restored, and your data has not been changed.";

    private static final Logger logger = LogsCenter.getLogger(RestoreCommand.class);

    private final String fileName;
    private final Clock clock;
    private final Path backupDir;

    /**
     * Creates a RestoreCommand that restores {@code fileName} from {@code BackupCommand.DEFAULT_BACKUP_DIR},
     * after backing up the current data there, timestamped with the system clock.
     *
     * @param fileName Name of the backup file, without a folder.
     */
    public RestoreCommand(String fileName) {
        this(fileName, Clock.systemDefaultZone(), BackupCommand.DEFAULT_BACKUP_DIR);
    }

    /**
     * Creates a RestoreCommand that restores {@code fileName} from {@code backupDir},
     * after backing up the current data there, timestamped with {@code clock}.
     *
     * @param fileName Name of the backup file, without a folder.
     * @param clock {@code Clock} that gives the date and time in the name of the automatic backup.
     * @param backupDir Folder that holds the backup file, and receives the automatic backup.
     */
    public RestoreCommand(String fileName, Clock clock, Path backupDir) {
        requireNonNull(fileName);
        requireNonNull(clock);
        requireNonNull(backupDir);
        assert Paths.get(fileName).getFileName().toString().equals(fileName)
                : "the parser accepts only a file name, so the backup cannot be outside the backup folder";
        this.fileName = fileName;
        this.clock = clock;
        this.backupDir = backupDir;
    }

    /**
     * Replaces the patient data in {@code model} with the data in the backup file.
     * The whole file is checked and the current data is backed up first, and if either step fails,
     * the model is not changed.
     *
     * @param model {@code Model} whose patient data is replaced.
     * @return a result showing how many patients were restored and where the current data was backed up.
     * @throws CommandException if the backup file is missing, unreadable or invalid,
     *     or the current data cannot be backed up.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ReadOnlyAddressBook restoredData = readBackup();
        Path automaticBackupFile = backUpCurrentData(model);

        model.setAddressBook(restoredData);
        model.updateFilteredPatientList(PREDICATE_SHOW_ACTIVE_PATIENTS);

        logger.info("Restored the backup " + backupDir.resolve(fileName));
        return createSuccessResult(restoredData, automaticBackupFile);
    }

    /**
     * Reads the backup file and checks every patient in it.
     *
     * @throws CommandException if the file is missing or unreadable, or has invalid data.
     */
    private ReadOnlyAddressBook readBackup() throws CommandException {
        Path backupFile = backupDir.resolve(fileName);
        String notFoundMessage = String.format(MESSAGE_NOT_FOUND, fileName, backupDir.toAbsolutePath().normalize());
        if (!Files.isRegularFile(backupFile)) {
            throw new CommandException(notFoundMessage);
        }

        try {
            // the file can still disappear after the check above, which readAddressBook reports as empty
            return new JsonAddressBookStorage(backupFile).readAddressBook()
                    .orElseThrow(() -> new CommandException(notFoundMessage));
        } catch (DataLoadingException dle) {
            String reason = describeLoadingFailure(dle);
            logger.warning("Could not restore " + backupFile + ": " + reason);
            throw new CommandException(reason, dle);
        }
    }

    /**
     * Saves the current patient data in {@code model} to a new backup file, and returns its path.
     *
     * @throws CommandException if the backup file cannot be written.
     */
    private Path backUpCurrentData(Model model) throws CommandException {
        try {
            return new BackupCommand(clock, backupDir).saveBackup(model.getAddressBook());
        } catch (IOException ioe) {
            String reason = Messages.describeWriteFailure(ioe);
            throw new CommandException(
                    String.format(MESSAGE_BACKUP_FAILED, backupDir.toAbsolutePath().normalize(), reason), ioe);
        }
    }

    /**
     * Returns the result to show after restoring {@code restoredData}: how many patients were restored, and the full
     * path of {@code automaticBackupFile}, which holds the data from before the restore.
     */
    private CommandResult createSuccessResult(ReadOnlyAddressBook restoredData, Path automaticBackupFile) {
        int patientCount = restoredData.getPatientList().size();
        String patientWord = patientCount == 1 ? "patient" : "patients";
        return new CommandResult(String.format(MESSAGE_SUCCESS, patientCount, patientWord, fileName,
                automaticBackupFile.toAbsolutePath().normalize()));
    }

    /**
     * Returns the message to show when the backup file could not be loaded because of {@code dle}.
     */
    private String describeLoadingFailure(DataLoadingException dle) {
        Throwable cause = dle.getCause();
        if (cause instanceof IllegalValueException) {
            return String.format(MESSAGE_INVALID_DATA, fileName, cause.getMessage());
        }
        if (cause instanceof AccessDeniedException) {
            return String.format(MESSAGE_NO_READ_PERMISSION, fileName);
        }
        return String.format(MESSAGE_UNREADABLE, fileName);
    }

    /**
     * Returns true if {@code other} is a {@code RestoreCommand} with the same file name, clock and backup folder.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RestoreCommand otherRestoreCommand)) {
            return false;
        }

        return fileName.equals(otherRestoreCommand.fileName)
                && clock.equals(otherRestoreCommand.clock)
                && backupDir.equals(otherRestoreCommand.backupDir);
    }

    /**
     * Returns a string showing the file name, clock and backup folder of this command.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("fileName", fileName)
                .add("clock", clock)
                .add("backupDir", backupDir)
                .toString();
    }
}
