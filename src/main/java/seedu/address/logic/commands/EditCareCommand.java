package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * Replaces one care instruction identified by its number in the detail panel.
 */
public class EditCareCommand extends Command {

    public static final String COMMAND_WORD = "editcare";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX CI_INDEX "
            + PREFIX_CARE_INSTRUCTION + "NEW_TEXT";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a patient's care instruction.\n"
            + "Parameters: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 2 " + PREFIX_CARE_INSTRUCTION + "Check blood sugar after lunch";
    public static final String MESSAGE_BLANK_CARE_INSTRUCTION = "Care instructions cannot be blank.";
    public static final String MESSAGE_DUPLICATE_CARE_INSTRUCTION =
            "This care instruction already exists for this patient.";
    public static final String MESSAGE_SUCCESS = "Updated care instruction for %1$s: %2$s";

    private final Index patientIndex;
    private final Index careInstructionIndex;
    private final String careInstruction;

    /**
     * Creates a command for the two displayed indices and replacement text.
     */
    public EditCareCommand(Index patientIndex, Index careInstructionIndex, String careInstruction) {
        requireAllNonNull(patientIndex, careInstructionIndex, careInstruction);
        this.patientIndex = patientIndex;
        this.careInstructionIndex = careInstructionIndex;
        this.careInstruction = normalizeInstruction(careInstruction);
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
        if (careInstruction.isBlank()) {
            throw new CommandException(MESSAGE_BLANK_CARE_INSTRUCTION);
        }
        for (int i = 0; i < careInstructions.size(); i++) {
            if (i != careInstructionIndex.getZeroBased()
                    && normalizeInstruction(careInstructions.get(i)).equalsIgnoreCase(careInstruction)) {
                throw new CommandException(MESSAGE_DUPLICATE_CARE_INSTRUCTION);
            }
        }
        careInstructions.set(careInstructionIndex.getZeroBased(), careInstruction);
        Patient editedPatient = patient.withCareInstructions(careInstructions);
        model.setPatient(patient, editedPatient);
        model.setViewedPatient(editedPatient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName(), careInstruction));
    }

    private static String normalizeInstruction(String instruction) {
        return instruction.trim().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCareCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && careInstructionIndex.equals(otherCommand.careInstructionIndex)
                && careInstruction.equals(otherCommand.careInstruction);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("careInstructionIndex", careInstructionIndex)
                .add("careInstruction", careInstruction)
                .toString();
    }
}
