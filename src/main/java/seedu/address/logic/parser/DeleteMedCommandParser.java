package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteMedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DeleteMedCommand} object.
 */
public class DeleteMedCommandParser implements Parser<DeleteMedCommand> {

    private static final int EXPECTED_INDEX_COUNT = 2;
    private static final int PATIENT_INDEX_POSITION = 0;
    private static final int MEDICATION_INDEX_POSITION = 1;
    private static final String WHITESPACE_REGEX = "\\s+";

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
        String[] tokens = args.trim().split(WHITESPACE_REGEX);
        if (args.isBlank() || tokens.length != EXPECTED_INDEX_COUNT) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMedCommand.MESSAGE_USAGE));
        }

        try {
            Index patientIndex = ParserUtil.parseIndex(tokens[PATIENT_INDEX_POSITION]);
            Index medicationIndex = ParserUtil.parseIndex(tokens[MEDICATION_INDEX_POSITION]);
            return new DeleteMedCommand(patientIndex, medicationIndex);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMedCommand.MESSAGE_USAGE),
                    pe);
        }
    }
}
