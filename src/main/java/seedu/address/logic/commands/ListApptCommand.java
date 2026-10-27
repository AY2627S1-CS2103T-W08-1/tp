package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PATIENTS;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * Lists the upcoming appointments of all active (not archived) patients, or every appointment of a patient
 * identified by their displayed index.
 */
public class ListApptCommand extends Command {

    public static final String COMMAND_WORD = "listappt";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists the upcoming appointments of all "
            + "active patients, soonest first. With an INDEX, lists every appointment of the patient "
            + "identified by the index number used in the displayed patient list, and shows them in the panel.\n"
            + "Parameters: [INDEX] (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_UPCOMING = "Upcoming appointments:";
    public static final String MESSAGE_NO_UPCOMING = "No upcoming appointments.";
    public static final String MESSAGE_PATIENT_APPOINTMENTS = "Appointments for %1$s:";
    public static final String MESSAGE_NO_PATIENT_APPOINTMENTS = "%1$s has no appointments.";
    public static final String HEADING_UPCOMING = "Upcoming:";
    public static final String HEADING_PAST = "Past:";
    public static final String NO_ENTRIES = " none";

    private final Optional<Index> targetIndex;
    private final Clock clock;

    /**
     * Creates a {@code ListApptCommand} that lists every appointment of the patient at {@code targetIndex},
     * or the upcoming appointments of all patients if it is empty, using the system clock for the current
     * date and time.
     */
    public ListApptCommand(Optional<Index> targetIndex) {
        this(targetIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code ListApptCommand} that uses {@code clock} for the current date and time when listing the
     * upcoming appointments of all patients. With an index, the model decides which appointments are upcoming.
     */
    public ListApptCommand(Optional<Index> targetIndex, Clock clock) {
        requireNonNull(targetIndex);
        requireNonNull(clock);
        this.targetIndex = targetIndex;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (targetIndex.isEmpty()) {
            List<Patient> activePatients = model.getAddressBook().getPatientList().stream()
                    .filter(PREDICATE_SHOW_ACTIVE_PATIENTS).toList();
            return new CommandResult(listUpcomingAppointments(activePatients, LocalDateTime.now(clock)));
        }

        List<Patient> lastShownList = model.getFilteredPatientList();
        if (targetIndex.get().getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patient = lastShownList.get(targetIndex.get().getZeroBased());

        // Shows the patient, so that the numbers listed stay the ones that later commands use.
        model.setViewedPatient(patient);
        // Splits them at the time the panel orders them, so each is under the heading the panel gives it.
        return new CommandResult(listAppointmentsOf(patient, model.getAppointmentsAsShown(patient),
                model.getAppointmentsOrderedAt(patient)));
    }

    /**
     * Returns the upcoming appointments of {@code patients}, soonest first, one per line after the
     * patient's name and NRIC, since two patients can have the same name.
     * Appointments at the same date and time keep the order of {@code patients}.
     */
    private static String listUpcomingAppointments(List<Patient> patients, LocalDateTime now) {
        List<Map.Entry<Appointment, Patient>> upcomingAppointments = new ArrayList<>();
        for (Patient patient : patients) {
            for (Appointment appointment : patient.getAppointments()) {
                if (!appointment.isBefore(now)) {
                    upcomingAppointments.add(Map.entry(appointment, patient));
                }
            }
        }
        if (upcomingAppointments.isEmpty()) {
            return MESSAGE_NO_UPCOMING;
        }

        upcomingAppointments.sort(Map.Entry.comparingByKey());
        List<String> lines = new ArrayList<>();
        lines.add(MESSAGE_UPCOMING);
        for (Map.Entry<Appointment, Patient> entry : upcomingAppointments) {
            Patient patient = entry.getValue();
            lines.add(patient.getName() + " (" + patient.getNric() + "): " + entry.getKey());
        }
        return String.join("\n", lines);
    }

    /**
     * Returns the {@code appointments} of {@code patient}, in the order and with the numbers of the patient's
     * detail panel, under an upcoming and a past heading decided at {@code orderedAt}.
     */
    private static String listAppointmentsOf(Patient patient, List<Appointment> appointments,
            LocalDateTime orderedAt) {
        if (appointments.isEmpty()) {
            return String.format(MESSAGE_NO_PATIENT_APPOINTMENTS, patient.getName());
        }

        List<String> upcomingLines = new ArrayList<>();
        List<String> pastLines = new ArrayList<>();
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            String line = Index.fromZeroBased(i).getOneBased() + ". " + appointment;
            if (appointment.isBefore(orderedAt)) {
                pastLines.add(line);
            } else {
                upcomingLines.add(line);
            }
        }
        return String.format(MESSAGE_PATIENT_APPOINTMENTS, patient.getName())
                + formatGroup(HEADING_UPCOMING, upcomingLines)
                + formatGroup(HEADING_PAST, pastLines);
    }

    /**
     * Returns {@code heading} on a new line, followed by {@code lines}, one per line, or by " none" if there
     * are no lines.
     */
    private static String formatGroup(String heading, List<String> lines) {
        if (lines.isEmpty()) {
            return "\n" + heading + NO_ENTRIES;
        }
        return "\n" + heading + "\n" + String.join("\n", lines);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ListApptCommand otherListApptCommand)) {
            return false;
        }

        return targetIndex.equals(otherListApptCommand.targetIndex)
                && clock.equals(otherListApptCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
