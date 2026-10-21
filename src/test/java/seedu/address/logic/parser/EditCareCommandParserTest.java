package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCareCommand;

public class EditCareCommandParserTest {

    private final EditCareCommandParser parser = new EditCareCommandParser();

    @Test
    public void parse_validArguments_commandCreated() {
        assertParseSuccess(parser, " 1   2 ci/  Check blood sugar after lunch  ",
                new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                        "Check blood sugar after lunch"));
    }

    @Test
    public void parse_invalidIndicesOrMissingText_rejected() {
        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditCareCommand.MESSAGE_USAGE);
        for (String args : List.of("", " 1 ci/Text", " 1 1 1 ci/Text", " 0 1 ci/Text", " 1 0 ci/Text",
                " -1 1 ci/Text", " 1 -1 ci/Text", " x 1 ci/Text", " 1 2147483648 ci/Text", " 1 1")) {
            assertParseFailure(parser, args, usage);
        }
    }

    @Test
    public void parse_blankText_rejected() {
        assertParseFailure(parser, " 1 1 ci/", EditCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
        assertParseFailure(parser, " 1 1 ci/   ", EditCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
    }

    @Test
    public void parse_duplicateAndUnknownPrefixes_rejected() {
        assertParseFailure(parser, " 1 1 ci/First ci/Second",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_CARE_INSTRUCTION));
        assertParseFailure(parser, " 1 1 x/value", String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/"));
        assertParseFailure(parser, " 1 1 ci/Ask s/o Clinic", String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "s/"));
    }
}
