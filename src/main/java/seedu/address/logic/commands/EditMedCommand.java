package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Edits the description and/or the daily times of a medication of a patient.
 * The patient is identified by the displayed patient index and the medication by its number in the detail panel.
 */
public class EditMedCommand extends Command {

    public static final String COMMAND_WORD = "editmed";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a medication of the patient identified "
            + "by the index number used in the displayed patient list. The medication is identified by its number "
            + "in the numbered medication list of the patient's details.\n"
            + "Parameters: INDEX (must be a positive integer) MED_INDEX (must be a positive integer) "
            + "[" + PREFIX_MEDICATION + "DESCRIPTION] [" + PREFIX_TIME + "TIME]...\n"
            + "At least one of " + PREFIX_MEDICATION + " or " + PREFIX_TIME + " must be given. "
            + "TIME is in H:MM or HH:MM (24-hour). Any " + PREFIX_TIME + " given replaces all existing times; "
            + PREFIX_TIME + " with nothing after it clears them and cannot be combined with another "
            + PREFIX_TIME + ".\n"
            + "Example: " + COMMAND_WORD + " 1 2 " + PREFIX_MEDICATION + "Metformin 850mg "
            + PREFIX_TIME + "08:00 " + PREFIX_TIME + "20:00";

    private final Index patientIndex;
    private final Index medicationIndex;
    private final Optional<String> newDescription;
    private final Optional<List<LocalTime>> newTimes;

    /**
     * Creates an {@code EditMedCommand} for the medication at {@code medicationIndex} of the patient at
     * {@code patientIndex}. An empty {@code newDescription} or {@code newTimes} leaves that part unchanged;
     * a present but empty list of times clears the times. At least one of them must be present.
     */
    public EditMedCommand(Index patientIndex, Index medicationIndex, Optional<String> newDescription,
            Optional<List<LocalTime>> newTimes) {
        requireNonNull(patientIndex);
        requireNonNull(medicationIndex);
        requireNonNull(newDescription);
        requireNonNull(newTimes);
        assert newDescription.isPresent() || newTimes.isPresent() : "the parser must reject an edit with no changes";
        this.patientIndex = patientIndex;
        this.medicationIndex = medicationIndex;
        this.newDescription = newDescription;
        this.newTimes = newTimes.map(List::copyOf);
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

        Medication editedMedication = createEditedMedication(medications.get(medicationIndex.getZeroBased()));
        rejectDuplicateDescription(medications, editedMedication);
        medications.set(medicationIndex.getZeroBased(), editedMedication);

        Patient editedPatient = patientToEdit.withMedications(medications);
        model.setPatient(patientToEdit, editedPatient);
        model.setViewedPatient(editedPatient);

        return new CommandResult(String.format(Messages.MESSAGE_EDIT_MEDICATION_SUCCESS,
                medicationIndex.getOneBased(), patientToEdit.getName(), editedMedication));
    }

    /**
     * Returns a copy of {@code medicationToEdit} with the description and/or times of this command applied.
     */
    private Medication createEditedMedication(Medication medicationToEdit) {
        String description = newDescription.orElse(medicationToEdit.getDescription());
        List<LocalTime> times = newTimes.orElse(medicationToEdit.getTimes());
        return new Medication(description, times);
    }

    /**
     * Throws a {@code CommandException} if {@code editedMedication} has the same description as a medication in
     * {@code medications} other than the one being edited.
     */
    private void rejectDuplicateDescription(List<Medication> medications, Medication editedMedication)
            throws CommandException {
        for (int i = 0; i < medications.size(); i++) {
            if (i != medicationIndex.getZeroBased() && medications.get(i).isSameMedication(editedMedication)) {
                throw new CommandException(String.format(Messages.MESSAGE_DUPLICATE_MEDICATION_DESCRIPTION,
                        editedMedication.getDescription()));
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditMedCommand otherEditMedCommand)) {
            return false;
        }

        return patientIndex.equals(otherEditMedCommand.patientIndex)
                && medicationIndex.equals(otherEditMedCommand.medicationIndex)
                && newDescription.equals(otherEditMedCommand.newDescription)
                && newTimes.equals(otherEditMedCommand.newTimes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("medicationIndex", medicationIndex)
                .add("newDescription", newDescription)
                .add("newTimes", newTimes)
                .toString();
    }
}
