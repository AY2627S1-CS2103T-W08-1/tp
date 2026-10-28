package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Deletes a medication of a patient.
 * The patient is identified by the displayed patient index and the medication by its number in the detail panel.
 */
public class DeleteMedCommand extends Command {

    public static final String COMMAND_WORD = "deletemed";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a medication of the patient identified "
            + "by the index number used in the displayed patient list. The medication is identified by its number "
            + "in the numbered medication list of the patient's details.\n"
            + "Parameters: INDEX (must be a positive integer) MED_INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 2";

    private final Index patientIndex;
    private final Index medicationIndex;

    /**
     * Creates a {@code DeleteMedCommand} to delete the medication at {@code medicationIndex} of the patient at
     * {@code patientIndex}.
     */
    public DeleteMedCommand(Index patientIndex, Index medicationIndex) {
        requireNonNull(patientIndex);
        requireNonNull(medicationIndex);
        this.patientIndex = patientIndex;
        this.medicationIndex = medicationIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> lastShownList = model.getFilteredPatientList();

        if (patientIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patientToEdit = lastShownList.get(patientIndex.getZeroBased());

        List<Medication> medications = new ArrayList<>(patientToEdit.getMedications());
        if (medicationIndex.getZeroBased() >= medications.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
        }

        Medication deletedMedication = medications.remove(medicationIndex.getZeroBased());
        Patient editedPatient = patientToEdit.withMedications(medications);
        model.setPatient(patientToEdit, editedPatient);
        model.setViewedPatient(editedPatient);

        return new CommandResult(String.format(Messages.MESSAGE_DELETE_MEDICATION_SUCCESS,
                medicationIndex.getOneBased(), patientToEdit.getName(), deletedMedication));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteMedCommand otherDeleteMedCommand)) {
            return false;
        }

        return patientIndex.equals(otherDeleteMedCommand.patientIndex)
                && medicationIndex.equals(otherDeleteMedCommand.medicationIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("medicationIndex", medicationIndex)
                .toString();
    }
}
