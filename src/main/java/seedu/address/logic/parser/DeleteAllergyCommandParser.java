package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteAllergyCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the patient index and allergy index of a deletion.
 */
public class DeleteAllergyCommandParser implements Parser<DeleteAllergyCommand> {

    @Override
    public DeleteAllergyCommand parse(String args) throws ParseException {
        String[] indices = args.strip().split("\\s+");
        try {
            if (indices.length != 2) {
                throw new ParseException(ParserUtil.MESSAGE_INVALID_INDEX);
            }
            Index patientIndex = ParserUtil.parseIndex(indices[0]);
            Index allergyIndex = ParserUtil.parseIndex(indices[1]);
            return new DeleteAllergyCommand(patientIndex, allergyIndex);
        } catch (ParseException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAllergyCommand.MESSAGE_USAGE), e);
        }
    }
}
