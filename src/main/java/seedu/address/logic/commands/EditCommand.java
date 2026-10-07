package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Placeholder for the edit command, which is outside the MVP scope.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";
    public static final String MESSAGE_NOT_IMPLEMENTED = "Edit command is not implemented yet.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(MESSAGE_NOT_IMPLEMENTED);
    }
}
