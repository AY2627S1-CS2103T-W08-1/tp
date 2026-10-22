package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.CancelApptCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code CancelApptCommand} object.
 */
public class CancelApptCommandParser implements Parser<CancelApptCommand> {

    /** The command takes the patient's index, then the appointment's number. */
    private static final int NUMBER_OF_INDEXES = 2;

    /**
     * Parses the given {@code String} of arguments in the context of the CancelApptCommand
     * and returns a CancelApptCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public CancelApptCommand parse(String args) throws ParseException {
        String usageMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, CancelApptCommand.MESSAGE_USAGE);
        String[] indexes = args.trim().split("\\s+");
        if (indexes.length != NUMBER_OF_INDEXES) {
            throw new ParseException(usageMessage);
        }

        try {
            Index patientIndex = ParserUtil.parseIndex(indexes[0]);
            Index appointmentIndex = ParserUtil.parseIndex(indexes[1]);
            return new CancelApptCommand(patientIndex, appointmentIndex);
        } catch (ParseException pe) {
            throw new ParseException(usageMessage, pe);
        }
    }
}
