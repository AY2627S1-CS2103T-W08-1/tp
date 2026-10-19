package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteMedCommand;

public class DeleteMedCommandParserTest {

    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMedCommand.MESSAGE_USAGE);

    private final DeleteMedCommandParser parser = new DeleteMedCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validIndices_success() {
        assertParseSuccess(parser, " 1 2", new DeleteMedCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT));
        assertParseSuccess(parser, "2 1", new DeleteMedCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT));
        assertParseSuccess(parser, " 1 1", new DeleteMedCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT));
    }

    @Test
    public void parse_extraWhitespace_success() {
        DeleteMedCommand expected = new DeleteMedCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);
        assertParseSuccess(parser, "    1      2    ", expected);
        assertParseSuccess(parser, "1\t2", expected);
    }

    @Test
    public void parse_largeValidIndices_success() {
        assertParseSuccess(parser, " 2147483647 2147483647",
                new DeleteMedCommand(Index.fromOneBased(Integer.MAX_VALUE), Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_missingIndices_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, "     ", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 2   ", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_extraTokens_throwsParseException() {
        assertParseFailure(parser, " 1 2 3", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 2 extra", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 2 med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 2 x/foo", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_nonNumericIndices_throwsParseException() {
        assertParseFailure(parser, " a 1", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 b", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " a b", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " one two", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1,2", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1/2", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_nonPositiveOrMalformedIndices_throwsParseException() {
        for (String indices : new String[] {"0 1", "1 0", "0 0", "-1 1", "1 -1", "1.5 1", "1 1.5", "+1 1", "1 +1",
            "99999999999 1", "1 99999999999", "2147483648 1", "1 2147483648"}) {
            assertParseFailure(parser, " " + indices, INVALID_FORMAT_MESSAGE);
        }
    }
}
