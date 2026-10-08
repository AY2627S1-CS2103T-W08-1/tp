package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Shows the help window, which lists every command with its format and a one-line description.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String COMMAND_SUMMARY = String.join("\n",
            AddCommand.COMMAND_WORD + " n/NAME ic/NRIC dob/DOB p/PHONE a/ADDRESS : Adds a patient.",
            ListCommand.COMMAND_WORD + " : Lists all patients.",
            ViewCommand.COMMAND_WORD + " INDEX : Shows the full details of the patient at INDEX.",
            DeleteCommand.COMMAND_WORD + " INDEX : Deletes the patient at INDEX.",
            FindCommand.COMMAND_WORD + " KEYWORD [MORE_KEYWORDS] : Finds patients whose names contain "
                    + "any of the keywords.",
            ClearCommand.COMMAND_WORD + " : Deletes all patients.",
            COMMAND_WORD + " : Shows this list of commands.",
            ExitCommand.COMMAND_WORD + " : Exits the app.");

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
