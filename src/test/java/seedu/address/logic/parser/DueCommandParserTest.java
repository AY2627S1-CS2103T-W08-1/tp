package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.Clock;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DueCommand;

public class DueCommandParserTest {

    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DueCommand.MESSAGE_USAGE);
    private static final Clock SYSTEM_CLOCK = Clock.systemDefaultZone();

    private final DueCommandParser parser = new DueCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_noArguments_listsAllPatients() {
        assertParseSuccess(parser, "", new DueCommand(SYSTEM_CLOCK));
        assertParseSuccess(parser, " ", new DueCommand(SYSTEM_CLOCK));
        assertParseSuccess(parser, "     \t ", new DueCommand(SYSTEM_CLOCK));
    }

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, " 1", new DueCommand(INDEX_FIRST_PATIENT, SYSTEM_CLOCK));
        assertParseSuccess(parser, "2", new DueCommand(INDEX_SECOND_PATIENT, SYSTEM_CLOCK));
        assertParseSuccess(parser, "   2   ", new DueCommand(INDEX_SECOND_PATIENT, SYSTEM_CLOCK));
        assertParseSuccess(parser, " 2147483647",
                new DueCommand(Index.fromOneBased(Integer.MAX_VALUE), SYSTEM_CLOCK));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String args : new String[] {" 0", " -1", " abc", " 1.5", " +1", " 99999999999", " 2147483648",
            " 1,2", " 1/2", " 1a", " one"}) {
            assertParseFailure(parser, args, INVALID_FORMAT_MESSAGE);
        }
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        assertParseFailure(parser, " 1 2", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 extra", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " extra", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 t/08:00", INVALID_FORMAT_MESSAGE);
    }
}
