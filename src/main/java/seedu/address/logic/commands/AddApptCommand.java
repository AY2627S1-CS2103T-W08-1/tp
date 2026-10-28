package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WITH;

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
 * Adds an appointment to a patient identified by their displayed index.
 */
public class AddApptCommand extends Command {

    public static final String COMMAND_WORD = "addappt";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an appointment to the patient "
            + "identified by the index number used in the displayed patient list.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_DATE + "DATE "
            + PREFIX_TIME + "TIME "
            + PREFIX_WITH + "WITH "
            + "[" + PREFIX_NOTE + "NOTE]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DATE + "2026-11-20 " + PREFIX_TIME + "09:30 "
            + PREFIX_WITH + "Dr Tan " + PREFIX_NOTE + "Bring blood test results";

    public static final String MESSAGE_SUCCESS = "New appointment for %1$s: %2$s";

    private final Index targetIndex;
    private final Appointment toAdd;
    private final Clock clock;

    /**
     * Creates an {@code AddApptCommand} that adds {@code appointment} to the patient at {@code targetIndex},
     * using the system clock to tell whether it is in the past.
     */
    public AddApptCommand(Index targetIndex, Appointment appointment) {
        this(targetIndex, appointment, Clock.systemDefaultZone());
    }

    /**
     * Creates an {@code AddApptCommand} that uses {@code clock} for the current date and time.
     */
    public AddApptCommand(Index targetIndex, Appointment appointment, Clock clock) {
        requireNonNull(targetIndex);
        requireNonNull(appointment);
        requireNonNull(clock);
        this.targetIndex = targetIndex;
        toAdd = appointment;
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
        if (patientToEdit.getDob().isAfter(toAdd.getDate())) {
            throw new CommandException(String.format(Messages.MESSAGE_APPOINTMENT_BEFORE_DOB,
                    patientToEdit.getDob()));
        }
        if (patientToEdit.getAppointments().stream().anyMatch(toAdd::isSameAppointment)) {
            throw new CommandException(Messages.MESSAGE_DUPLICATE_APPOINTMENT);
        }

        List<Appointment> updatedAppointments = new ArrayList<>(patientToEdit.getAppointments());
        updatedAppointments.add(toAdd);
        Patient editedPatient = patientToEdit.withAppointments(updatedAppointments);
        model.setPatient(patientToEdit, editedPatient);
        model.setViewedPatient(editedPatient);

        String message = String.format(MESSAGE_SUCCESS, patientToEdit.getName(), toAdd)
                + Messages.formatAppointmentWarnings(patientToEdit.getName(), toAdd,
                        patientToEdit.getAppointments(), LocalDateTime.now(clock));
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddApptCommand otherAddApptCommand)) {
            return false;
        }

        return targetIndex.equals(otherAddApptCommand.targetIndex)
                && toAdd.equals(otherAddApptCommand.toAdd)
                && clock.equals(otherAddApptCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("toAdd", toAdd)
                .toString();
    }
}
