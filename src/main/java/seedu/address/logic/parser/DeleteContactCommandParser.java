package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the patient index and contact index of a deletion.
 */
public class DeleteContactCommandParser implements Parser<DeleteContactCommand> {

    @Override
    public DeleteContactCommand parse(String args) throws ParseException {
        try {
            List<Index> indices = ParserUtil.parseTwoIndexes(args);
            return new DeleteContactCommand(indices.get(0), indices.get(1));
        } catch (ParseException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteContactCommand.MESSAGE_USAGE), e);
        }
    }
}
