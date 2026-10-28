package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_MEDICATION_DESCRIPTION;
import static seedu.address.logic.Messages.MESSAGE_EDIT_MEDICATION_SUCCESS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class EditMedCommandTest {

    private static final LocalTime MORNING = LocalTime.of(8, 0);
    private static final LocalTime EVENING = LocalTime.of(20, 0);
    private static final Medication METFORMIN = new Medication("Metformin 500mg", List.of(MORNING, EVENING));
    private static final Medication ASPIRIN = new Medication("Aspirin 100mg");
    private static final Index FIRST_MEDICATION = Index.fromOneBased(1);
    private static final Index SECOND_MEDICATION = Index.fromOneBased(2);
    private static final Index THIRD_MEDICATION = Index.fromOneBased(3);

    private final Patient patient = new PatientBuilder(ALICE).withMedications(METFORMIN, ASPIRIN)
            .withAllergens("Penicillin").withCareInstructions("Check blood sugar").build();
    private final Model model = createModel(patient);

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        Index patientIndex = INDEX_FIRST_PATIENT;
        Index medIndex = FIRST_MEDICATION;
        Optional<String> description = Optional.of("Aspirin");
        Optional<List<LocalTime>> times = Optional.empty();
        assertThrows(NullPointerException.class, () -> new EditMedCommand(null, medIndex, description, times));
        assertThrows(NullPointerException.class, () -> new EditMedCommand(patientIndex, null, description, times));
        assertThrows(NullPointerException.class, () -> new EditMedCommand(patientIndex, medIndex, null, times));
        assertThrows(NullPointerException.class, () -> new EditMedCommand(patientIndex, medIndex, description, null));
    }

    @Test
    public void constructor_nothingToEdit_failsAssertion() {
        assertThrows(AssertionError.class, () -> new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.empty(), Optional.empty()));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        EditMedCommand command = descriptionOnly(FIRST_MEDICATION, "Metformin 850mg");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_descriptionOnly_keepsTimes() {
        Medication edited = new Medication("Metformin 850mg", List.of(MORNING, EVENING));
        assertEditSuccess(descriptionOnly(FIRST_MEDICATION, "Metformin 850mg"), edited, FIRST_MEDICATION,
                METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_timesOnly_keepsDescription() {
        Medication edited = new Medication("Aspirin 100mg", List.of(MORNING));
        assertEditSuccess(timesOnly(SECOND_MEDICATION, MORNING), edited, SECOND_MEDICATION, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_descriptionAndTimes_replacesBoth() {
        Medication edited = new Medication("Amlodipine 5mg", List.of(EVENING));
        EditMedCommand command = new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Amlodipine 5mg"), Optional.of(List.of(EVENING)));
        assertEditSuccess(command, edited, FIRST_MEDICATION, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_emptyTimes_clearsTimes() {
        Medication edited = new Medication("Metformin 500mg");
        assertEditSuccess(timesOnly(FIRST_MEDICATION), edited, FIRST_MEDICATION, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_duplicateAndUnsortedTimes_collapsedAndSorted() {
        Medication edited = new Medication("Aspirin 100mg", List.of(MORNING, EVENING));
        EditMedCommand command = timesOnly(SECOND_MEDICATION, EVENING, MORNING, EVENING, MORNING);
        assertEditSuccess(command, edited, SECOND_MEDICATION, METFORMIN, ASPIRIN);
        assertEquals("Updated medication 2 of Alice Pauline: Aspirin 100mg (08:00, 20:00)",
                String.format(MESSAGE_EDIT_MEDICATION_SUCCESS, 2, ALICE.getName(), edited));
    }

    @Test
    public void execute_newTimesReplaceAllExistingTimes() {
        Medication edited = new Medication("Metformin 500mg", List.of(LocalTime.NOON));
        assertEditSuccess(timesOnly(FIRST_MEDICATION, LocalTime.NOON), edited, FIRST_MEDICATION, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_changeOnlyLetterCaseOfOwnDescription_success() {
        Medication edited = new Medication("METFORMIN 500MG", List.of(MORNING, EVENING));
        assertEditSuccess(descriptionOnly(FIRST_MEDICATION, "METFORMIN 500MG"), edited, FIRST_MEDICATION,
                METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_sameValuesAgain_succeedsAndLeavesPatientUnchanged() {
        EditMedCommand command = new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Metformin 500mg"), Optional.of(List.of(MORNING, EVENING)));
        assertEditSuccess(command, METFORMIN, FIRST_MEDICATION, METFORMIN, ASPIRIN);
        assertEditSuccess(command, METFORMIN, FIRST_MEDICATION, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_otherPatientFieldsAndMedicationsUnchanged() throws Exception {
        descriptionOnly(SECOND_MEDICATION, "Aspirin 75mg").execute(model);

        Patient edited = model.getFilteredPatientList().get(0);
        assertEquals(patient.getName(), edited.getName());
        assertEquals(patient.getNric(), edited.getNric());
        assertEquals(patient.getDob(), edited.getDob());
        assertEquals(patient.getPhone(), edited.getPhone());
        assertEquals(patient.getAddress(), edited.getAddress());
        assertEquals(patient.getAllergens(), edited.getAllergens());
        assertEquals(patient.getCareInstructions(), edited.getCareInstructions());
        assertEquals(List.of(METFORMIN, new Medication("Aspirin 75mg")), edited.getMedications());
    }

    @Test
    public void execute_contactsAppointmentsAndArchivedFlag_allKeptAfterEdit() throws Exception {
        Patient archivedPatient = new PatientBuilder(patient).withContacts(GP).withAppointments(CHECKUP)
                .withArchived(true).build();
        Model archivedModel = createModel(archivedPatient);
        archivedModel.updateFilteredPatientList(Model.PREDICATE_SHOW_ARCHIVED_PATIENTS);

        descriptionOnly(FIRST_MEDICATION, "Metformin 850mg").execute(archivedModel);

        Patient edited = archivedModel.getAddressBook().getPatientList().get(0);
        assertEquals(List.of(GP), edited.getContacts());
        assertEquals(List.of(CHECKUP), edited.getAppointments());
        assertTrue(edited.isArchived());
        assertEquals(new PatientBuilder(archivedPatient).withMedications(
                new Medication("Metformin 850mg", METFORMIN.getTimes()), ASPIRIN).build(), edited);
    }

    @Test
    public void execute_viewedPatient_detailPanelPatientUpdated() throws Exception {
        model.setViewedPatient(patient);

        descriptionOnly(FIRST_MEDICATION, "Metformin 850mg").execute(model);

        assertEquals(model.getFilteredPatientList().get(0), model.getViewedPatient().getValue());
    }

    @Test
    public void execute_otherPatientViewed_detailPanelShowsEditedPatient() throws Exception {
        model.addPatient(BENSON);
        model.setViewedPatient(BENSON);

        descriptionOnly(FIRST_MEDICATION, "Metformin 850mg").execute(model);

        assertEquals(model.getFilteredPatientList().get(0), model.getViewedPatient().getValue());
    }

    @Test
    public void execute_noPatientViewed_detailPanelShowsEditedPatient() throws Exception {
        descriptionOnly(FIRST_MEDICATION, "Metformin 850mg").execute(model);

        assertEquals(model.getFilteredPatientList().get(0), model.getViewedPatient().getValue());
    }

    @Test
    public void execute_duplicateDescriptionOfOtherMedication_failsAndLeavesModelUnchanged() {
        String message = String.format(MESSAGE_DUPLICATE_MEDICATION_DESCRIPTION, "aspirin 100mg");
        assertCommandFailure(descriptionOnly(FIRST_MEDICATION, "aspirin   100mg"), model, message);
        assertCommandFailure(new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("ASPIRIN 100MG"), Optional.of(List.of(MORNING))), model,
                String.format(MESSAGE_DUPLICATE_MEDICATION_DESCRIPTION, "ASPIRIN 100MG"));
        assertEquals(List.of(METFORMIN, ASPIRIN), model.getFilteredPatientList().get(0).getMedications());
    }

    @Test
    public void execute_invalidMedicationIndex_failsAndLeavesModelUnchanged() {
        assertCommandFailure(descriptionOnly(THIRD_MEDICATION, "Aspirin 75mg"), model,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
        assertCommandFailure(timesOnly(Index.fromOneBased(Integer.MAX_VALUE), MORNING), model,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientWithoutMedications_invalidMedicationIndex() {
        Model emptyMedicationModel = createModel(new PatientBuilder(BENSON).build());
        assertCommandFailure(timesOnly(FIRST_MEDICATION, MORNING), emptyMedicationModel,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidPatientIndex_failsAndLeavesModelUnchanged() {
        EditMedCommand command = new EditMedCommand(INDEX_SECOND_PATIENT, FIRST_MEDICATION,
                Optional.of("Metformin 850mg"), Optional.empty());
        assertCommandFailure(command, model, MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyPatientList_invalidPatientIndex() {
        assertCommandFailure(descriptionOnly(FIRST_MEDICATION, "Aspirin"), new ModelManager(),
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidPatientIndexIsCheckedBeforeMedicationIndex() {
        EditMedCommand command = new EditMedCommand(INDEX_THIRD_PATIENT, THIRD_MEDICATION,
                Optional.empty(), Optional.of(List.of()));
        assertCommandFailure(command, model, MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_editsPatientAtDisplayedIndex() {
        Model filteredModel = new ModelManager();
        filteredModel.addPatient(BENSON);
        filteredModel.addPatient(patient);
        showPatientAtIndex(filteredModel, INDEX_SECOND_PATIENT);

        EditMedCommand command = descriptionOnly(FIRST_MEDICATION, "Metformin 850mg");
        Patient expected = new PatientBuilder(patient)
                .withMedications(new Medication("Metformin 850mg", List.of(MORNING, EVENING)), ASPIRIN).build();
        Model expectedModel = new ModelManager(filteredModel.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patient, expected);
        expectedModel.setViewedPatient(expected);
        showPatientAtIndex(expectedModel, INDEX_SECOND_PATIENT);

        assertCommandSuccess(command, filteredModel, String.format(MESSAGE_EDIT_MEDICATION_SUCCESS, 1,
                patient.getName(), new Medication("Metformin 850mg", List.of(MORNING, EVENING))), expectedModel);
    }

    @Test
    public void execute_success_commandDoesNotShowHelpOrExit() throws Exception {
        CommandResult result = descriptionOnly(FIRST_MEDICATION, "Metformin 850mg").execute(model);
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void equals() {
        EditMedCommand command = new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Aspirin"), Optional.of(List.of(MORNING)));

        assertEquals(command, command);
        assertEquals(command, new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Aspirin"), Optional.of(List.of(MORNING))));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new EditMedCommand(INDEX_SECOND_PATIENT, FIRST_MEDICATION,
                Optional.of("Aspirin"), Optional.of(List.of(MORNING))));
        assertNotEquals(command, new EditMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION,
                Optional.of("Aspirin"), Optional.of(List.of(MORNING))));
        assertNotEquals(command, new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Other"), Optional.of(List.of(MORNING))));
        assertNotEquals(command, new EditMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION,
                Optional.of("Aspirin"), Optional.empty()));
    }

    @Test
    public void toString_containsAllFields() {
        EditMedCommand command = new EditMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION,
                Optional.of("Aspirin"), Optional.of(List.of(MORNING)));
        String expected = EditMedCommand.class.getCanonicalName() + "{patientIndex=" + INDEX_FIRST_PATIENT
                + ", medicationIndex=" + SECOND_MEDICATION + ", newDescription=Optional[Aspirin], "
                + "newTimes=Optional[[08:00]]}";
        assertEquals(expected, command.toString());
    }

    /**
     * Asserts that executing {@code command} on the patient with {@code originalMedications} succeeds and
     * replaces the medication at {@code medicationIndex} with {@code edited}.
     */
    private void assertEditSuccess(EditMedCommand command, Medication edited, Index medicationIndex,
            Medication... originalMedications) {
        Model actualModel = createModel(patient);
        List<Medication> expectedMedications = new ArrayList<>(List.of(originalMedications));
        expectedMedications.set(medicationIndex.getZeroBased(), edited);
        Patient expectedPatient = new PatientBuilder(patient)
                .withMedications(expectedMedications.toArray(new Medication[0])).build();
        Model expectedModel = createModel(expectedPatient);
        expectedModel.setViewedPatient(expectedPatient);

        String expectedMessage = String.format(MESSAGE_EDIT_MEDICATION_SUCCESS, medicationIndex.getOneBased(),
                patient.getName(), edited);
        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    private static Model createModel(Patient patientToAdd) {
        Model newModel = new ModelManager();
        newModel.addPatient(patientToAdd);
        return newModel;
    }

    private static EditMedCommand descriptionOnly(Index medicationIndex, String description) {
        return new EditMedCommand(INDEX_FIRST_PATIENT, medicationIndex, Optional.of(description), Optional.empty());
    }

    private static EditMedCommand timesOnly(Index medicationIndex, LocalTime... times) {
        return new EditMedCommand(INDEX_FIRST_PATIENT, medicationIndex, Optional.empty(),
                Optional.of(List.of(times)));
    }
}
