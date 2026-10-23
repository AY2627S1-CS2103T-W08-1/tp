package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Lists the backup files in the backup folder, newest first, so that their names can be given to {@code restore}.
 */
public class ListBackupsCommand extends Command {

    public static final String MESSAGE_SUCCESS = "Backups in %1$s, newest first:\n%2$s\nRestore one with: "
            + RestoreCommand.COMMAND_WORD + " FILENAME";
    public static final String MESSAGE_NO_BACKUPS = "No backups found in %1$s. Make one with "
            + BackupCommand.COMMAND_WORD + ".";
    public static final String MESSAGE_OLDER_BACKUPS = "... and %1$d older";
    public static final String MESSAGE_FAILURE = "Could not list the backups in %1$s.";

    /** Most backups listed, so that the list stays short enough to read in the result box. */
    public static final int MAX_LISTED_BACKUPS = 10;

    private static final String BACKUP_EXTENSION = ".json";

    private static final Logger logger = LogsCenter.getLogger(ListBackupsCommand.class);

    private final Path backupDir;

    /**
     * Creates a ListBackupsCommand that lists the backups in {@code BackupCommand.DEFAULT_BACKUP_DIR}.
     */
    public ListBackupsCommand() {
        this(BackupCommand.DEFAULT_BACKUP_DIR);
    }

    /**
     * Creates a ListBackupsCommand that lists the backups in {@code backupDir}.
     *
     * @param backupDir Folder that holds the backups.
     */
    public ListBackupsCommand(Path backupDir) {
        requireNonNull(backupDir);
        this.backupDir = backupDir;
    }

    /**
     * Lists the names of the backup files, newest first, or says that there are none.
     * The model is not changed.
     *
     * @param model {@code Model} of the app, which is not used.
     * @return a result listing the backup names and the full path of the backup folder.
     * @throws CommandException if the backup folder cannot be read.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path shownBackupDir = backupDir.toAbsolutePath().normalize();
        List<Path> backups = findBackupsNewestFirst(shownBackupDir);

        logger.info("Listed " + backups.size() + " backups in " + backupDir);
        if (backups.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_BACKUPS, shownBackupDir));
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, shownBackupDir, formatNames(backups)));
    }

    /**
     * Returns the backup files in the backup folder, newest first, or an empty list if the folder does not exist.
     *
     * @param shownBackupDir Full path of the backup folder, for the error message.
     * @throws CommandException if the folder or the times its files were last changed cannot be read.
     */
    private List<Path> findBackupsNewestFirst(Path shownBackupDir) throws CommandException {
        if (!Files.isDirectory(backupDir)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(backupDir)) {
            return sortNewestFirst(files.filter(ListBackupsCommand::isBackupFile).toList());
        } catch (IOException | UncheckedIOException e) {
            logger.warning("Could not list the backups in " + backupDir + ": " + e.getClass().getSimpleName());
            throw new CommandException(String.format(MESSAGE_FAILURE, shownBackupDir), e);
        }
    }

    /**
     * Returns true if {@code file} is a JSON file, which {@code restore} can restore if its data is valid.
     */
    private static boolean isBackupFile(Path file) {
        return Files.isRegularFile(file)
                && file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(BACKUP_EXTENSION);
    }

    /**
     * Returns {@code backups} sorted from the most recently changed to the least, and by name, last first, if two
     * were changed at the same time.
     *
     * @throws IOException if the time a file was last changed cannot be read.
     */
    private static List<Path> sortNewestFirst(List<Path> backups) throws IOException {
        Map<Path, FileTime> changedTimes = new HashMap<>();
        for (Path backup : backups) {
            changedTimes.put(backup, Files.getLastModifiedTime(backup));
        }

        Comparator<Path> newestFirst = Comparator.comparing((Path backup) -> changedTimes.get(backup)).reversed();
        Comparator<Path> lastNameFirst = Comparator.comparing((Path backup) -> backup.getFileName()).reversed();
        return backups.stream().sorted(newestFirst.thenComparing(lastNameFirst)).toList();
    }

    /**
     * Returns the names of the first {@code MAX_LISTED_BACKUPS} of {@code backups}, one per line, followed by how
     * many more there are, if any.
     */
    private static String formatNames(List<Path> backups) {
        String names = backups.stream()
                .limit(MAX_LISTED_BACKUPS)
                .map(backup -> backup.getFileName().toString())
                .collect(Collectors.joining("\n"));
        int olderCount = backups.size() - MAX_LISTED_BACKUPS;
        return olderCount > 0 ? names + "\n" + String.format(MESSAGE_OLDER_BACKUPS, olderCount) : names;
    }

    /**
     * Returns true if {@code other} is a {@code ListBackupsCommand} with the same backup folder.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ListBackupsCommand otherListBackupsCommand)) {
            return false;
        }

        return backupDir.equals(otherListBackupsCommand.backupDir);
    }

    /**
     * Returns a string showing the backup folder of this command.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("backupDir", backupDir)
                .toString();
    }
}
