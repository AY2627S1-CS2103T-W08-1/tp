package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.patient.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "", FindCommandParser.MESSAGE_NO_KEYWORDS);
    }

    @Test
    public void parse_blankArg_throwsParseException() {
        assertParseFailure(parser, "     ", FindCommandParser.MESSAGE_NO_KEYWORDS);
    }

    @Test
    public void parse_singleKeyword_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertParseSuccess(parser, "Alice", expectedFindCommand);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_unicodeSpacesBetweenKeywords_splitsIntoKeywords() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alex", "Yeoh")));

        // a no-break space, an em space and an ideographic space, as copied from other applications
        assertParseSuccess(parser, "Alex\u00a0Yeoh", expectedFindCommand);
        assertParseSuccess(parser, "\u00a0Alex\u2003Yeoh\u3000", expectedFindCommand);
        assertParseSuccess(parser, "Alex \u00a0 Yeoh", expectedFindCommand);
    }

    @Test
    public void parse_onlyUnicodeSpaces_throwsParseException() {
        assertParseFailure(parser, "\u00a0", FindCommandParser.MESSAGE_NO_KEYWORDS);
        assertParseFailure(parser, " \u2003\u3000 ", FindCommandParser.MESSAGE_NO_KEYWORDS);
    }

    @Test
    public void parse_decomposedAccentKeyword_returnsComposedKeyword() {
        assertParseSuccess(parser, "Jose\u0301",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Jos\u00e9"))));
    }

    @Test
    public void parse_numericKeyword_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("123")));
        assertParseSuccess(parser, "123", expectedFindCommand);
    }

}
