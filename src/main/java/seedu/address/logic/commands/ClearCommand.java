package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.time.Clock;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears all active and archived patients after confirmation has been parsed.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRMATION_WORD = "confirm";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " " + CONFIRMATION_WORD;
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Backs up and clears all active and archived patients and their care information.\n"
            + "If the backup fails, no patients are cleared.\n"
            + "Format: " + COMMAND_FORMAT;
    public static final String MESSAGE_SUCCESS = "All patients have been cleared.";

    private final BackupCommand backupCommand;

    /**
     * Creates a command that backs up the current data to the default backup folder before clearing it.
     */
    public ClearCommand() {
        this(Clock.systemDefaultZone(), BackupCommand.DEFAULT_BACKUP_DIR);
    }

    /**
     * Creates a command that backs up the current data to {@code backupDir}, timestamped with {@code clock}.
     */
    public ClearCommand(Clock clock, Path backupDir) {
        backupCommand = new BackupCommand(clock, backupDir);
    }

    /**
     * Backs up all patient data before clearing it, and returns the backup location with the success message.
     *
     * @throws CommandException if the backup cannot be saved, leaving all patient data unchanged.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        CommandResult backupResult = backupCommand.execute(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS + "\n" + backupResult.getFeedbackToUser());
    }
}
