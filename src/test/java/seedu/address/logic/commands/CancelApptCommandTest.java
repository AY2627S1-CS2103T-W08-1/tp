package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CancelApptCommand.MESSAGE_SUCCESS;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.createModelWithAppointments;
import static seedu.address.logic.commands.CommandTestUtil.createViewedModelWithAppointments;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CARDIOLOGY;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.testutil.AdjustableClock;

public class CancelApptCommandTest {

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(null, INDEX_FIRST_PATIENT));
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(INDEX_FIRST_PATIENT, null));
    }

    @Test
    public void execute_validIndexes_removesAppointment() {
        Model model = createModelWithAppointments(CHECKUP, CARDIOLOGY);
        Model expectedModel = createViewedModelWithAppointments(CHECKUP);

        assertCommandSuccess(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), model,
                String.format(MESSAGE_SUCCESS, 2, ALICE.getName(), CARDIOLOGY), expectedModel);
    }

    @Test
    public void execute_pastAndUpcomingAppointments_numbersUpcomingFirst() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Dr Lee", "");

        assertCommandSuccess(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(past, CHECKUP),
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), CHECKUP),
                createViewedModelWithAppointments(past));
    }

    @Test
    public void execute_appointmentPassesWhileShown_removesAppointmentWithShownNumber() {
        AdjustableClock clock = new AdjustableClock(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
        Appointment soon = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 1), "Dr Tan", "");
        Model model = createModelWithAppointments(clock, soon, CARDIOLOGY);
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        clock.advance(Duration.ofMinutes(2));

        // the panel still shows the appointment at 12:01 as number 1, although it has now passed
        assertCommandSuccess(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT), model,
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), soon),
                createViewedModelWithAppointments(CARDIOLOGY));
    }

    @Test
    public void execute_appointmentIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        assertCommandFailure(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(new CancelApptCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        CancelApptCommand command = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);

        // same values -> returns true
        assertTrue(command.equals(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different patient index or appointment index -> returns false
        assertFalse(command.equals(new CancelApptCommand(INDEX_SECOND_PATIENT, INDEX_SECOND_PATIENT)));
        assertFalse(command.equals(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT)));
    }

    @Test
    public void toStringMethod() {
        CancelApptCommand command = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);
        String expected = CancelApptCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT + ", appointmentIndex=" + INDEX_SECOND_PATIENT + "}";
        assertEquals(expected, command.toString());
    }
}
