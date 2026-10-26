package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_CLASH;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_APPOINTMENT;
import static seedu.address.logic.Messages.MESSAGE_PAST_APPOINTMENT;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.createModelWithAppointments;
import static seedu.address.logic.commands.CommandTestUtil.createViewedModelWithAppointments;
import static seedu.address.logic.commands.EditApptCommand.MESSAGE_SUCCESS;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AdjustableClock;
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
    public void execute_modelLosesEditedAppointment_throwsAssertionError() {
        // a model that does not show the edited appointment breaks the command's assumption
        Model model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK) {
            @Override
            public List<Appointment> getAppointmentsAsShown(Patient patient) {
                return patient.getAppointments().contains(CHECKUP) ? super.getAppointmentsAsShown(patient) : List.of();
            }
        };
        model.addPatient(new PatientBuilder(ALICE).withAppointments(CHECKUP).build());
        EditApptCommand command = createCommand(Optional.empty(), Optional.of(NEW_TIME), Optional.empty(),
                Optional.empty());

        assertThrows(AssertionError.class, () -> command.execute(model));
    }

    @Test
    public void execute_localeWithOtherDigits_givesNumberInPanelDigits() {
        Locale defaultLocale = Locale.getDefault();
        try {
            // String.format writes 1 as an Arabic-Indic digit in Egyptian Arabic unless given Locale.ROOT
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            Appointment editedAppointment = new Appointment(CHECKUP.getDate(), NEW_TIME, "Dr Tan", "Fasting");
            assertCommandSuccess(createCommand(Optional.empty(), Optional.of(NEW_TIME), Optional.empty(),
                    Optional.empty()), createModelWithAppointments(CHECKUP),
                    "Edited appointment 1 of " + ALICE.getName() + ": " + editedAppointment,
                    createViewedModelWithAppointments(editedAppointment));
        } finally {
            Locale.setDefault(defaultLocale);
        }
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
    public void execute_pastAndUpcomingAppointments_numbersUpcomingFirst() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Dr Lee", "");
        Appointment editedCheckup = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", "Moved");
        EditApptCommand command = createCommand(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of("Moved"));

        assertCommandSuccess(command, createModelWithAppointments(past, CHECKUP),
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), editedCheckup),
                createViewedModelWithAppointments(past, editedCheckup));
    }

    @Test
    public void execute_appointmentPassesWhileShown_editsAppointmentWithShownNumber() {
        AdjustableClock clock = new AdjustableClock(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
        Appointment soon = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 1), "Dr Tan", "");
        Model model = createModelWithAppointments(clock, soon, CARDIOLOGY);
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        clock.advance(Duration.ofMinutes(2));
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("Seen"), clock);
        Appointment editedSoon = new Appointment(soon.getDate(), soon.getTime(), "Dr Tan", "Seen");

        // number 1 is still the appointment at 12:01 shown in the panel, which is now past and numbered 2
        assertCommandSuccess(command, model, String.format(MESSAGE_SUCCESS, 2, ALICE.getName(), editedSoon)
                + "\n" + MESSAGE_PAST_APPOINTMENT, createViewedModelWithAppointments(editedSoon, CARDIOLOGY));
    }

    @Test
    public void execute_newDateMovesAppointment_showsNewNumber() {
        LocalDate beforeCheckup = LocalDate.of(2026, 11, 1);
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(beforeCheckup), Optional.empty(), Optional.empty(), Optional.empty(), FIXED_CLOCK);
        Appointment movedCardiology = new Appointment(beforeCheckup, CARDIOLOGY.getTime(), "Cardiology clinic", "");

        assertCommandSuccess(command, createModelWithAppointments(CHECKUP, CARDIOLOGY),
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), movedCardiology),
                createViewedModelWithAppointments(CHECKUP, movedCardiology));
    }

    @Test
    public void execute_sameTimeAsOtherAppointment_editsWithClashWarning() {
        EditApptCommand command = new EditApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(CHECKUP.getDate()), Optional.of(CHECKUP.getTime()), Optional.empty(),
                Optional.empty(), FIXED_CLOCK);
        Appointment editedAppointment =
                new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Cardiology clinic", "");

        assertCommandSuccess(command, createModelWithAppointments(CHECKUP, CARDIOLOGY),
                String.format(MESSAGE_SUCCESS, 2, ALICE.getName(), editedAppointment)
                + "\n" + String.format(MESSAGE_CLASH, ALICE.getName(), "Dr Tan"),
                createViewedModelWithAppointments(CHECKUP, editedAppointment));
    }

    @Test
    public void execute_appointmentWithClash_keepsItsNumber() {
        Appointment clash = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Lim", "");
        Appointment editedCheckup = new Appointment(CHECKUP.getDate(), CHECKUP.getTime(), "Dr Tan", "");

        EditApptCommand command = createCommand(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(""));

        assertCommandSuccess(command, createModelWithAppointments(CHECKUP, clash),
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), editedCheckup)
                + "\n" + String.format(MESSAGE_CLASH, ALICE.getName(), "Dr Lim"),
                createViewedModelWithAppointments(editedCheckup, clash));
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
                String.format(MESSAGE_SUCCESS, 1, ALICE.getName(), editedAppointment) + expectedWarnings,
                createViewedModelWithAppointments(editedAppointment));
    }
}
