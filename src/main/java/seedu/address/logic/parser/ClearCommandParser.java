package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Requires explicit confirmation before creating a command that clears all patients.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    @Override
    public ClearCommand parse(String args) throws ParseException {
        if (!ClearCommand.CONFIRMATION_WORD.equals(args.strip())) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
        }
        return new ClearCommand();
    }
}
