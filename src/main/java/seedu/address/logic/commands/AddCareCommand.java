package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * Adds care information to a patient identified by their displayed index.
 */
public class AddCareCommand extends Command {

    public static final String COMMAND_WORD = "addcare";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds care information to the patient identified "
            + "by the index number used in the displayed patient list.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_MEDICATION + "MEDICATION]... "
            + "[" + PREFIX_ALLERGEN + "ALLERGEN]... "
            + "[" + PREFIX_CARE_INSTRUCTION + "CARE_INSTRUCTION]...\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_MEDICATION + "Metformin 500mg twice daily";

    public static final String MESSAGE_NO_CARE_INFO =
            "Please specify at least one of med/, al/, or ci/ to add.";
    public static final String MESSAGE_BLANK_MEDICATION = "Medication entries cannot be blank.";
    public static final String MESSAGE_BLANK_ALLERGEN = "Allergen entries cannot be blank.";
    public static final String MESSAGE_BLANK_CARE_INSTRUCTION = "Care instructions cannot be blank.";
    public static final String MESSAGE_DUPLICATE_MEDICATION =
            "This medication already exists for this patient and was not added again.";
    public static final String MESSAGE_DUPLICATE_ALLERGEN =
            "This allergen already exists for this patient and was not added again.";
    public static final String MESSAGE_DUPLICATE_CARE_INSTRUCTION =
            "This instruction already exists for this patient and was not added again.";
    public static final String MESSAGE_SUCCESS = "Updated care info for %1$s. Added: %2$s.";

    private final Index targetIndex;
    private final List<String> medications;
    private final List<String> allergens;
    private final List<String> careInstructions;

    /**
     * Creates an {@code AddCareCommand} for the patient at {@code targetIndex}.
     */
    public AddCareCommand(Index targetIndex, List<String> medications, List<String> allergens,
            List<String> careInstructions) {
        requireNonNull(targetIndex);
        requireNonNull(medications);
        requireNonNull(allergens);
        requireNonNull(careInstructions);
        this.targetIndex = targetIndex;
        this.medications = List.copyOf(medications);
        this.allergens = List.copyOf(allergens);
        this.careInstructions = List.copyOf(careInstructions);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> lastShownList = model.getFilteredPatientList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }

        Patient patientToEdit = lastShownList.get(targetIndex.getZeroBased());
        List<String> updatedMedications = new ArrayList<>(patientToEdit.getMedications());
        List<String> updatedAllergens = new ArrayList<>(patientToEdit.getAllergens());
        List<String> updatedCareInstructions = new ArrayList<>(patientToEdit.getCareInstructions());
        List<String> addedEntries = new ArrayList<>();
        List<String> duplicateMessages = new ArrayList<>();

        if (appendNewEntries(updatedMedications, medications, addedEntries)) {
            duplicateMessages.add(MESSAGE_DUPLICATE_MEDICATION);
        }
        if (appendNewEntries(updatedAllergens, allergens, addedEntries)) {
            duplicateMessages.add(MESSAGE_DUPLICATE_ALLERGEN);
        }
        if (appendNewEntries(updatedCareInstructions, careInstructions, addedEntries)) {
            duplicateMessages.add(MESSAGE_DUPLICATE_CARE_INSTRUCTION);
        }

        if (addedEntries.isEmpty()) {
            String message = duplicateMessages.isEmpty()
                    ? MESSAGE_NO_CARE_INFO
                    : String.join("\n", duplicateMessages);
            throw new CommandException(message);
        }

        Patient editedPatient = new Patient(patientToEdit.getName(), patientToEdit.getNric(), patientToEdit.getDob(),
                patientToEdit.getPhone(), patientToEdit.getAddress(), updatedMedications, updatedAllergens,
                updatedCareInstructions);
        model.setPatient(patientToEdit, editedPatient);

        String successMessage = String.format(MESSAGE_SUCCESS, patientToEdit.getName(),
                String.join(", ", addedEntries));
        if (!duplicateMessages.isEmpty()) {
            successMessage += "\n" + String.join("\n", duplicateMessages);
        }
        return new CommandResult(successMessage);
    }

    private static boolean appendNewEntries(List<String> existingEntries, List<String> requestedEntries,
            List<String> addedEntries) {
        boolean hasDuplicate = false;
        for (String requestedEntry : requestedEntries) {
            String trimmedEntry = requestedEntry.trim();
            boolean isDuplicate = existingEntries.stream()
                    .anyMatch(existingEntry -> existingEntry.trim().equalsIgnoreCase(trimmedEntry));
            if (isDuplicate) {
                hasDuplicate = true;
                continue;
            }
            existingEntries.add(trimmedEntry);
            addedEntries.add(trimmedEntry);
        }
        return hasDuplicate;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddCareCommand otherAddCareCommand)) {
            return false;
        }

        return targetIndex.equals(otherAddCareCommand.targetIndex)
                && medications.equals(otherAddCareCommand.medications)
                && allergens.equals(otherAddCareCommand.allergens)
                && careInstructions.equals(otherAddCareCommand.careInstructions);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("medications", medications)
                .add("allergens", allergens)
                .add("careInstructions", careInstructions)
                .toString();
    }
}
