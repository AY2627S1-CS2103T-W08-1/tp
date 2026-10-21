package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Lists the medication times of today, for all patients or for the patient at a displayed index.
 * Each dose is listed on its own line, sorted by time and then by patient name.
 */
public class DueCommand extends Command {

    public static final String COMMAND_WORD = "due";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists today's medication times, sorted by time, "
            + "for all patients, or only for the patient identified by the index number used in the displayed "
            + "patient list. Medications without times are not listed. A time earlier than the current time is "
            + "marked" + Messages.MESSAGE_DUE_EARLIER_TODAY_SUFFIX + ".\n"
            + "Parameters: [INDEX] (must be a positive integer)\n"
            + "Examples: " + COMMAND_WORD + "\n"
            + "          " + COMMAND_WORD + " 1";

    private static final String LINE_SEPARATOR = "\n";

    private final Optional<Index> patientIndex;
    private final Clock clock;

    /**
     * Creates a {@code DueCommand} that lists the doses of all patients, taking the current time from
     * {@code clock}.
     */
    public DueCommand(Clock clock) {
        requireNonNull(clock);
        this.patientIndex = Optional.empty();
        this.clock = clock;
    }

    /**
     * Creates a {@code DueCommand} that lists the doses of the patient at {@code patientIndex} in the displayed
     * patient list, taking the current time from {@code clock}.
     */
    public DueCommand(Index patientIndex, Clock clock) {
        requireNonNull(patientIndex);
        requireNonNull(clock);
        this.patientIndex = Optional.of(patientIndex);
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> candidates = getCandidatePatients(model);
        LocalTime now = LocalTime.now(clock).truncatedTo(ChronoUnit.MINUTES);

        List<Dose> doses = keepActive(candidates.stream())
                .flatMap(DueCommand::toDoses)
                .sorted(Comparator.comparing(Dose::time)
                        .thenComparing(dose -> dose.patient().getName().fullName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(dose -> dose.patient().getNric().value))
                .collect(Collectors.toList());

        if (doses.isEmpty()) {
            return new CommandResult(createNothingDueMessage(candidates));
        }
        return new CommandResult(doses.stream()
                .map(dose -> formatDose(dose, now))
                .collect(Collectors.joining(LINE_SEPARATOR)));
    }

    /**
     * Returns the patients to list: all patients, or only the patient at the index of this command.
     *
     * @throws CommandException if the index is not in the displayed patient list.
     */
    private List<Patient> getCandidatePatients(Model model) throws CommandException {
        if (patientIndex.isEmpty()) {
            return new ArrayList<>(model.getAddressBook().getPatientList());
        }

        List<Patient> lastShownList = model.getFilteredPatientList();
        if (patientIndex.get().getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        return List.of(lastShownList.get(patientIndex.get().getZeroBased()));
    }

    /**
     * Returns only the patients whose medications are to be listed.
     * Patients that should not be listed are left out here.
     */
    private static Stream<Patient> keepActive(Stream<Patient> patients) {
        return patients;
    }

    private static Stream<Dose> toDoses(Patient patient) {
        return patient.getMedications().stream()
                .flatMap(medication -> medication.getTimes().stream()
                        .map(time -> new Dose(time, patient, medication)));
    }

    private static String formatDose(Dose dose, LocalTime now) {
        String suffix = dose.time().isBefore(now) ? Messages.MESSAGE_DUE_EARLIER_TODAY_SUFFIX : "";
        return String.format(Messages.MESSAGE_DUE_ENTRY, Medication.formatTime(dose.time()),
                dose.patient().getName(), dose.medication().getDescription()) + suffix;
    }

    private String createNothingDueMessage(List<Patient> candidates) {
        if (patientIndex.isPresent()) {
            return String.format(Messages.MESSAGE_NOTHING_DUE_TODAY_FOR_PATIENT, candidates.get(0).getName());
        }
        return Messages.MESSAGE_NOTHING_DUE_TODAY;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DueCommand otherDueCommand)) {
            return false;
        }

        return patientIndex.equals(otherDueCommand.patientIndex) && clock.equals(otherDueCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("clock", clock)
                .toString();
    }

    /**
     * A single time at which a patient takes a medication.
     */
    private record Dose(LocalTime time, Patient patient, Medication medication) { }
}
