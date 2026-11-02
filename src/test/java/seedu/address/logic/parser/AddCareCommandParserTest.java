package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Medication;
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
    public void parse_standaloneSlashWords_success() {
        for (String token : List.of("s/o", "d/o", "c/o", "a/l", "a/p", "w/o", "C/O")) {
            String instruction = "Bring documents " + token + " daughter";
            assertParseSuccess(parser, "1 ci/" + instruction,
                    new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of(), List.of(instruction)));
        }
    }

    @Test
    public void parse_standaloneSlashWordsInMedication_success() {
        for (String word : List.of("s/o", "d/o", "c/o", "a/l", "a/p", "w/o", "W/O")) {
            String medication = "Paracetamol 1g " + word + " food";
            assertParseSuccess(parser, "1 med/" + medication,
                    new AddCareCommand(INDEX_FIRST_PATIENT, List.of(medication), List.of(), List.of()));
        }
    }

    @Test
    public void parse_otherSlashWords_unchanged() {
        assertParseSuccess(parser, "1 med/Take f/o g/d",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Take f/o g/d"), List.of(), List.of()));
        assertParseFailure(parser, "1 med/Take A/B", String.format(MESSAGE_UNKNOWN_PREFIX, "A/"));
        assertParseFailure(parser, "1 med/Take n/a", String.format(MESSAGE_UNKNOWN_PREFIX, "n/"));
        assertParseSuccess(parser, "1 med/Take N/A",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Take N/A"), List.of(), List.of()));
    }

    @Test
    public void parse_punctuatedSlashWord_throwsParseException() {
        for (String word : List.of("c/o", "s/o", "w/o")) {
            assertParseFailure(parser, "1 med/Take " + word + ", daughter",
                    String.format(MESSAGE_PUNCTUATED_SLASH_TOKEN, word + ",", word));
        }
    }

    @Test
    public void parse_realPrefixesAfterSlashWord_stillDetected() {
        assertParseSuccess(parser, "1 med/Take w/o food al/Latex",
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Take w/o food"), List.of("Latex"), List.of()));
        assertParseFailure(parser, "1 med/Take w/o food t/08:00", String.format(MESSAGE_UNKNOWN_PREFIX, "t/"));
        assertParseFailure(parser, "1 med/Take w/o food p/123", String.format(MESSAGE_UNKNOWN_PREFIX, "p/"));
    }

    @Test
    public void parse_slashWordBeforeField_sameMessageAsOtherStrayWord() {
        ParseException slashWordError = assertThrows(ParseException.class, () ->
                parser.parse("1 w/o med/Paracetamol"));
        ParseException strayWordError = assertThrows(ParseException.class, () ->
                parser.parse("1 hello med/Paracetamol"));
        assertEquals(strayWordError.getMessage(), slashWordError.getMessage());
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        for (String prefix : List.of("p/", "dob/")) {
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
        assertParseFailure(parser, "1 med/\u00a0", AddCareCommand.MESSAGE_BLANK_MEDICATION);
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
    public void parse_maxLengthMedicationAndAllergen_success() {
        String medication = "m".repeat(Medication.MAX_DESCRIPTION_LENGTH);
        String allergen = "a".repeat(Patient.MAX_ALLERGEN_LENGTH);

        assertParseSuccess(parser, "1 med/" + medication + " al/" + allergen,
                new AddCareCommand(INDEX_FIRST_PATIENT, List.of(medication), List.of(allergen), List.of()));
    }

    @Test
    public void parse_overlongMedicationOrAllergen_throwsParseException() {
        assertParseFailure(parser,
                "1 med/" + "m".repeat(Medication.MAX_DESCRIPTION_LENGTH + 1),
                AddCareCommand.MESSAGE_MEDICATION_TOO_LONG);
        assertParseFailure(parser,
                "1 al/" + "a".repeat(Patient.MAX_ALLERGEN_LENGTH + 1),
                AddCareCommand.MESSAGE_ALLERGEN_TOO_LONG);
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
