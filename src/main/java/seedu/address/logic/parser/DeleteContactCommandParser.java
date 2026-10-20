package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the patient index and contact index of a deletion.
 */
public class DeleteContactCommandParser implements Parser<DeleteContactCommand> {

    @Override
    public DeleteContactCommand parse(String args) throws ParseException {
        String[] indices = args.strip().split("\\s+");
        try {
            if (indices.length != 2) {
                throw new ParseException(ParserUtil.MESSAGE_INVALID_INDEX);
            }
            Index patientIndex = ParserUtil.parseIndex(indices[0]);
            Index contactIndex = ParserUtil.parseIndex(indices[1]);
            return new DeleteContactCommand(patientIndex, contactIndex);
        } catch (ParseException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteContactCommand.MESSAGE_USAGE), e);
        }
    }
}
