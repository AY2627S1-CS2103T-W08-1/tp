package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WITH;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * Edits an appointment of a patient identified by their displayed index.
 */
public class EditApptCommand extends Command {

    public static final String COMMAND_WORD = "editappt";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits an appointment of the patient "
            + "identified by the index number used in the displayed patient list. "
            + "Only the fields given are changed; "
            + PREFIX_NOTE + " with nothing after it clears the note.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "APPT_INDEX (the appointment's number in the detail panel, a positive integer) "
            + "[" + PREFIX_DATE + "DATE] "
            + "[" + PREFIX_TIME + "TIME] "
            + "[" + PREFIX_WITH + "WITH] "
            + "[" + PREFIX_NOTE + "NOTE]\n"
            + "Example: " + COMMAND_WORD + " 1 2 " + PREFIX_TIME + "10:00 " + PREFIX_NOTE;

    public static final String MESSAGE_SUCCESS = "Edited appointment for %1$s: %2$s";
    public static final String MESSAGE_NOT_EDITED = "At least one of " + PREFIX_DATE + ", " + PREFIX_TIME
            + ", " + PREFIX_WITH + " or " + PREFIX_NOTE + " must be provided.";

    private final Index targetIndex;
    private final Index appointmentIndex;
    private final Optional<LocalDate> newDate;
    private final Optional<LocalTime> newTime;
    private final Optional<String> newProvider;
    private final Optional<String> newNote;
    private final Clock clock;

    /**
     * Creates an {@code EditApptCommand} that edits the appointment at {@code appointmentIndex} of the
     * patient at {@code targetIndex}, using the system clock to tell whether it is in the past.
     * An empty {@code Optional} leaves that field unchanged.
     */
    public EditApptCommand(Index targetIndex, Index appointmentIndex, Optional<LocalDate> newDate,
            Optional<LocalTime> newTime, Optional<String> newProvider, Optional<String> newNote) {
        this(targetIndex, appointmentIndex, newDate, newTime, newProvider, newNote,
                Clock.systemDefaultZone());
    }

    /**
     * Creates an {@code EditApptCommand} that uses {@code clock} for the current date and time.
     */
    public EditApptCommand(Index targetIndex, Index appointmentIndex, Optional<LocalDate> newDate,
            Optional<LocalTime> newTime, Optional<String> newProvider, Optional<String> newNote,
            Clock clock) {
        requireNonNull(targetIndex);
        requireNonNull(appointmentIndex);
        requireNonNull(newDate);
        requireNonNull(newTime);
        requireNonNull(newProvider);
        requireNonNull(newNote);
        requireNonNull(clock);
        assert newDate.isPresent() || newTime.isPresent() || newProvider.isPresent() || newNote.isPresent()
                : "the parser must reject an edit with no fields";
        this.targetIndex = targetIndex;
        this.appointmentIndex = appointmentIndex;
        this.newDate = newDate;
        this.newTime = newTime;
        this.newProvider = newProvider;
        this.newNote = newNote;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> lastShownList = model.getFilteredPatientList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }

        Patient patientToEdit = lastShownList.get(targetIndex.getZeroBased());
        List<Appointment> otherAppointments =
                new ArrayList<>(patientToEdit.getAppointmentsUpcomingFirst(LocalDateTime.now(clock)));
        if (appointmentIndex.getZeroBased() >= otherAppointments.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        }

        Appointment appointmentToEdit = otherAppointments.remove(appointmentIndex.getZeroBased());
        Appointment editedAppointment = createEditedAppointment(appointmentToEdit);
        if (otherAppointments.stream().anyMatch(editedAppointment::isSameAppointment)) {
            throw new CommandException(AddApptCommand.MESSAGE_DUPLICATE_APPOINTMENT);
        }

        // Put it back in its place, so appointments at the same date and time keep their numbers.
        List<Appointment> updatedAppointments = new ArrayList<>(otherAppointments);
        updatedAppointments.add(appointmentIndex.getZeroBased(), editedAppointment);
        model.setPatient(patientToEdit, patientToEdit.withAppointments(updatedAppointments));

        String message = String.format(MESSAGE_SUCCESS, patientToEdit.getName(), editedAppointment)
                + AddApptCommand.buildWarnings(patientToEdit.getName(), editedAppointment, otherAppointments,
                        clock);
        return new CommandResult(message);
    }

    /**
     * Returns a copy of {@code appointmentToEdit} with the fields given in this command changed.
     */
    private Appointment createEditedAppointment(Appointment appointmentToEdit) {
        LocalDate date = newDate.orElse(appointmentToEdit.getDate());
        LocalTime time = newTime.orElse(appointmentToEdit.getTime());
        String provider = newProvider.orElse(appointmentToEdit.getProvider());
        String note = newNote.orElse(appointmentToEdit.getNote());
        return new Appointment(date, time, provider, note);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EditApptCommand otherEditApptCommand)) {
            return false;
        }

        return targetIndex.equals(otherEditApptCommand.targetIndex)
                && appointmentIndex.equals(otherEditApptCommand.appointmentIndex)
                && newDate.equals(otherEditApptCommand.newDate)
                && newTime.equals(otherEditApptCommand.newTime)
                && newProvider.equals(otherEditApptCommand.newProvider)
                && newNote.equals(otherEditApptCommand.newNote)
                && clock.equals(otherEditApptCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("appointmentIndex", appointmentIndex)
                .add("newDate", newDate)
                .add("newTime", newTime)
                .add("newProvider", newProvider)
                .add("newNote", newNote)
                .toString();
    }
}
