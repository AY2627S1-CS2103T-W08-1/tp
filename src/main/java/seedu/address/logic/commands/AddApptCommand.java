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
    public static final String MESSAGE_DUPLICATE_APPOINTMENT =
            "This appointment already exists for this patient.";
    public static final String MESSAGE_PAST_APPOINTMENT = "Note: this appointment is in the past.";
    public static final String MESSAGE_CLASH =
            "Warning: %1$s already has an appointment at this date and time (with %2$s).";

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
        if (patientToEdit.getAppointments().stream().anyMatch(toAdd::isSameAppointment)) {
            throw new CommandException(MESSAGE_DUPLICATE_APPOINTMENT);
        }

        List<Appointment> updatedAppointments = new ArrayList<>(patientToEdit.getAppointments());
        updatedAppointments.add(toAdd);
        Patient editedPatient = new Patient(patientToEdit.getName(), patientToEdit.getNric(),
                patientToEdit.getDob(), patientToEdit.getPhone(), patientToEdit.getAddress(),
                patientToEdit.getMedications(), patientToEdit.getAllergens(),
                patientToEdit.getCareInstructions(), patientToEdit.getContacts(), updatedAppointments,
                patientToEdit.isArchived());
        model.setPatient(patientToEdit, editedPatient);

        return new CommandResult(buildSuccessMessage(patientToEdit));
    }

    /**
     * Returns the success message for adding the appointment to {@code patient}, followed by a note
     * if the appointment is in the past and a warning for each appointment at the same date and time.
     */
    private String buildSuccessMessage(Patient patient) {
        String message = String.format(MESSAGE_SUCCESS, patient.getName(), toAdd);
        boolean isInPast = toAdd.isBefore(LocalDateTime.now(clock));
        if (isInPast) {
            message += "\n" + MESSAGE_PAST_APPOINTMENT;
        }
        for (Appointment appointment : patient.getAppointments()) {
            if (toAdd.isAtSameTimeAs(appointment)) {
                message += "\n" + String.format(MESSAGE_CLASH, patient.getName(), appointment.getProvider());
            }
        }
        return message;
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
