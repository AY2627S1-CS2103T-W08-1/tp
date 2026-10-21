package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnarchiveCommand;

public class UnarchiveCommandParserTest {

    private final UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validIndex_returnsUnarchiveCommand() {
        assertParseSuccess(parser, "1", new UnarchiveCommand(INDEX_FIRST_PATIENT));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnarchiveCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "0", "-1", "abc", "1 2"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
