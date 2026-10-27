package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCareCommand;
import seedu.address.model.patient.Patient;

public class AddCareCommandParserTest {

    private final AddCareCommandParser parser = new AddCareCommandParser();

    @Test
    public void parse_eachCareCategory_success() {
        assertParseSuccess(parser, "1 med/Metformin 500mg twice daily",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Metformin 500mg twice daily"), List.of(), List.of()));
        assertParseSuccess(parser, "1 al/Penicillin",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of("Penicillin"), List.of()));
        assertParseSuccess(parser, "1 ci/Check blood sugar before breakfast",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of(),
                        List.of("Check blood sugar before breakfast")));
    }

    @Test
    public void parse_repeatedPrefixes_success() {
        assertParseSuccess(parser, "1 med/Metformin med/Aspirin al/Penicillin al/Shellfish",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Metformin", "Aspirin"),
                        List.of("Penicillin", "Shellfish"), List.of()));
    }

    @Test
    public void parse_slashTextThatIsNotPrefix_success() {
        assertParseSuccess(parser, "1 med/Take 500mg/5ml ci/See https://clinic.example/guide",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Take 500mg/5ml"), List.of(),
                        List.of("See https://clinic.example/guide")));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        for (String prefix : List.of("p/", "dob/", "xyz/")) {
            assertParseFailure(parser, "1 med/Aspirin " + prefix + "value",
                    String.format(MESSAGE_UNKNOWN_PREFIX, prefix));
        }
    }

    @Test
    public void parse_noCarePrefix_throwsParseException() {
        assertParseFailure(parser, "1", AddCareCommand.MESSAGE_NO_CARE_INFO);
    }

    @Test
    public void parse_blankEntry_throwsParseException() {
        assertParseFailure(parser, "1 med/", AddCareCommand.MESSAGE_BLANK_MEDICATION);
        assertParseFailure(parser, "1 al/", AddCareCommand.MESSAGE_BLANK_ALLERGEN);
        assertParseFailure(parser, "1 ci/", AddCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
        assertParseFailure(parser, "1 med/Metformin al/", AddCareCommand.MESSAGE_BLANK_ALLERGEN);
        assertParseFailure(parser, "1 al/\u00a0", AddCareCommand.MESSAGE_BLANK_ALLERGEN);
        assertParseFailure(parser, "1 ci/\u00a0", AddCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
    }

    @Test
    public void parse_overlongCareInstruction_throwsParseException() {
        assertParseFailure(parser,
                "1 ci/" + "a".repeat(Patient.MAX_CARE_INSTRUCTION_LENGTH + 1),
                AddCareCommand.MESSAGE_CARE_INSTRUCTION_TOO_LONG);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCareCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "0 med/x", expectedMessage);
        assertParseFailure(parser, "not-an-index med/Metformin", expectedMessage);
        assertParseFailure(parser, "not-an-index", expectedMessage);
        assertParseFailure(parser, "", expectedMessage);
    }
}
