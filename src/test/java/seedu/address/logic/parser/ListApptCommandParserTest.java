package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListApptCommand;

public class ListApptCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListApptCommand.MESSAGE_USAGE);

    private final ListApptCommandParser parser = new ListApptCommandParser();

    @Test
    public void parse_noArguments_listsAllPatients() {
        assertParseSuccess(parser, "", new ListApptCommand(Optional.empty()));
        assertParseSuccess(parser, "   ", new ListApptCommand(Optional.empty()));
    }

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, " 1 ", new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT)));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "abc", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2", MESSAGE_INVALID_FORMAT);
    }
}
