package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Medication;
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
    public static final String MESSAGE_DUPLICATE_EXISTING =
            "%1$s \"%2$s\" was not added: it already exists for this patient.";
    public static final String MESSAGE_DUPLICATE_IN_COMMAND =
            "%1$s \"%2$s\" was not added: it was repeated in this command.";
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
        List<String> updatedMedicationDescriptions = patientToEdit.getMedications().stream()
                .map(Medication::getDescription)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> updatedAllergens = new ArrayList<>(patientToEdit.getAllergens());
        List<String> updatedCareInstructions = new ArrayList<>(patientToEdit.getCareInstructions());
        List<String> addedEntries = new ArrayList<>();
        List<String> duplicateMessages = new ArrayList<>();

        int existingMedicationCount = updatedMedicationDescriptions.size();
        appendNewEntries(updatedMedicationDescriptions, medications, "Medication", addedEntries, duplicateMessages);
        appendNewEntries(updatedAllergens, allergens, "Allergen", addedEntries, duplicateMessages);
        appendNewEntries(updatedCareInstructions, careInstructions, "Care instruction", addedEntries,
                duplicateMessages);

        if (addedEntries.isEmpty()) {
            String message = duplicateMessages.isEmpty()
                    ? MESSAGE_NO_CARE_INFO
                    : String.join("\n", duplicateMessages);
            throw new CommandException(message);
        }

        // A medication added through addcare has a description only, i.e. no daily times.
        List<Medication> updatedMedications = new ArrayList<>(patientToEdit.getMedications());
        updatedMedicationDescriptions.subList(existingMedicationCount, updatedMedicationDescriptions.size())
                .forEach(description -> updatedMedications.add(new Medication(description)));

        Patient editedPatient = new Patient(patientToEdit.getName(), patientToEdit.getNric(), patientToEdit.getDob(),
                patientToEdit.getPhone(), patientToEdit.getAddress(), updatedMedications, updatedAllergens,
                updatedCareInstructions, patientToEdit.getContacts(), patientToEdit.getAppointments(),
                patientToEdit.isArchived());
        model.setPatient(patientToEdit, editedPatient);

        String successMessage = String.format(MESSAGE_SUCCESS, patientToEdit.getName(),
                String.join(", ", addedEntries));
        if (!duplicateMessages.isEmpty()) {
            successMessage += "\n" + String.join("\n", duplicateMessages);
        }
        return new CommandResult(successMessage);
    }

    private static void appendNewEntries(List<String> existingEntries, List<String> requestedEntries,
            String entryType, List<String> addedEntries, List<String> duplicateMessages) {
        List<String> originalEntries = List.copyOf(existingEntries);
        List<String> seenInCommand = new ArrayList<>();
        for (String requestedEntry : requestedEntries) {
            String normalizedEntry = normalizeEntry(requestedEntry);
            if (containsIgnoreCase(originalEntries, normalizedEntry)) {
                duplicateMessages.add(String.format(MESSAGE_DUPLICATE_EXISTING, entryType, normalizedEntry));
                continue;
            }
            if (containsIgnoreCase(seenInCommand, normalizedEntry)) {
                duplicateMessages.add(String.format(MESSAGE_DUPLICATE_IN_COMMAND, entryType, normalizedEntry));
                continue;
            }
            seenInCommand.add(normalizedEntry);
            existingEntries.add(normalizedEntry);
            addedEntries.add(entryType + ": " + normalizedEntry);
        }
    }

    private static boolean containsIgnoreCase(List<String> entries, String requestedEntry) {
        return entries.stream().anyMatch(entry -> normalizeEntry(entry).equalsIgnoreCase(requestedEntry));
    }

    private static String normalizeEntry(String entry) {
        return entry.trim().replaceAll("\\s+", " ");
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
