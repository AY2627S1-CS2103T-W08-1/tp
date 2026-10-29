package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteAllergyCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the patient index and allergy index of a deletion.
 */
public class DeleteAllergyCommandParser implements Parser<DeleteAllergyCommand> {

    @Override
    public DeleteAllergyCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            List<Index> indices = ParserUtil.parseTwoIndexes(args);
            return new DeleteAllergyCommand(indices.get(0), indices.get(1));
        } catch (ParseException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAllergyCommand.MESSAGE_USAGE), e);
        }
    }
}
