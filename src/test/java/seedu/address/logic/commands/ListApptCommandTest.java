package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.ListApptCommand.HEADING_PAST;
import static seedu.address.logic.commands.ListApptCommand.HEADING_UPCOMING;
import static seedu.address.logic.commands.ListApptCommand.MESSAGE_ARCHIVED_PATIENT;
import static seedu.address.logic.commands.ListApptCommand.MESSAGE_NO_PATIENT_APPOINTMENTS;
import static seedu.address.logic.commands.ListApptCommand.MESSAGE_NO_UPCOMING;
import static seedu.address.logic.commands.ListApptCommand.MESSAGE_PATIENT_APPOINTMENTS;
import static seedu.address.logic.commands.ListApptCommand.MESSAGE_UPCOMING;
import static seedu.address.logic.commands.ListApptCommand.NO_ENTRIES;
import static seedu.address.model.Model.PREDICATE_SHOW_ARCHIVED_PATIENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CARDIOLOGY;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.CARL;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AdjustableClock;
import seedu.address.testutil.PatientBuilder;

public class ListApptCommandTest {

    /** One minute before the fixed clock's current time, so already in the past. */
    private static final Appointment ONE_MINUTE_AGO =
            new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(11, 59), "Dr Ong", "");
    /** At the fixed clock's current time, so still upcoming. */
    private static final Appointment NOW =
            new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 0), "Dr Ong", "");

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListApptCommand(null));
        assertThrows(NullPointerException.class, () -> new ListApptCommand(Optional.empty(), null));
    }

    @Test
    public void execute_noIndex_listsUpcomingAppointmentsSoonestFirst() {
        String expectedMessage = MESSAGE_UPCOMING
                + "\n" + BENSON.getName() + ": " + NOW
                + "\n" + ALICE.getName() + ": " + CHECKUP
                + "\n" + BENSON.getName() + ": " + CARDIOLOGY;
        assertCommandSuccess(new ListApptCommand(Optional.empty(), FIXED_CLOCK), createModel(),
                expectedMessage, createModel());
    }

    @Test
    public void execute_noIndex_skipsArchivedPatients() {
        Patient alice = new PatientBuilder(ALICE).withAppointments(ONE_MINUTE_AGO, CHECKUP).build();
        Patient archivedBenson = new PatientBuilder(BENSON).withAppointments(NOW, CARDIOLOGY).withArchived(true)
                .build();
        String expectedMessage = MESSAGE_UPCOMING + "\n" + ALICE.getName() + ": " + CHECKUP;

        assertCommandSuccess(new ListApptCommand(Optional.empty(), FIXED_CLOCK),
                createModelWith(alice, archivedBenson), expectedMessage, createModelWith(alice, archivedBenson));
    }

    @Test
    public void execute_noUpcomingAppointments_showsNoUpcomingMessage() {
        Patient patientWithPastAppointment =
                new PatientBuilder(ALICE).withAppointments(ONE_MINUTE_AGO).build();
        assertCommandSuccess(new ListApptCommand(Optional.empty(), FIXED_CLOCK),
                createModelWith(patientWithPastAppointment, CARL), MESSAGE_NO_UPCOMING,
                createModelWith(patientWithPastAppointment, CARL));
    }

    @Test
    public void execute_index_listsUpcomingThenPastWithPanelNumbers() {
        String expectedMessage = String.format(MESSAGE_PATIENT_APPOINTMENTS, ALICE.getName())
                + "\n" + HEADING_UPCOMING + "\n1. " + CHECKUP
                + "\n" + HEADING_PAST + "\n2. " + ONE_MINUTE_AGO;
        ListApptCommand command = new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), FIXED_CLOCK);
        assertCommandSuccess(command, createModel(), expectedMessage,
                showPatient(createModel(), INDEX_FIRST_PATIENT));
    }

    @Test
    public void execute_indexWithNoPastAppointments_showsNoneForPast() {
        String expectedMessage = String.format(MESSAGE_PATIENT_APPOINTMENTS, BENSON.getName())
                + "\n" + HEADING_UPCOMING + "\n1. " + NOW + "\n2. " + CARDIOLOGY
                + "\n" + HEADING_PAST + NO_ENTRIES;
        ListApptCommand command = new ListApptCommand(Optional.of(INDEX_SECOND_PATIENT), FIXED_CLOCK);
        assertCommandSuccess(command, createModel(), expectedMessage,
                showPatient(createModel(), INDEX_SECOND_PATIENT));
    }

    @Test
    public void execute_indexOfPatientWithNoAppointments_showsNoAppointmentsMessage() {
        ListApptCommand command = new ListApptCommand(Optional.of(INDEX_THIRD_PATIENT), FIXED_CLOCK);
        assertCommandSuccess(command, createModel(),
                String.format(MESSAGE_NO_PATIENT_APPOINTMENTS, CARL.getName()),
                showPatient(createModel(), INDEX_THIRD_PATIENT));
    }

    @Test
    public void execute_indexOfArchivedPatient_throwsCommandException() {
        Model model = createModelWith(new PatientBuilder(ALICE).withArchived(true).build());
        model.updateFilteredPatientList(PREDICATE_SHOW_ARCHIVED_PATIENTS);

        assertCommandFailure(new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), FIXED_CLOCK), model,
                MESSAGE_ARCHIVED_PATIENT);
    }

    @Test
    public void execute_indexThenAppointmentPasses_laterCommandsUseListedNumbers() throws Exception {
        AdjustableClock clock = new AdjustableClock(Instant.parse("2026-10-18T12:00:30Z"), ZoneOffset.UTC);
        Appointment soon = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 1), "Dr Tan", "");
        Model model = new ModelManager(new AddressBook(), new UserPrefs(), clock);
        model.addPatient(new PatientBuilder(ALICE).withAppointments(ONE_MINUTE_AGO, soon, CHECKUP).build());
        String listed = new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), clock).execute(model)
                .getFeedbackToUser();
        assertTrue(listed.contains("1. " + soon));
        clock.advance(Duration.ofMinutes(2));

        // number 1 is still the appointment listed as 1, although it has now passed
        new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model);
        assertEquals(List.of(ONE_MINUTE_AGO, CHECKUP),
                model.getFilteredPatientList().get(0).getAppointments());
    }

    @Test
    public void execute_indexOutOfRange_throwsCommandException() {
        ListApptCommand command = new ListApptCommand(Optional.of(Index.fromOneBased(4)), FIXED_CLOCK);
        assertCommandFailure(command, createModel(), Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ListApptCommand command = new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), FIXED_CLOCK);

        // same values -> returns true
        assertTrue(command.equals(new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), FIXED_CLOCK)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different index or clock -> returns false
        assertFalse(command.equals(new ListApptCommand(Optional.of(INDEX_SECOND_PATIENT), FIXED_CLOCK)));
        assertFalse(command.equals(new ListApptCommand(Optional.empty(), FIXED_CLOCK)));
        assertFalse(command.equals(new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT))));
    }

    @Test
    public void toStringMethod() {
        ListApptCommand command = new ListApptCommand(Optional.of(INDEX_FIRST_PATIENT), FIXED_CLOCK);
        String expected = ListApptCommand.class.getCanonicalName()
                + "{targetIndex=" + Optional.of(INDEX_FIRST_PATIENT) + "}";
        assertEquals(expected, command.toString());
    }

    /**
     * Returns a model with Alice (an appointment a minute ago and {@code CHECKUP}), Benson (one now and
     * {@code CARDIOLOGY}) and Carl (none), in that order.
     */
    private static Model createModel() {
        return createModelWith(new PatientBuilder(ALICE).withAppointments(ONE_MINUTE_AGO, CHECKUP).build(),
                new PatientBuilder(BENSON).withAppointments(NOW, CARDIOLOGY).build(), CARL);
    }

    /**
     * Returns a model with the given {@code patients}, in that order.
     */
    private static Model createModelWith(Patient... patients) {
        Model model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        for (Patient patient : patients) {
            model.addPatient(patient);
        }
        return model;
    }

    /**
     * Returns {@code model} with the patient at {@code index} of its displayed list shown in the panel.
     */
    private static Model showPatient(Model model, Index index) {
        model.setViewedPatient(model.getFilteredPatientList().get(index.getZeroBased()));
        return model;
    }
}
