package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.CancelApptCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code CancelApptCommand} object.
 */
public class CancelApptCommandParser implements Parser<CancelApptCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the CancelApptCommand
     * and returns a CancelApptCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public CancelApptCommand parse(String args) throws ParseException {
        try {
            List<Index> indexes = ParserUtil.parseTwoIndexes(args);
            return new CancelApptCommand(indexes.get(0), indexes.get(1));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, CancelApptCommand.MESSAGE_USAGE), pe);
        }
    }
}
