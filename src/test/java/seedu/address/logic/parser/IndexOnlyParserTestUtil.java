package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.List;
import java.util.function.Function;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;

/**
 * Checks parsers of commands whose only argument is an index in the displayed patient list, e.g., {@code view} and
 * {@code export}, against the same partitions of valid and invalid input, so that each partition is defined once.
 */
public class IndexOnlyParserTestUtil {

    /** Largest valid index, which is also the largest {@code int}. */
    private static final String LARGEST_INDEX = "2147483647";

    /** Smallest index that is too large, one more than the largest {@code int}. */
    private static final String TOO_LARGE_INDEX = "2147483648";

    private static final List<String> EMPTY_ARGS = List.of("", "   ");
    private static final List<String> NON_POSITIVE_INDEXES = List.of("0", "-1", "+1");
    private static final List<String> NON_NUMERIC_ARGS = List.of("a", "1.5", "1 2", "1 n/Alice", "S0000001A");

    private IndexOnlyParserTestUtil() {} // prevents instantiation

    /**
     * Asserts that {@code parser} parses the smallest index, 1, with and without spaces around it, into the command
     * that {@code commandFactory} creates for that index.
     */
    public static void assertParsesSmallestIndex(Parser<? extends Command> parser,
            Function<Index, ? extends Command> commandFactory) {
        assertParseSuccess(parser, "1", commandFactory.apply(INDEX_FIRST_PATIENT));
        assertParseSuccess(parser, "  1  ", commandFactory.apply(INDEX_FIRST_PATIENT));
    }

    /**
     * Asserts that {@code parser} parses the largest index into the command that {@code commandFactory} creates for
     * that index.
     */
    public static void assertParsesLargestIndex(Parser<? extends Command> parser,
            Function<Index, ? extends Command> commandFactory) {
        assertParseSuccess(parser, LARGEST_INDEX, commandFactory.apply(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    /**
     * Asserts that {@code parser} rejects empty and blank input with {@code expectedMessage}.
     */
    public static void assertRejectsEmptyArgs(Parser<? extends Command> parser, String expectedMessage) {
        assertRejectsAll(parser, EMPTY_ARGS, expectedMessage);
    }

    /**
     * Asserts that {@code parser} rejects zero, negative and signed indexes with {@code expectedMessage}.
     */
    public static void assertRejectsNonPositiveIndexes(Parser<? extends Command> parser, String expectedMessage) {
        assertRejectsAll(parser, NON_POSITIVE_INDEXES, expectedMessage);
    }

    /**
     * Asserts that {@code parser} rejects input that is not a single whole number with {@code expectedMessage}.
     */
    public static void assertRejectsNonNumericArgs(Parser<? extends Command> parser, String expectedMessage) {
        assertRejectsAll(parser, NON_NUMERIC_ARGS, expectedMessage);
    }

    /**
     * Asserts that {@code parser} rejects the smallest index that is too large with {@code expectedMessage}.
     */
    public static void assertRejectsTooLargeIndex(Parser<? extends Command> parser, String expectedMessage) {
        assertParseFailure(parser, TOO_LARGE_INDEX, expectedMessage);
    }

    private static void assertRejectsAll(Parser<? extends Command> parser, List<String> inputs,
            String expectedMessage) {
        for (String input : inputs) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }
}
