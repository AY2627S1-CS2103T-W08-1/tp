package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.AddApptCommand;
import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddContactCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.CancelApptCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteAllergyCommand;
import seedu.address.logic.commands.DeleteCareCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteContactCommand;
import seedu.address.logic.commands.DeleteMedCommand;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.commands.EditApptCommand;
import seedu.address.logic.commands.EditCareCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditContactCommand;
import seedu.address.logic.commands.EditMedCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListApptCommand;
import seedu.address.logic.commands.ListArchivedCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("\\h");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Returns {@code userInput} with every horizontal whitespace character, such as a tab or a no-break space
     * copied from another app, replaced by a normal space, and without whitespace at both ends, so that the
     * parsers only see normal spaces.
     */
    private static String normalizeWhitespace(String userInput) {
        return StringUtil.stripUnicodeWhitespace(HORIZONTAL_WHITESPACE.matcher(userInput).replaceAll(" "));
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(normalizeWhitespace(userInput));
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord").toLowerCase(Locale.ROOT);
        final String arguments = matcher.group("arguments");

        return switch (commandWord) {
            case AddContactCommand.COMMAND_WORD -> new AddContactCommandParser().parse(arguments);
            case EditContactCommand.COMMAND_WORD -> new EditContactCommandParser().parse(arguments);
            case DeleteContactCommand.COMMAND_WORD -> new DeleteContactCommandParser().parse(arguments);
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case AddCareCommand.COMMAND_WORD -> new AddCareCommandParser().parse(arguments);
            case AddApptCommand.COMMAND_WORD -> new AddApptCommandParser().parse(arguments);
            case EditApptCommand.COMMAND_WORD -> new EditApptCommandParser().parse(arguments);
            case CancelApptCommand.COMMAND_WORD -> new CancelApptCommandParser().parse(arguments);
            case ListApptCommand.COMMAND_WORD -> new ListApptCommandParser().parse(arguments);
            case ArchiveCommand.COMMAND_WORD -> new ArchiveCommandParser().parse(arguments);
            case EditCareCommand.COMMAND_WORD -> new EditCareCommandParser().parse(arguments);
            case DeleteCareCommand.COMMAND_WORD -> new DeleteCareCommandParser().parse(arguments);
            case DeleteAllergyCommand.COMMAND_WORD -> new DeleteAllergyCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case DueCommand.COMMAND_WORD -> new DueCommandParser().parse(arguments);
            case DeleteMedCommand.COMMAND_WORD -> new DeleteMedCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case EditMedCommand.COMMAND_WORD -> new EditMedCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case BackupCommand.COMMAND_WORD -> new BackupCommand();
            case RestoreCommand.COMMAND_WORD -> new RestoreCommandParser().parse(arguments);
            case ExportCommand.COMMAND_WORD -> new ExportCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ListArchivedCommand.COMMAND_WORD -> new ListArchivedCommand();
            case UnarchiveCommand.COMMAND_WORD -> new UnarchiveCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                // The word is not logged, as a caregiver may have typed a patient's NRIC or name instead
                logger.finer("Unknown command word");
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
