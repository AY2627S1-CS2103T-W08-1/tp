package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CancelApptCommand.MESSAGE_SUCCESS;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.testutil.PatientBuilder;

public class CancelApptCommandTest {

    /** Fixes the current date and time at 2026-10-18 12:00:30, half a minute into the 12:00 minute. */
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-10-18T12:00:30Z"), ZoneOffset.UTC);

    private static final Appointment LATER_APPOINTMENT =
            new Appointment(LocalDate.of(2026, 12, 10), LocalTime.of(14, 0), "Cardiology clinic", "");

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(null, INDEX_FIRST_PATIENT));
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(INDEX_FIRST_PATIENT, null));
        assertThrows(NullPointerException.class, ()
            -> new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, null));
    }

    @Test
    public void execute_validIndexes_removesAppointment() {
        Model model = createModelWithAppointments(CHECKUP, LATER_APPOINTMENT);
        Model expectedModel = createModelWithAppointments(CHECKUP);

        assertCommandSuccess(createCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), model,
                String.format(MESSAGE_SUCCESS, 2, ALICE.getName(), LATER_APPOINTMENT), expectedModel);
    }

    @Test
    public void execute_pastAndUpcomingAppointments_numbersUpcomingFirst() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Dr Lee", "");

        assertCommandSuccess(createCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(past, CHECKUP),
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), CHECKUP), createModelWithAppointments(past));
    }

    @Test
    public void execute_appointmentIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(createCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        assertCommandFailure(createCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(createCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        CancelApptCommand command = createCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);

        // same values -> returns true
        assertTrue(command.equals(createCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different patient index, appointment index or clock -> returns false
        assertFalse(command.equals(createCommand(INDEX_SECOND_PATIENT, INDEX_SECOND_PATIENT)));
        assertFalse(command.equals(createCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT)));
        assertFalse(command.equals(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));
    }

    @Test
    public void toStringMethod() {
        CancelApptCommand command = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);
        String expected = CancelApptCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT + ", appointmentIndex=" + INDEX_SECOND_PATIENT + "}";
        assertEquals(expected, command.toString());
    }

    /**
     * Returns a {@code CancelApptCommand} that uses the fixed clock.
     */
    private static CancelApptCommand createCommand(Index targetIndex, Index appointmentIndex) {
        return new CancelApptCommand(targetIndex, appointmentIndex, FIXED_CLOCK);
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
