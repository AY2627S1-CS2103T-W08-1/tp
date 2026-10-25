package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertParsesLargestIndex;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertParsesSmallestIndex;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertRejectsEmptyArgs;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertRejectsNonNumericArgs;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertRejectsNonPositiveIndexes;
import static seedu.address.logic.parser.IndexOnlyParserTestUtil.assertRejectsTooLargeIndex;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        // Boundary value analysis: 1 is the smallest valid index; spaces around it are ignored
        assertParsesSmallestIndex(parser, ViewCommand::new);
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        // Equivalence partitioning: no index at all, as empty and as blank input
        assertRejectsEmptyArgs(parser, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonPositiveIndex_throwsParseException() {
        // Boundary value analysis: 0 is the invalid index just below the smallest valid one; -1 and +1 are other
        // signed inputs
        assertRejectsNonPositiveIndexes(parser, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonNumericArgs_throwsParseException() {
        // Equivalence partitioning: input that is not a single whole number, e.g., a word, a decimal, two numbers,
        // a number with a prefix, or an NRIC
        assertRejectsNonNumericArgs(parser, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_largestIndex_returnsViewCommand() {
        // Boundary value analysis: 2147483647 is the largest valid index, one below the 2147483648 that
        // parse_indexTooLarge_throwsParseException rejects
        assertParsesLargestIndex(parser, ViewCommand::new);
    }

    @Test
    public void parse_indexTooLarge_throwsParseException() {
        // Boundary value analysis: 2147483648 is the smallest index too large to be an int
        assertRejectsTooLargeIndex(parser, MESSAGE_INVALID_FORMAT);
    }
}
