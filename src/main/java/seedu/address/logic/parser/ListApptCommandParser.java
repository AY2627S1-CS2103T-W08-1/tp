package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ListApptCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ListApptCommand} object.
 */
public class ListApptCommandParser implements Parser<ListApptCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ListApptCommand
     * and returns a ListApptCommand object for execution.
     * No arguments lists the upcoming appointments of all patients.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public ListApptCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new ListApptCommand(Optional.empty());
        }

        try {
            Index index = ParserUtil.parseIndex(args);
            return new ListApptCommand(Optional.of(index));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListApptCommand.MESSAGE_USAGE), pe);
        }
    }
}
