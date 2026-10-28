package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_CLASH;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_APPOINTMENT;
import static seedu.address.logic.Messages.MESSAGE_FAR_FUTURE_APPOINTMENT;
import static seedu.address.logic.Messages.MESSAGE_PAST_APPOINTMENT;
import static seedu.address.logic.commands.AddApptCommand.MESSAGE_SUCCESS;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class AddApptCommandTest {

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddApptCommand(null, CHECKUP));
        assertThrows(NullPointerException.class, () -> new AddApptCommand(INDEX_FIRST_PATIENT, null));
        assertThrows(NullPointerException.class, ()
            -> new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP, null));
    }

    @Test
    public void execute_futureAppointment_addsAppointment() {
        assertAddSuccess(ALICE, CHECKUP, new PatientBuilder(ALICE).withAppointments(CHECKUP).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), CHECKUP));
    }

    @Test
    public void execute_oneMinuteBeforeNow_addsWithPastNote() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(11, 59), "Dr Tan", "");
        assertAddSuccess(ALICE, past, new PatientBuilder(ALICE).withAppointments(past).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), past) + "\n" + MESSAGE_PAST_APPOINTMENT);
    }

    @Test
    public void execute_appointmentInCurrentMinute_addsWithoutPastNote() {
        Appointment now = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 0), "Dr Tan", "");
        assertAddSuccess(ALICE, now, new PatientBuilder(ALICE).withAppointments(now).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), now));
    }

    @Test
    public void execute_dayAfterTwoYearsAhead_addsWithFarFutureNote() {
        Appointment farAhead = new Appointment(LocalDate.of(2028, 10, 19), LocalTime.of(9, 30), "Dr Tan", "");
        assertAddSuccess(ALICE, farAhead, new PatientBuilder(ALICE).withAppointments(farAhead).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), farAhead) + "\n" + MESSAGE_FAR_FUTURE_APPOINTMENT);
    }

    @Test
    public void execute_exactlyTwoYearsAhead_addsWithoutFarFutureNote() {
        Appointment twoYearsAhead = new Appointment(LocalDate.of(2028, 10, 18), LocalTime.of(23, 59), "Dr Tan", "");
        assertAddSuccess(ALICE, twoYearsAhead, new PatientBuilder(ALICE).withAppointments(twoYearsAhead).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), twoYearsAhead));
    }

    @Test
    public void execute_sameTimeDifferentProvider_addsWithClashWarning() {
        Patient patient = new PatientBuilder(ALICE).withAppointments(CHECKUP).build();
        Appointment clash = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Lim", "");
        assertAddSuccess(patient, clash, new PatientBuilder(ALICE).withAppointments(CHECKUP, clash).build(),
                String.format(MESSAGE_SUCCESS, ALICE.getName(), clash)
                + "\n" + String.format(MESSAGE_CLASH, ALICE.getName(), "Dr Tan"));
    }

    @Test
    public void execute_archivedPatientForcedIntoDisplayedList_preservesArchivedState() throws Exception {
        Model model = new ModelManager();
        model.addPatient(new PatientBuilder(ALICE).withArchived(true).build());
        model.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);

        new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP, FIXED_CLOCK).execute(model);

        Patient updatedPatient = model.getAddressBook().getPatientList().get(0);
        assertTrue(updatedPatient.isArchived());
        assertEquals(List.of(CHECKUP), updatedPatient.getAppointments());
    }

    @Test
    public void execute_duplicateAppointment_throwsCommandException() {
        Model model = new ModelManager();
        model.addPatient(new PatientBuilder(ALICE).withAppointments(CHECKUP).build());
        Appointment duplicate = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "dr  TAN", "");
        assertCommandFailure(new AddApptCommand(INDEX_FIRST_PATIENT, duplicate, FIXED_CLOCK), model,
                MESSAGE_DUPLICATE_APPOINTMENT);
    }

    @Test
    public void execute_dateBeforeDob_throwsCommandException() {
        Model model = new ModelManager();
        model.addPatient(ALICE);
        Appointment dayBeforeBirth = new Appointment(ALICE.getDob().value.minusDays(1), LocalTime.of(10, 0),
                "Dr Tan", "");
        assertCommandFailure(new AddApptCommand(INDEX_FIRST_PATIENT, dayBeforeBirth, FIXED_CLOCK), model,
                String.format(Messages.MESSAGE_APPOINTMENT_BEFORE_DOB, ALICE.getDob()));
    }

    @Test
    public void execute_dateOnDob_addsAppointment() throws Exception {
        Model model = new ModelManager();
        model.addPatient(ALICE);
        Appointment onBirthday = new Appointment(ALICE.getDob().value, LocalTime.of(10, 0), "Dr Tan", "");

        new AddApptCommand(INDEX_FIRST_PATIENT, onBirthday, FIXED_CLOCK).execute(model);

        assertEquals(List.of(onBirthday), model.getAddressBook().getPatientList().get(0).getAppointments());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Model model = new ModelManager();
        model.addPatient(ALICE);
        assertCommandFailure(new AddApptCommand(INDEX_SECOND_PATIENT, CHECKUP, FIXED_CLOCK), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        AddApptCommand command = new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP, FIXED_CLOCK);
        Appointment otherAppointment = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Lim", "");

        // same values -> returns true
        assertTrue(command.equals(new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP, FIXED_CLOCK)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different index, appointment or clock -> returns false
        assertFalse(command.equals(new AddApptCommand(INDEX_SECOND_PATIENT, CHECKUP, FIXED_CLOCK)));
        assertFalse(command.equals(new AddApptCommand(INDEX_FIRST_PATIENT, otherAppointment, FIXED_CLOCK)));
        assertFalse(command.equals(new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP)));
    }

    @Test
    public void toStringMethod() {
        AddApptCommand command = new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP, FIXED_CLOCK);
        String expected = AddApptCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT + ", toAdd=" + CHECKUP + "}";
        assertEquals(expected, command.toString());
    }

    /**
     * Asserts that adding {@code toAdd} to {@code patient}, the only patient in the model,
     * changes it to {@code expectedPatient}, shows it in the detail panel and shows {@code expectedMessage}.
     */
    private static void assertAddSuccess(Patient patient, Appointment toAdd, Patient expectedPatient,
            String expectedMessage) {
        Model model = new ModelManager();
        model.addPatient(patient);
        Model expectedModel = new ModelManager();
        expectedModel.addPatient(expectedPatient);
        expectedModel.setViewedPatient(expectedPatient);
        assertCommandSuccess(new AddApptCommand(INDEX_FIRST_PATIENT, toAdd, FIXED_CLOCK), model,
                expectedMessage, expectedModel);
    }
}
