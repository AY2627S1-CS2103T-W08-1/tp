package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether successful execution requires saving even if the patient data is unchanged.
     * Explicit data replacement commands override this so they can repair an unreadable data file.
     * Other commands are saved only when they change patient data.
     */
    public boolean requiresSaveWhenUnchanged() {
        return false;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return a non-null result containing feedback for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
