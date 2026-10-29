package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddApptCommand;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Medication;

public class AddApptCommandParserTest {

    private static final String VALID_FIELDS = " d/2026-11-05 t/09:30 w/Dr Tan";

    private final AddApptCommandParser parser = new AddApptCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, "1" + VALID_FIELDS + " nt/Fasting",
                new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP));
    }

    @Test
    public void parse_oneDigitHourTime_success() {
        assertParseSuccess(parser, "1 d/2026-11-05 t/9:30 w/Dr Tan nt/Fasting",
                new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP));
    }

    @Test
    public void parse_noBreakSpaceAroundTime_success() {
        assertParseSuccess(parser, "1 d/2026-11-05 t/\u00a009:30 w/Dr Tan nt/Fasting",
                new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP));
        assertParseSuccess(parser, "1 d/2026-11-05 t/09:30\u00a0 w/Dr Tan nt/Fasting",
                new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP));
    }

    @Test
    public void parse_noNote_successWithEmptyNote() {
        Appointment expected = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", "");
        assertParseSuccess(parser, "1" + VALID_FIELDS, new AddApptCommand(INDEX_FIRST_PATIENT, expected));
        assertParseSuccess(parser, "1" + VALID_FIELDS + " nt/",
                new AddApptCommand(INDEX_FIRST_PATIENT, expected));
    }

    @Test
    public void parse_indianNameSlashWords_providerAccepted() {
        for (String token : new String[] {"a/l", "a/p", "A/L", "A/P"}) {
            String provider = "Dr Muthu " + token + " Ramasamy";
            Appointment appointment = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), provider, "");

            assertParseSuccess(parser, "1 d/2026-11-05 t/09:30 w/" + provider,
                    new AddApptCommand(INDEX_FIRST_PATIENT, appointment));
        }
    }

    @Test
    public void parse_slashWordsInNote_keptInNote() {
        // w/o and d/o start like the w/ and d/ prefixes, but must not be read as a second WITH or DATE
        for (String token : new String[] {"w/o", "W/O", "d/o", "s/o", "c/o"}) {
            String note = "MRI " + token + " contrast";
            Appointment appointment = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", note);

            assertParseSuccess(parser, "1 d/2026-11-05 t/09:30 w/Dr Tan nt/" + note,
                    new AddApptCommand(INDEX_FIRST_PATIENT, appointment));
        }
    }

    @Test
    public void parse_missingField_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddApptCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 t/09:30 w/Dr Tan", expectedMessage);
        assertParseFailure(parser, "1 d/2026-11-05 w/Dr Tan", expectedMessage);
        assertParseFailure(parser, "1 d/2026-11-05 t/09:30", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddApptCommand.MESSAGE_USAGE);
        assertParseFailure(parser, VALID_FIELDS, expectedMessage);
        assertParseFailure(parser, "0" + VALID_FIELDS, expectedMessage);
    }

    @Test
    public void parse_invalidValue_throwsParseException() {
        assertParseFailure(parser, "1 d/2026-02-30 t/09:30 w/Dr Tan", Appointment.MESSAGE_DATE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/2026-11-05 t/9:60 w/Dr Tan", Medication.MESSAGE_TIME_CONSTRAINTS);
        assertParseFailure(parser, "1 d/2026-11-05 t/09:30 w/ ", Appointment.MESSAGE_PROVIDER_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "1" + VALID_FIELDS + " d/2026-11-06",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        assertParseFailure(parser, "1 x/12" + VALID_FIELDS, String.format(MESSAGE_UNKNOWN_PREFIX, "x/"));
    }
}
