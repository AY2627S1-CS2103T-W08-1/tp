package seedu.address.logic.commands;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WITH;

import seedu.address.model.Model;

/**
 * Shows the help window, which lists every command with its format and a one-line description.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    /** Separates the format of a command from its description in {@code COMMAND_SUMMARY}. */
    public static final String FORMAT_DESCRIPTION_SEPARATOR = " : ";

    /**
     * Lists every command, one per line, as its format followed by a one-line description.
     * Formats are built from the same prefixes the parsers use, so they stay in sync with the accepted input.
     */
    public static final String COMMAND_SUMMARY = String.join("\n",
            formatEntry(AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_NRIC + "NRIC "
                    + PREFIX_DOB + "DOB " + PREFIX_PHONE + "PHONE " + PREFIX_ADDRESS + "ADDRESS",
                    "Adds a patient."),
            formatEntry(ListCommand.COMMAND_WORD,
                    "Lists all patients."),
            formatEntry(DeleteCommand.COMMAND_WORD + " INDEX",
                    "Deletes the patient at INDEX."),
            formatEntry(AddCareCommand.COMMAND_WORD + " INDEX [" + PREFIX_MEDICATION + "MEDICATION]... ["
                    + PREFIX_ALLERGEN + "ALLERGEN]... [" + PREFIX_CARE_INSTRUCTION + "CARE_INSTRUCTION]...",
                    "Adds care information (at least one entry) to the patient at INDEX."),
            formatEntry(AddApptCommand.COMMAND_WORD + " INDEX " + PREFIX_DATE + "DATE "
                    + PREFIX_TIME + "TIME " + PREFIX_WITH + "WITH [" + PREFIX_NOTE + "NOTE]",
                    "Adds an appointment to the patient at INDEX."),
            formatEntry(EditApptCommand.COMMAND_WORD + " INDEX APPT_INDEX [" + PREFIX_DATE + "DATE] ["
                    + PREFIX_TIME + "TIME] [" + PREFIX_WITH + "WITH] [" + PREFIX_NOTE + "NOTE]",
                    "Edits appointment APPT_INDEX of the patient at INDEX (at least one field)."),
            formatEntry(CancelApptCommand.COMMAND_WORD + " INDEX APPT_INDEX",
                    "Cancels appointment APPT_INDEX of the patient at INDEX."),
            formatEntry(ViewCommand.COMMAND_WORD + " INDEX",
                    "Shows the full details of the patient at INDEX."),
            formatEntry(FindCommand.COMMAND_WORD + " KEYWORD [MORE_KEYWORDS]...",
                    "Finds patients whose names contain any of the keywords (case-insensitive)."),
            formatEntry(COMMAND_WORD,
                    "Shows this list of commands."),
            formatEntry(ClearCommand.COMMAND_WORD,
                    "Deletes all patients."),
            formatEntry(ExitCommand.COMMAND_WORD,
                    "Exits the app."));

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }

    /**
     * Returns a line of {@code COMMAND_SUMMARY} that pairs a command's format with its description.
     *
     * @param format Format of the command, starting with its command word.
     * @param description One-sentence description of what the command does.
     * @return The format and description joined by {@code FORMAT_DESCRIPTION_SEPARATOR}.
     */
    private static String formatEntry(String format, String description) {
        return format + FORMAT_DESCRIPTION_SEPARATOR + description;
    }
}
