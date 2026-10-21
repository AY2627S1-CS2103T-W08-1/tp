package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * Cancels an appointment of a patient identified by their displayed index.
 */
public class CancelApptCommand extends Command {

    public static final String COMMAND_WORD = "cancelappt";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Cancels an appointment of the patient "
            + "identified by the index number used in the displayed patient list.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "APPT_INDEX (the appointment's number in the detail panel, a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 2";

    public static final String MESSAGE_SUCCESS = "Cancelled appointment %1$d of %2$s: %3$s";

    private final Index targetIndex;
    private final Index appointmentIndex;
    private final Clock clock;

    /**
     * Creates a {@code CancelApptCommand} that removes the appointment at {@code appointmentIndex}
     * from the patient at {@code targetIndex}, numbering appointments as the detail panel does
     * at the current time of the system clock.
     */
    public CancelApptCommand(Index targetIndex, Index appointmentIndex) {
        this(targetIndex, appointmentIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code CancelApptCommand} that uses {@code clock} for the current date and time.
     */
    public CancelApptCommand(Index targetIndex, Index appointmentIndex, Clock clock) {
        requireNonNull(targetIndex);
        requireNonNull(appointmentIndex);
        requireNonNull(clock);
        this.targetIndex = targetIndex;
        this.appointmentIndex = appointmentIndex;
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
        List<Appointment> updatedAppointments =
                new ArrayList<>(patientToEdit.getAppointmentsUpcomingFirst(LocalDateTime.now(clock)));
        if (appointmentIndex.getZeroBased() >= updatedAppointments.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        }

        Appointment cancelledAppointment = updatedAppointments.remove(appointmentIndex.getZeroBased());
        Patient editedPatient = patientToEdit.withAppointments(updatedAppointments);
        model.setPatient(patientToEdit, editedPatient);
        model.setViewedPatient(editedPatient);
        String message = String.format(MESSAGE_SUCCESS, appointmentIndex.getOneBased(), patientToEdit.getName(),
                cancelledAppointment);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof CancelApptCommand otherCancelApptCommand)) {
            return false;
        }

        return targetIndex.equals(otherCancelApptCommand.targetIndex)
                && appointmentIndex.equals(otherCancelApptCommand.appointmentIndex)
                && clock.equals(otherCancelApptCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("appointmentIndex", appointmentIndex)
                .toString();
    }
}
