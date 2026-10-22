package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CancelApptCommand;

public class CancelApptCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, CancelApptCommand.MESSAGE_USAGE);

    private final CancelApptCommandParser parser = new CancelApptCommandParser();

    @Test
    public void parse_twoIndexes_success() {
        CancelApptCommand expectedCommand = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);
        assertParseSuccess(parser, "1 2", expectedCommand);
        assertParseSuccess(parser, "  1   2  ", expectedCommand);
    }

    @Test
    public void parse_wrongNumberOfArguments_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2 3", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "0 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 -2", MESSAGE_INVALID_FORMAT);
    }
}
