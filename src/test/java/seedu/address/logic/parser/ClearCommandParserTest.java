package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_confirmation_success() throws Exception {
        assertTrue(parser.parse("confirm") instanceof ClearCommand);
        assertTrue(parser.parse(" \tconfirm \t") instanceof ClearCommand);
    }

    @Test
    public void parse_missingOrIncorrectConfirmation_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " \t", "1", "CONFIRM", "confirm 1", "confirm confirm", "confirmx"}) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
