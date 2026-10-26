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
 * Removes an allergen identified by its number in the detail panel.
 */
public class DeleteAllergyCommand extends Command {

    public static final String COMMAND_WORD = "deleteallergy";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX ALLERGY_INDEX";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a patient's allergen.\n"
            + "Parameters: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_SUCCESS = "Deleted allergen for %1$s: %2$s";

    private final Index patientIndex;
    private final Index allergyIndex;

    /**
     * Creates a command for the two displayed indices.
     */
    public DeleteAllergyCommand(Index patientIndex, Index allergyIndex) {
        requireAllNonNull(patientIndex, allergyIndex);
        this.patientIndex = patientIndex;
        this.allergyIndex = allergyIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> patients = model.getFilteredPatientList();
        if (patientIndex.getZeroBased() >= patients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patient = patients.get(patientIndex.getZeroBased());
        if (allergyIndex.getZeroBased() >= patient.getAllergens().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_ALLERGEN_INDEX);
        }
        List<String> allergens = new ArrayList<>(patient.getAllergens());
        String removed = allergens.remove(allergyIndex.getZeroBased());
        Patient editedPatient = patient.withAllergens(allergens);
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
        if (!(other instanceof DeleteAllergyCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && allergyIndex.equals(otherCommand.allergyIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("allergyIndex", allergyIndex)
                .toString();
    }
}
