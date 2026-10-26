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

    /** Separates the groups of commands in {@code COMMAND_SUMMARY}, each of which starts with its heading. */
    public static final String GROUP_SEPARATOR = "\n\n";

    /**
     * Lists every command under the heading of its group, one per line, as its format followed by a one-line
     * description, with a blank line between groups.
     * Formats are built from the same prefixes the parsers use, so they stay in sync with the accepted input.
     */
    public static final String COMMAND_SUMMARY = String.join(GROUP_SEPARATOR,
            formatGroup("Patients",
                    formatEntry(AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_NRIC + "NRIC "
                            + PREFIX_DOB + "DOB " + PREFIX_PHONE + "PHONE " + PREFIX_ADDRESS + "ADDRESS",
                            "Adds a patient."),
                    formatEntry(EditCommand.COMMAND_FORMAT,
                            "Edits the patient at INDEX (at least one field)."),
                    formatEntry(ListCommand.COMMAND_WORD,
                            "Lists all active patients."),
                    formatEntry(FindCommand.COMMAND_WORD + " KEYWORD [MORE_KEYWORDS]...",
                            "Finds active patients whose names contain any of the keywords (case-insensitive)."),
                    formatEntry(ViewCommand.COMMAND_WORD + " INDEX",
                            "Shows the full details of the patient at INDEX."),
                    formatEntry(ArchiveCommand.COMMAND_FORMAT,
                            "Archives the patient at INDEX."),
                    formatEntry(ListArchivedCommand.COMMAND_WORD,
                            "Lists all archived patients and their indices for unarchive."),
                    formatEntry(UnarchiveCommand.COMMAND_FORMAT,
                            "Restores the archived patient at INDEX to the active list."),
                    formatEntry(DeleteCommand.COMMAND_WORD + " INDEX",
                            "Deletes the patient at INDEX.")),
            formatGroup("Care information",
                    formatEntry(AddCareCommand.COMMAND_WORD + " INDEX [" + PREFIX_MEDICATION + "MEDICATION]... ["
                            + PREFIX_ALLERGEN + "ALLERGEN]... [" + PREFIX_CARE_INSTRUCTION + "CARE_INSTRUCTION]...",
                            "Adds care information (at least one entry) to the patient at INDEX."),
                    formatEntry(EditCareCommand.COMMAND_FORMAT,
                            "Edits a care instruction of the patient at INDEX."),
                    formatEntry(DeleteCareCommand.COMMAND_FORMAT,
                            "Deletes a care instruction of the patient at INDEX."),
                    formatEntry(DeleteAllergyCommand.COMMAND_FORMAT,
                            "Deletes an allergen of the patient at INDEX."),
                    formatEntry(EditMedCommand.COMMAND_WORD + " INDEX MED_INDEX [" + PREFIX_MEDICATION
                            + "DESCRIPTION] [" + PREFIX_TIME + "TIME]...",
                            "Edits the description and/or daily times of the medication at MED_INDEX "
                                    + "of the patient at INDEX."),
                    formatEntry(DeleteMedCommand.COMMAND_WORD + " INDEX MED_INDEX",
                            "Deletes the medication at MED_INDEX of the patient at INDEX."),
                    formatEntry(DueCommand.COMMAND_WORD + " [INDEX]",
                            "Lists today's medication times of all active patients, or of the patient at INDEX.")),
            formatGroup("Contacts",
                    formatEntry(AddContactCommand.COMMAND_FORMAT,
                            "Adds a contact to the patient at INDEX."),
                    formatEntry(EditContactCommand.COMMAND_FORMAT,
                            "Edits a contact of the patient at INDEX."),
                    formatEntry(DeleteContactCommand.COMMAND_FORMAT,
                            "Deletes a contact of the patient at INDEX.")),
            formatGroup("Appointments",
                    formatEntry(AddApptCommand.COMMAND_WORD + " INDEX " + PREFIX_DATE + "DATE "
                            + PREFIX_TIME + "TIME " + PREFIX_WITH + "WITH [" + PREFIX_NOTE + "NOTE]",
                            "Adds an appointment to the patient at INDEX."),
                    formatEntry(EditApptCommand.COMMAND_WORD + " INDEX APPT_INDEX [" + PREFIX_DATE + "DATE] ["
                            + PREFIX_TIME + "TIME] [" + PREFIX_WITH + "WITH] [" + PREFIX_NOTE + "NOTE]",
                            "Edits appointment APPT_INDEX of the patient at INDEX (at least one field)."),
                    formatEntry(CancelApptCommand.COMMAND_WORD + " INDEX APPT_INDEX",
                            "Cancels appointment APPT_INDEX of the patient at INDEX."),
                    formatEntry(ListApptCommand.COMMAND_WORD + " [INDEX]",
                            "Lists all upcoming appointments, or every appointment of the active patient at INDEX.")),
            formatGroup("Data",
                    formatEntry(BackupCommand.COMMAND_WORD,
                            "Saves a copy of all patient data to data/backups."),
                    formatEntry(RestoreCommand.COMMAND_WORD + " [FILENAME]",
                            "Replaces all patient data with the backup FILENAME from data/backups, "
                                    + "after backing up the current data. Without FILENAME, lists the backups."),
                    formatEntry(ExportCommand.COMMAND_WORD + " INDEX",
                            "Saves the full record of the patient at INDEX to data/exports, "
                                    + "in a file named after their NRIC."),
                    formatEntry(ClearCommand.COMMAND_WORD,
                            "Deletes all patients.")),
            formatGroup("Other",
                    formatEntry(COMMAND_WORD,
                            "Shows this list of commands."),
                    formatEntry(ExitCommand.COMMAND_WORD,
                            "Exits the app.")));

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }

    /**
     * Returns a group of {@code COMMAND_SUMMARY}: {@code heading}, then {@code entries}, one per line.
     *
     * @param heading Name of the group, e.g., "Patients".
     * @param entries Lines made by {@code formatEntry}, in the order to list them.
     * @return the heading and entries, each on its own line.
     */
    private static String formatGroup(String heading, String... entries) {
        return heading + "\n" + String.join("\n", entries);
    }

    /**
     * Returns a line of {@code COMMAND_SUMMARY} that pairs a command's format with its description.
     *
     * @param format Format of the command, starting with its command word.
     * @param description One-sentence description of what the command does.
     * @return the format and description joined by {@code FORMAT_DESCRIPTION_SEPARATOR}.
     */
    private static String formatEntry(String format, String description) {
        return format + FORMAT_DESCRIPTION_SEPARATOR + description;
    }
}
