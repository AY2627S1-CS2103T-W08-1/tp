package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditApptCommand;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Medication;

public class EditApptCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditApptCommand.MESSAGE_USAGE);
    private static final Optional<LocalDate> NEW_DATE = Optional.of(LocalDate.of(2026, 11, 6));

    private final EditApptCommandParser parser = new EditApptCommandParser();

    @Test
    public void parse_oneField_success() {
        EditApptCommand expectedCommand = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                NEW_DATE, Optional.empty(), Optional.empty(), Optional.empty());
        assertParseSuccess(parser, "1 2 d/2026-11-06", expectedCommand);
        assertParseSuccess(parser, "  1   2   d/2026-11-06  ", expectedCommand);
    }

    @Test
    public void parse_allFields_success() {
        assertParseSuccess(parser, "1 2 d/2026-11-06 t/10:00 w/Dr  Lim nt/ Bring scans ",
                new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, NEW_DATE,
                        Optional.of(LocalTime.of(10, 0)), Optional.of("Dr Lim"), Optional.of("Bring scans")));
    }

    @Test
    public void parse_emptyNote_successWithEmptyNote() {
        assertParseSuccess(parser, "1 2 nt/", new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("")));
    }

    @Test
    public void parse_noFields_throwsParseException() {
        assertParseFailure(parser, "1 2", EditApptCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_wrongIndexes_throwsParseException() {
        assertParseFailure(parser, " d/2026-11-06", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 d/2026-11-06", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2 3 d/2026-11-06", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 1 d/2026-11-06", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x d/2026-11-06", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_throwsParseException() {
        assertParseFailure(parser, "1 2 d/2026-02-30", Appointment.MESSAGE_DATE_CONSTRAINTS);
        assertParseFailure(parser, "1 2 t/9:30", Medication.MESSAGE_TIME_CONSTRAINTS);
        assertParseFailure(parser, "1 2 w/ ", Appointment.MESSAGE_PROVIDER_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_throwsParseException() {
        assertParseFailure(parser, "1 2 d/2026-11-06 d/2026-11-07",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        assertParseFailure(parser, "1 2 d/2026-11-06 x/12", String.format(MESSAGE_UNKNOWN_PREFIX, "x/"));
    }
}
