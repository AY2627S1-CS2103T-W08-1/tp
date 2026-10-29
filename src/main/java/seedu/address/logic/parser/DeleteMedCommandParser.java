package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteMedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DeleteMedCommand} object.
 */
public class DeleteMedCommandParser implements Parser<DeleteMedCommand> {

    private static final int PATIENT_INDEX_POSITION = 0;
    private static final int MEDICATION_INDEX_POSITION = 1;

    /**
     * Parses the given {@code String} of arguments in the context of the {@code DeleteMedCommand}
     * and returns a {@code DeleteMedCommand} object for execution.
     *
     * @throws ParseException if the arguments are not exactly a patient index followed by a medication index,
     *     each a positive integer.
     */
    @Override
    public DeleteMedCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            List<Index> indices = ParserUtil.parseTwoIndexes(args);
            return new DeleteMedCommand(indices.get(PATIENT_INDEX_POSITION), indices.get(MEDICATION_INDEX_POSITION));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMedCommand.MESSAGE_USAGE),
                    pe);
        }
    }
}
