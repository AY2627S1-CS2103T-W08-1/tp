package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * Removes one care instruction identified by its number in the detail panel.
 */
public class DeleteCareCommand extends Command {

    public static final String COMMAND_WORD = "deletecare";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX CI_INDEX";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a patient's care instruction.\n"
            + "Format: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_SUCCESS = "Deleted care instruction for %1$s: %2$s";

    private final Index patientIndex;
    private final Index careInstructionIndex;

    /**
     * Creates a command for the two displayed indices.
     */
    public DeleteCareCommand(Index patientIndex, Index careInstructionIndex) {
        requireAllNonNull(patientIndex, careInstructionIndex);
        this.patientIndex = patientIndex;
        this.careInstructionIndex = careInstructionIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> patients = model.getFilteredPatientList();
        if (patientIndex.getZeroBased() >= patients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patient = patients.get(patientIndex.getZeroBased());
        List<String> careInstructions = new ArrayList<>(patient.getCareInstructions());
        if (careInstructionIndex.getZeroBased() >= careInstructions.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CARE_INSTRUCTION_INDEX);
        }
        String removed = careInstructions.remove(careInstructionIndex.getZeroBased());
        Patient editedPatient = patient.withCareInstructions(careInstructions);
        model.setPatient(patient, editedPatient);
        model.setViewedPatient(editedPatient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName(), removed));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCareCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && careInstructionIndex.equals(otherCommand.careInstructionIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("careInstructionIndex", careInstructionIndex)
                .toString();
    }
}
