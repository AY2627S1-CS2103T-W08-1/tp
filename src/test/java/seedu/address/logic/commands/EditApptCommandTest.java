package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.AddApptCommand.MESSAGE_CLASH;
import static seedu.address.logic.commands.AddApptCommand.MESSAGE_DUPLICATE_APPOINTMENT;
import static seedu.address.logic.commands.AddApptCommand.MESSAGE_PAST_APPOINTMENT;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.EditApptCommand.MESSAGE_SUCCESS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CARDIOLOGY;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.testutil.PatientBuilder;

public class EditApptCommandTest {

    private static final LocalDate NEW_DATE = LocalDate.of(2026, 11, 6);
    private static final LocalTime NEW_TIME = LocalTime.of(10, 0);

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditApptCommand(null, INDEX_FIRST_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new EditApptCommand(INDEX_FIRST_PATIENT,
                INDEX_FIRST_PATIENT, Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.empty(), null));
    }

    @Test
    public void execute_eachFieldAlone_editsOnlyThatField() {
        assertEditSuccess(createCommand(Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.empty()), new Appointment(NEW_DATE, CHECKUP.getTime(), "Dr Tan", "Fasting"), "");
        assertEditSuccess(createCommand(Optional.empty(), Optional.of(NEW_TIME), Optional.empty(),
                Optional.empty()), new Appointment(CHECKUP.getDate(), NEW_TIME, "Dr Tan", "Fasting"), "");
        assertEditSuccess(createCommand(Optional.empty(), Optional.empty(), Optional.of("Dr Lim"),
                Optional.empty()),
                new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Lim", "Fasting"), "");
        assertEditSuccess(createCommand(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of("Bring scans")),
                new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", "Bring scans"), "");
    }

    @Test
    public void execute_severalFields_editsAllOfThem() {
        assertEditSuccess(createCommand(Optional.of(NEW_DATE), Optional.of(NEW_TIME), Optional.of("Dr Lim"),
                Optional.empty()), new Appointment(NEW_DATE, NEW_TIME, "Dr Lim", "Fasting"), "");
    }

    @Test
    public void execute_emptyNote_clearsNote() {
        assertEditSuccess(createCommand(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of("")), new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", ""), "");
    }

    @Test
    public void execute_pastDateAndTime_editsWithPastNote() {
        LocalDate today = LocalDate.of(2026, 10, 18);
        LocalTime oneMinuteBeforeNow = LocalTime.of(11, 59);
        assertEditSuccess(createCommand(Optional.of(today), Optional.of(oneMinuteBeforeNow), Optional.empty(),
                Optional.empty()), new Appointment(today, oneMinuteBeforeNow, "Dr Tan", "Fasting"),
                "\n" + MESSAGE_PAST_APPOINTMENT);
    }

    @Test
    public void execute_unchangedValues_editsWithoutDuplicateOrClash() {
        assertEditSuccess(createCommand(Optional.empty(), Optional.empty(), Optional.of("Dr Tan"),
                Optional.empty()), CHECKUP, "");
    }

    @Test
    public void execute_sameTimeAsOtherAppointment_editsWithClashWarning() {
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(CHECKUP.getDate()), Optional.of(CHECKUP.getTime()), Optional.empty(),
                Optional.empty(), FIXED_CLOCK);
        Appointment editedAppointment =
                new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Cardiology clinic", "");

        assertCommandSuccess(command, createModelWithAppointments(CHECKUP, CARDIOLOGY),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), editedAppointment)
                + "\n" + String.format(MESSAGE_CLASH, ALICE.getName(), "Dr Tan"),
                createModelWithAppointments(CHECKUP, editedAppointment));
    }

    @Test
    public void execute_sameAsOtherAppointment_throwsCommandException() {
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(CHECKUP.getDate()), Optional.of(CHECKUP.getTime()),
                Optional.of("dr  TAN"), Optional.empty(), FIXED_CLOCK);
        assertCommandFailure(command, createModelWithAppointments(CHECKUP, CARDIOLOGY),
                MESSAGE_DUPLICATE_APPOINTMENT);
    }

    @Test
    public void execute_appointmentIndexOutOfRange_throwsCommandException() {
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty(), FIXED_CLOCK);
        assertCommandFailure(command, createModelWithAppointments(CHECKUP),
                Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientIndexOutOfRange_throwsCommandException() {
        EditApptCommand command = new EditApptCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty(), FIXED_CLOCK);
        assertCommandFailure(command, createModelWithAppointments(CHECKUP),
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        EditApptCommand command = createCommand(Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.empty());

        // same values -> returns true
        assertTrue(command.equals(createCommand(Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.empty())));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different index, field or clock -> returns false
        assertFalse(command.equals(new EditApptCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty(), FIXED_CLOCK)));
        assertFalse(command.equals(new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty(), FIXED_CLOCK)));
        assertFalse(command.equals(createCommand(Optional.empty(), Optional.of(NEW_TIME), Optional.empty(),
                Optional.empty())));
        assertFalse(command.equals(createCommand(Optional.of(NEW_DATE), Optional.empty(),
                Optional.of("Dr Lim"), Optional.empty())));
        assertFalse(command.equals(createCommand(Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.of(""))));
        assertFalse(command.equals(new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of(NEW_DATE), Optional.empty(), Optional.empty(), Optional.empty())));
    }

    @Test
    public void toStringMethod() {
        EditApptCommand command = createCommand(Optional.of(NEW_DATE), Optional.empty(), Optional.empty(),
                Optional.of(""));
        String expected = EditApptCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT + ", appointmentIndex=" + INDEX_FIRST_PATIENT
                + ", newDate=" + Optional.of(NEW_DATE) + ", newTime=" + Optional.empty()
                + ", newProvider=" + Optional.empty() + ", newNote=" + Optional.of("") + "}";
        assertEquals(expected, command.toString());
    }

    /**
     * Returns an {@code EditApptCommand} with the fixed clock that edits the first appointment of the first
     * patient with the given fields.
     */
    private static EditApptCommand createCommand(Optional<LocalDate> newDate, Optional<LocalTime> newTime,
            Optional<String> newProvider, Optional<String> newNote) {
        return new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, newDate, newTime, newProvider,
                newNote, FIXED_CLOCK);
    }

    /**
     * Asserts that {@code command} changes {@code CHECKUP}, the only appointment of the only patient,
     * to {@code editedAppointment} and shows the success message followed by {@code expectedWarnings}.
     */
    private static void assertEditSuccess(EditApptCommand command, Appointment editedAppointment,
            String expectedWarnings) {
        assertCommandSuccess(command, createModelWithAppointments(CHECKUP),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), editedAppointment) + expectedWarnings,
                createModelWithAppointments(editedAppointment));
    }

    /**
     * Returns a model whose only patient is Alice with the given {@code appointments}.
     */
    private static Model createModelWithAppointments(Appointment... appointments) {
        Model model = new ModelManager();
        model.addPatient(new PatientBuilder(ALICE).withAppointments(appointments).build());
        return model;
    }
}
