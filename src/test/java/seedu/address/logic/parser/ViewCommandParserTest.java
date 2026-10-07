package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

    private ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PATIENT));

        // leading and trailing whitespace
        assertParseSuccess(parser, "  1  ", new ViewCommand(INDEX_FIRST_PATIENT));
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonPositiveIndex_throwsParseException() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "+1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonNumericArgs_throwsParseException() {
        assertParseFailure(parser, "a", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Alice", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexTooLarge_throwsParseException() {
        assertParseFailure(parser, "2147483648", MESSAGE_INVALID_FORMAT);
    }
}
