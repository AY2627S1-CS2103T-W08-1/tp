package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
            + ": Permanently deletes all active and archived patients and their care information.\n"
            + "Run backup first if you may need to recover the data.\n"
            + "Format: " + COMMAND_FORMAT;
    public static final String MESSAGE_SUCCESS = "All patients have been cleared.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
