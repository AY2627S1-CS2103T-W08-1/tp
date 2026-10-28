package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_CLEAR_TIMES_CANNOT_BE_COMBINED;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_NO_MEDICATION_FIELD_TO_EDIT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditMedCommand;
import seedu.address.model.patient.Medication;

public class EditMedCommandParserTest {

    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditMedCommand.MESSAGE_USAGE);
    private static final LocalTime MORNING = LocalTime.of(8, 0);
    private static final LocalTime EVENING = LocalTime.of(20, 0);

    private final EditMedCommandParser parser = new EditMedCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_descriptionOnly_success() {
        assertParseSuccess(parser, " 1 2 med/Metformin 850mg",
                edit(Optional.of("Metformin 850mg"), Optional.empty()));
    }

    @Test
    public void parse_timesOnly_success() {
        assertParseSuccess(parser, " 1 2 t/08:00 t/20:00",
                edit(Optional.empty(), Optional.of(List.of(MORNING, EVENING))));
    }

    @Test
    public void parse_descriptionAndTimes_success() {
        assertParseSuccess(parser, " 1 2 med/Aspirin t/20:00",
                edit(Optional.of("Aspirin"), Optional.of(List.of(EVENING))));
        assertParseSuccess(parser, " 1 2 t/20:00 med/Aspirin",
                edit(Optional.of("Aspirin"), Optional.of(List.of(EVENING))));
    }

    @Test
    public void parse_emptyTime_clearsTimes() {
        assertParseSuccess(parser, " 1 2 t/", edit(Optional.empty(), Optional.of(List.of())));
        assertParseSuccess(parser, " 1 2 t/    ", edit(Optional.empty(), Optional.of(List.of())));
        assertParseSuccess(parser, " 1 2 med/Aspirin t/", edit(Optional.of("Aspirin"), Optional.of(List.of())));
    }

    @Test
    public void parse_extraSpacesAndNoLeadingSpace_success() {
        Optional<List<LocalTime>> times = Optional.of(List.of(MORNING));
        assertParseSuccess(parser, "   1    2   med/   Aspirin   t/  08:00  ", edit(Optional.of("Aspirin"), times));
        assertParseSuccess(parser, "1 2 t/08:00", edit(Optional.empty(), times));
    }

    @Test
    public void parse_repeatedTimes_keptForCommandToCollapse() {
        assertParseSuccess(parser, " 1 2 t/08:00 t/08:00",
                edit(Optional.empty(), Optional.of(List.of(MORNING, MORNING))));
    }

    @Test
    public void parse_differentIndices_success() {
        assertParseSuccess(parser, " 2 1 med/Aspirin", new EditMedCommand(INDEX_SECOND_PATIENT,
                Index.fromOneBased(1), Optional.of("Aspirin"), Optional.empty()));
    }

    @Test
    public void parse_missingOrExtraIndex_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, "     ", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " t/08:00", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 2 3 med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " 1 abc 2 med/Aspirin", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, "1\t2\tt/08:00", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String indices : new String[] {"0 1", "1 0", "-1 1", "1 -1", "a 1", "1 b", "1.5 1", "1 1.5",
            "99999999999 1", "1 99999999999", "+1 1", "1 +1", "0 0"}) {
            assertParseFailure(parser, " " + indices + " med/Aspirin", INVALID_FORMAT_MESSAGE);
        }
    }

    @Test
    public void parse_noFieldToEdit_throwsParseException() {
        assertParseFailure(parser, " 1 2", MESSAGE_NO_MEDICATION_FIELD_TO_EDIT);
        assertParseFailure(parser, "   1   2   ", MESSAGE_NO_MEDICATION_FIELD_TO_EDIT);
    }

    @Test
    public void parse_blankDescription_throwsParseException() {
        assertParseFailure(parser, " 1 2 med/", Medication.MESSAGE_DESCRIPTION_CONSTRAINTS);
        assertParseFailure(parser, " 1 2 med/   ", Medication.MESSAGE_DESCRIPTION_CONSTRAINTS);
        assertParseFailure(parser, " 1 2 med/ t/08:00", Medication.MESSAGE_DESCRIPTION_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTime_throwsParseException() {
        for (String time : new String[] {"24:00", "08:60", "8:30", "0830", "08:30:00", "abc", "-1:00", "08:30pm",
            "08:00 20:00", "08:00,20:00"}) {
            assertParseFailure(parser, " 1 2 t/" + time, Medication.MESSAGE_TIME_CONSTRAINTS);
        }
        assertParseFailure(parser, " 1 2 med/Aspirin t/25:00", Medication.MESSAGE_TIME_CONSTRAINTS);
        assertParseFailure(parser, " 1 2 t/08:00 t/24:00", Medication.MESSAGE_TIME_CONSTRAINTS);
    }

    @Test
    public void parse_emptyTimeMixedWithOtherTimes_throwsParseException() {
        for (String times : new String[] {"t/ t/08:00", "t/08:00 t/", "t/ t/", "t/08:00 t/ t/20:00", "t/   t/08:00"}) {
            assertParseFailure(parser, " 1 2 " + times, MESSAGE_CLEAR_TIMES_CANNOT_BE_COMBINED);
        }
        assertParseFailure(parser, " 1 2 med/Aspirin t/ t/08:00", MESSAGE_CLEAR_TIMES_CANNOT_BE_COMBINED);
    }

    @Test
    public void parse_blankTimeWithInvalidTime_reportsCombinationFirst() {
        assertParseFailure(parser, " 1 2 t/ t/24:00", MESSAGE_CLEAR_TIMES_CANNOT_BE_COMBINED);
    }

    @Test
    public void parse_repeatedDescription_throwsParseException() {
        assertParseFailure(parser, " 1 2 med/Aspirin med/Metformin",
                getErrorMessageForDuplicatePrefixes(PREFIX_MEDICATION));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        assertParseFailure(parser, " 4 1 med/Aspirin p/123", String.format(MESSAGE_UNKNOWN_PREFIX, "p/"));
        for (String prefix : List.of("p/", "dob/", "al/", "ci/")) {
            assertParseFailure(parser, " 1 2 med/Aspirin " + prefix + "value",
                    String.format(MESSAGE_UNKNOWN_PREFIX, prefix));
        }
        for (String prefix : List.of("p/", "dob/", "xyz/", "x/", "T/", "MED/", "al/", "ci/")) {
            assertParseFailure(parser, " 1 2 " + prefix + "value",
                    String.format(MESSAGE_UNKNOWN_PREFIX, prefix));
            assertParseFailure(parser, " 1 2 t/08:00 " + prefix + "value",
                    String.format(MESSAGE_UNKNOWN_PREFIX, prefix));
        }
    }

    @Test
    public void parse_unknownPrefixWithoutMedOrTime_reportsUnrecognizedParameter() {
        assertParseFailure(parser, " 1 1 x/foo", String.format(MESSAGE_UNKNOWN_PREFIX, "x/"));
        assertParseFailure(parser, " 1 1 al/Latex", String.format(MESSAGE_UNKNOWN_PREFIX, "al/"));
    }

    @Test
    public void parse_clinicalSlashWords_preserved() {
        assertParseSuccess(parser, " 1 2 med/Insulin 10 units/day",
                edit(Optional.of("Insulin 10 units/day"), Optional.empty()));
        assertParseSuccess(parser, " 1 2 med/Amoxicillin 250 mg/ml",
                edit(Optional.of("Amoxicillin 250 mg/ml"), Optional.empty()));
    }

    @Test
    public void parse_slashTextThatIsNotPrefix_success() {
        assertParseSuccess(parser, " 1 2 med/Take 500mg/5ml t/08:00",
                edit(Optional.of("Take 500mg/5ml"), Optional.of(List.of(MORNING))));
        assertParseSuccess(parser, " 1 2 med/Dose 1/2 tablet", edit(Optional.of("Dose 1/2 tablet"), Optional.empty()));
        assertParseSuccess(parser, " 1 2 med/See https://clinic.example/guide",
                edit(Optional.of("See https://clinic.example/guide"), Optional.empty()));
    }

    @Test
    public void parse_specialCharactersAndLongDescription_success() {
        String special = "<b>\"';DROP TABLE--&é中文";
        assertParseSuccess(parser, " 1 2 med/" + special, edit(Optional.of(special), Optional.empty()));
        String longText = "x".repeat(5000);
        assertParseSuccess(parser, " 1 2 med/" + longText, edit(Optional.of(longText), Optional.empty()));
    }

    private static EditMedCommand edit(Optional<String> description, Optional<List<LocalTime>> times) {
        return new EditMedCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, description, times);
    }
}
