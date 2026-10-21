package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCareCommand;

public class DeleteCareCommandParserTest {

    private final DeleteCareCommandParser parser = new DeleteCareCommandParser();

    @Test
    public void parse_twoIndices_commandCreated() {
        assertParseSuccess(parser, " 1   2 ", new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT));
    }

    @Test
    public void parse_invalidIndicesOrExtraArguments_rejected() {
        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DeleteCareCommand.MESSAGE_USAGE);
        for (String args : List.of("", " 1", " 1 2 3", " 0 1", " 1 0", " -1 1", " 1 -1", " x 1",
                " 1 2147483648", " 1 1 ci/Text")) {
            assertParseFailure(parser, args, usage);
        }
    }
}
