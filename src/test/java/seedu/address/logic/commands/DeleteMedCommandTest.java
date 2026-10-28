package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_DELETE_MEDICATION_SUCCESS;
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
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class DeleteMedCommandTest {

    private static final Medication METFORMIN =
            new Medication("Metformin 500mg", List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)));
    private static final Medication ASPIRIN = new Medication("Aspirin 100mg");
    private static final Medication AMLODIPINE = new Medication("Amlodipine 5mg", List.of(LocalTime.of(9, 0)));
    private static final Index FIRST_MEDICATION = Index.fromOneBased(1);
    private static final Index SECOND_MEDICATION = Index.fromOneBased(2);
    private static final Index THIRD_MEDICATION = Index.fromOneBased(3);
    private static final Index FOURTH_MEDICATION = Index.fromOneBased(4);

    private final Patient patient = new PatientBuilder(ALICE).withMedications(METFORMIN, ASPIRIN, AMLODIPINE)
            .withAllergens("Penicillin").withCareInstructions("Check blood sugar").build();
    private final Model model = createModel(patient);

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteMedCommand(null, FIRST_MEDICATION));
        assertThrows(NullPointerException.class, () -> new DeleteMedCommand(INDEX_FIRST_PATIENT, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DeleteMedCommand command = new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_firstMedication_removesItAndRenumbersTheRest() {
        assertDeleteSuccess(FIRST_MEDICATION, METFORMIN, ASPIRIN, AMLODIPINE);
    }

    @Test
    public void execute_middleMedication_removesIt() {
        assertDeleteSuccess(SECOND_MEDICATION, ASPIRIN, METFORMIN, AMLODIPINE);
    }

    @Test
    public void execute_lastMedication_removesIt() {
        assertDeleteSuccess(THIRD_MEDICATION, AMLODIPINE, METFORMIN, ASPIRIN);
    }

    @Test
    public void execute_onlyMedication_leavesEmptyList() {
        Patient single = new PatientBuilder(ALICE).withMedications(ASPIRIN).build();
        Model actualModel = createModel(single);
        Patient expectedSingle = new PatientBuilder(single).withMedications(new Medication[0]).build();
        Model expectedModel = createModel(expectedSingle);
        expectedModel.setViewedPatient(expectedSingle);

        assertCommandSuccess(new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION), actualModel,
                String.format(MESSAGE_DELETE_MEDICATION_SUCCESS, 1, single.getName(), ASPIRIN), expectedModel);
    }

    @Test
    public void execute_successMessage_echoesDeletedMedicationWithAndWithoutTimes() throws Exception {
        assertEquals("Deleted medication 1 of Alice Pauline: Metformin 500mg (08:00, 20:00)",
                new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model).getFeedbackToUser());
        assertEquals("Deleted medication 1 of Alice Pauline: Aspirin 100mg",
                new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_otherPatientFieldsUnchanged() throws Exception {
        new DeleteMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION).execute(model);

        Patient edited = model.getFilteredPatientList().get(0);
        assertEquals(patient.getName(), edited.getName());
        assertEquals(patient.getNric(), edited.getNric());
        assertEquals(patient.getDob(), edited.getDob());
        assertEquals(patient.getPhone(), edited.getPhone());
        assertEquals(patient.getAddress(), edited.getAddress());
        assertEquals(patient.getAllergens(), edited.getAllergens());
        assertEquals(patient.getCareInstructions(), edited.getCareInstructions());
    }

    @Test
    public void execute_fullyPopulatedPatient_keepsEveryOtherField() throws Exception {
        Patient populated = new PatientBuilder(patient).withContacts(GP).withAppointments(CHECKUP)
                .withArchived(true).build();
        Model populatedModel = createModel(populated);
        populatedModel.updateFilteredPatientList(Model.PREDICATE_SHOW_ARCHIVED_PATIENTS);

        new DeleteMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION).execute(populatedModel);

        Patient edited = populatedModel.getAddressBook().getPatientList().get(0);
        assertEquals(List.of(METFORMIN, AMLODIPINE), edited.getMedications());
        assertEquals(populated.getName(), edited.getName());
        assertEquals(populated.getNric(), edited.getNric());
        assertEquals(populated.getDob(), edited.getDob());
        assertEquals(populated.getPhone(), edited.getPhone());
        assertEquals(populated.getAddress(), edited.getAddress());
        assertEquals(List.of("Penicillin"), edited.getAllergens());
        assertEquals(List.of("Check blood sugar"), edited.getCareInstructions());
        assertEquals(List.of(GP), edited.getContacts());
        assertEquals(List.of(CHECKUP), edited.getAppointments());
        assertTrue(edited.isArchived());
        assertEquals(new PatientBuilder(populated).withMedications(METFORMIN, AMLODIPINE).build(), edited);
    }

    @Test
    public void execute_otherPatientsUnchanged() throws Exception {
        Model twoPatientModel = createModel(patient);
        Patient other = new PatientBuilder(BENSON).withMedications(ASPIRIN).build();
        twoPatientModel.addPatient(other);

        new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(twoPatientModel);

        assertEquals(other, twoPatientModel.getFilteredPatientList().get(1));
    }

    @Test
    public void execute_viewedPatient_detailPanelPatientUpdated() throws Exception {
        model.setViewedPatient(patient);

        new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model);

        Patient viewed = model.getViewedPatient().getValue();
        assertEquals(model.getFilteredPatientList().get(0), viewed);
        assertEquals(List.of(ASPIRIN, AMLODIPINE), viewed.getMedications());
    }

    @Test
    public void execute_otherPatientViewed_detailPanelShowsEditedPatient() throws Exception {
        model.addPatient(BENSON);
        model.setViewedPatient(BENSON);

        new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model);

        Patient viewed = model.getViewedPatient().getValue();
        assertEquals(model.getFilteredPatientList().get(0), viewed);
        assertEquals(List.of(ASPIRIN, AMLODIPINE), viewed.getMedications());
    }

    @Test
    public void execute_noPatientViewed_detailPanelShowsEditedPatient() throws Exception {
        new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model);

        assertEquals(model.getFilteredPatientList().get(0), model.getViewedPatient().getValue());
    }

    @Test
    public void execute_repeatedUntilEmpty_thenInvalidMedicationIndex() throws Exception {
        DeleteMedCommand command = new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION);
        command.execute(model);
        command.execute(model);
        command.execute(model);

        assertTrue(model.getFilteredPatientList().get(0).getMedications().isEmpty());
        assertCommandFailure(command, model, MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidMedicationIndex_failsAndLeavesModelUnchanged() {
        assertCommandFailure(new DeleteMedCommand(INDEX_FIRST_PATIENT, FOURTH_MEDICATION), model,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
        assertCommandFailure(new DeleteMedCommand(INDEX_FIRST_PATIENT, Index.fromOneBased(Integer.MAX_VALUE)), model,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientWithoutMedications_invalidMedicationIndex() {
        Model noMedicationModel = createModel(new PatientBuilder(BENSON).build());
        assertCommandFailure(new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION), noMedicationModel,
                MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidPatientIndex_failsAndLeavesModelUnchanged() {
        assertCommandFailure(new DeleteMedCommand(INDEX_SECOND_PATIENT, FIRST_MEDICATION), model,
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyPatientList_invalidPatientIndex() {
        assertCommandFailure(new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION), new ModelManager(),
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_bothIndicesInvalid_patientIndexReportedFirst() {
        assertCommandFailure(new DeleteMedCommand(INDEX_THIRD_PATIENT, FOURTH_MEDICATION), model,
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_deletesFromPatientAtDisplayedIndex() {
        Model filteredModel = new ModelManager();
        filteredModel.addPatient(BENSON);
        filteredModel.addPatient(patient);
        showPatientAtIndex(filteredModel, INDEX_SECOND_PATIENT);

        Model expectedModel = new ModelManager(filteredModel.getAddressBook(), new UserPrefs());
        Patient expected = new PatientBuilder(patient).withMedications(ASPIRIN, AMLODIPINE).build();
        expectedModel.setPatient(patient, expected);
        expectedModel.setViewedPatient(expected);
        showPatientAtIndex(expectedModel, INDEX_SECOND_PATIENT);

        assertCommandSuccess(new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION), filteredModel,
                String.format(MESSAGE_DELETE_MEDICATION_SUCCESS, 1, patient.getName(), METFORMIN), expectedModel);
    }

    @Test
    public void execute_success_commandDoesNotShowHelpOrExit() throws Exception {
        CommandResult result = new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION).execute(model);
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void equals() {
        DeleteMedCommand command = new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION);

        assertEquals(command, command);
        assertEquals(command, new DeleteMedCommand(INDEX_FIRST_PATIENT, FIRST_MEDICATION));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new DeleteMedCommand(INDEX_SECOND_PATIENT, FIRST_MEDICATION));
        assertNotEquals(command, new DeleteMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION));
    }

    @Test
    public void toString_containsAllFields() {
        DeleteMedCommand command = new DeleteMedCommand(INDEX_FIRST_PATIENT, SECOND_MEDICATION);
        String expected = DeleteMedCommand.class.getCanonicalName() + "{patientIndex=" + INDEX_FIRST_PATIENT
                + ", medicationIndex=" + SECOND_MEDICATION + "}";
        assertEquals(expected, command.toString());
    }

    /**
     * Asserts that deleting the medication at {@code medicationIndex} succeeds and leaves
     * {@code remaining} as the patient's medications, in order.
     */
    private void assertDeleteSuccess(Index medicationIndex, Medication deleted, Medication... remaining) {
        Patient expectedPatient = new PatientBuilder(patient).withMedications(remaining).build();
        Model expectedModel = createModel(expectedPatient);
        expectedModel.setViewedPatient(expectedPatient);
        String expectedMessage = String.format(MESSAGE_DELETE_MEDICATION_SUCCESS, medicationIndex.getOneBased(),
                patient.getName(), deleted);
        assertCommandSuccess(new DeleteMedCommand(INDEX_FIRST_PATIENT, medicationIndex), model, expectedMessage,
                expectedModel);
    }

    private static Model createModel(Patient patientToAdd) {
        Model newModel = new ModelManager();
        newModel.addPatient(patientToAdd);
        return newModel;
    }
}
