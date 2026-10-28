package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

public class AddCareCommandTest {


    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_sameTextAcrossCategories_updatesPatientAndLabelsFeedback() {
        Patient patientToEdit = model.getFilteredPatientList().get(INDEX_FIRST_PATIENT.getZeroBased());
        model.setViewedPatient(model.getFilteredPatientList().get(INDEX_SECOND_PATIENT.getZeroBased()));
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of(" Aspirin   daily "), List.of(" Aspirin   daily "), List.of(" Aspirin   daily "));

        Patient editedPatient = new PatientBuilder(patientToEdit)
                .withMedications("Aspirin daily")
                .withAllergens("Aspirin daily")
                .withCareInstructions("Aspirin daily")
                .build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patientToEdit, editedPatient);
        expectedModel.setViewedPatient(editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patientToEdit.getName(),
                "Medication: Aspirin daily, Allergen: Aspirin daily, Care instruction: Aspirin daily");
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_newMedication_keepsExistingTimesAndAddsNewWithNoTimes() {
        Medication scheduled = new Medication("Metformin", List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)));
        Patient patient = new PatientBuilder(ALICE).withMedications(scheduled).build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Aspirin"), List.of(), List.of());

        Patient editedPatient = new PatientBuilder(patient)
                .withMedications(scheduled, new Medication("Aspirin"))
                .build();
        Model expectedModel = new ModelManager(actualModel.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patient, editedPatient);
        expectedModel.setViewedPatient(editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patient.getName(),
                "Medication: Aspirin");
        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_archivedPatientForcedIntoDisplayedList_preservesArchivedState() throws Exception {
        Patient archivedPatient = new PatientBuilder(ALICE).withAppointments(CHECKUP).withArchived(true).build();
        Model actualModel = new ModelManager(
                new AddressBookBuilder().withPatient(archivedPatient).build(), new UserPrefs());
        actualModel.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);

        new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of("Latex"), List.of()).execute(actualModel);

        Patient updatedPatient = actualModel.getAddressBook().getPatientList().get(0);
        assertEquals(updatedPatient, actualModel.getViewedPatient().getValue());
        assertTrue(updatedPatient.isArchived());
        assertEquals(List.of("Latex"), updatedPatient.getAllergens());
        assertEquals(List.of(CHECKUP), updatedPatient.getAppointments());
    }

    @Test
    public void execute_specialCharactersAndMaxLengthText_addedAndSurvivesStorageRoundTrip() throws Exception {
        String special = "<b>\"';DROP TABLE--&\u00e9\u4e2d\u6587/x";
        String longText = "x".repeat(Medication.MAX_DESCRIPTION_LENGTH);
        Patient patient = new PatientBuilder(ALICE).build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        new AddCareCommand(INDEX_FIRST_PATIENT, List.of(special, longText), List.of(), List.of())
                .execute(actualModel);

        Patient edited = actualModel.getFilteredPatientList().get(0);
        assertEquals(edited, actualModel.getViewedPatient().getValue());
        assertEquals(List.of(new Medication(special), new Medication(longText)), edited.getMedications());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("addressbook.json"));
        storage.saveAddressBook(actualModel.getAddressBook());
        assertEquals(List.of(edited), storage.readAddressBook().get().getPatientList());
    }

    @Test
    public void execute_existingAndRepeatedDuplicates_addsUniqueEntryAndReportsEachDuplicate() {
        Patient patient = new PatientBuilder(ALICE).withMedications("Metformin").build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of(" metformin ", "METFORMIN", " Atorvastatin ", "ATORVASTATIN"), List.of(), List.of());

        Patient editedPatient = new PatientBuilder(patient)
                .withMedications("Metformin", "Atorvastatin")
                .build();
        Model expectedModel = new ModelManager(actualModel.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patient, editedPatient);
        expectedModel.setViewedPatient(editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patient.getName(),
                "Medication: Atorvastatin")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING, "Medication", "metformin")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING, "Medication", "METFORMIN")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_IN_COMMAND, "Medication", "ATORVASTATIN");
        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_patientWithAppointments_keepsAppointments() {
        Patient patient = new PatientBuilder(ALICE).withAppointments(CHECKUP).build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of(), List.of("Shellfish"), List.of());

        Patient editedPatient = new PatientBuilder(patient).withAllergens("Shellfish").build();
        Model expectedModel = new ModelManager(actualModel.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patient, editedPatient);
        expectedModel.setViewedPatient(editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patient.getName(),
                "Allergen: Shellfish");
        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_updatesDisplayedPatient() {
        showPatientAtIndex(model, INDEX_SECOND_PATIENT);
        Patient patientToEdit = model.getFilteredPatientList().get(INDEX_FIRST_PATIENT.getZeroBased());
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of(), List.of("Shellfish"), List.of());

        Patient editedPatient = new PatientBuilder(patientToEdit).withAllergens("Shellfish").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patientToEdit, editedPatient);
        showPatientAtIndex(expectedModel, INDEX_SECOND_PATIENT);
        expectedModel.setViewedPatient(editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patientToEdit.getName(),
                "Allergen: Shellfish");
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_allDuplicates_throwsCommandException() {
        Patient patient = new PatientBuilder(ALICE)
                .withMedications("Metformin 500mg")
                .withAllergens("Dust mites")
                .withCareInstructions("Check blood sugar")
                .build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("METFORMIN   500MG"), List.of("dust   mites"), List.of("check   blood   sugar"));

        String expectedMessage = String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                "Medication", "METFORMIN 500MG")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                        "Allergen", "dust mites")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                        "Care instruction", "check blood sugar");
        assertCommandFailure(command, actualModel, expectedMessage);
    }

    @Test
    public void execute_unicodeWhitespaceDuplicate_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder(ALICE).withAllergens("Dust mites")
                .withCareInstructions("Check blood sugar").build();
        Model actualModel = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of(),
                List.of("Dust\u00a0mites"), List.of("Check\u00a0blood sugar"));

        String expectedMessage = String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                "Allergen", "Dust mites")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                        "Care instruction", "Check blood sugar");
        assertCommandFailure(command, actualModel, expectedMessage);
    }

    @Test
    public void execute_canonicallyEquivalentDuplicate_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder(ALICE).withAllergens("Caf\u00e9")
                .withCareInstructions("R\u00e9sum\u00e9").build();
        Model actualModel = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of(),
                List.of("Cafe\u0301"), List.of("Re\u0301sume\u0301"));

        String expectedMessage = String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                "Allergen", "Caf\u00e9")
                + "\n" + String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                        "Care instruction", "R\u00e9sum\u00e9");
        assertCommandFailure(command, actualModel, expectedMessage);
    }

    @Test
    public void execute_unicodeWhitespaceDuplicateMedication_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder(ALICE).withMedications("Aspirin 100mg").build();
        Model actualModel = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Aspirin\u00a0100mg"),
                List.of(), List.of());

        String expectedMessage = String.format(AddCareCommand.MESSAGE_DUPLICATE_EXISTING,
                "Medication", "Aspirin 100mg");
        assertCommandFailure(command, actualModel, expectedMessage);
    }

    @Test
    public void execute_noCareEntries_throwsCommandException() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of(), List.of());

        assertCommandFailure(command, model, AddCareCommand.MESSAGE_NO_CARE_INFO);
    }

    @Test
    public void execute_overlongCareInstruction_throwsCommandException() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of(), List.of(),
                List.of("a".repeat(Patient.MAX_CARE_INSTRUCTION_LENGTH + 1)));

        assertCommandFailure(command, model, AddCareCommand.MESSAGE_CARE_INSTRUCTION_TOO_LONG);
    }

    @Test
    public void execute_overlongMedication_throwsCommandException() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("m".repeat(Medication.MAX_DESCRIPTION_LENGTH + 1)), List.of(), List.of());

        assertCommandFailure(command, model, AddCareCommand.MESSAGE_MEDICATION_TOO_LONG);
    }

    @Test
    public void execute_overlongAllergen_throwsCommandException() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT, List.of(),
                List.of("a".repeat(Patient.MAX_ALLERGEN_LENGTH + 1)), List.of());

        assertCommandFailure(command, model, AddCareCommand.MESSAGE_ALLERGEN_TOO_LONG);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPatientList().size() + 1);
        AddCareCommand command = new AddCareCommand(outOfBoundsIndex, List.of("Metformin"), List.of(), List.of());

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);
        AddCareCommand command = new AddCareCommand(INDEX_SECOND_PATIENT,
                List.of("Metformin"), List.of(), List.of());

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin"), List.of(), List.of());
        AddCareCommand sameCommand = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin"), List.of(), List.of());
        AddCareCommand differentIndexCommand = new AddCareCommand(INDEX_SECOND_PATIENT,
                List.of("Metformin"), List.of(), List.of());
        AddCareCommand differentEntryCommand = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Aspirin"), List.of(), List.of());
        AddCareCommand differentAllergenCommand = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin"), List.of("Penicillin"), List.of());
        AddCareCommand differentCareInstructionCommand = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin"), List.of(), List.of("Check blood sugar"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertFalse(command.equals(differentIndexCommand));
        assertFalse(command.equals(differentEntryCommand));
        assertFalse(command.equals(differentAllergenCommand));
        assertFalse(command.equals(differentCareInstructionCommand));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
    }

    @Test
    public void toStringMethod() {
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin"), List.of("Penicillin"), List.of("Check blood sugar"));
        String expected = AddCareCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT
                + ", medications=[Metformin], allergens=[Penicillin], careInstructions=[Check blood sugar]}";
        assertEquals(expected, command.toString());
    }

    @Test
    public void execute_existingContacts_preserved() throws Exception {
        var contact = GP;
        Medication medication = new Medication("Metformin", List.of(LocalTime.of(8, 0)));
        Patient patient = new PatientBuilder().withContacts(contact).withMedications(medication).build();
        Model modelWithContacts = new ModelManager(
                new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Medication"), List.of(), List.of()).execute(modelWithContacts);
        assertEquals(List.of(contact), modelWithContacts.getFilteredPatientList().get(0).getContacts());
        assertEquals(List.of(medication, new Medication("Medication")),
                modelWithContacts.getFilteredPatientList().get(0).getMedications());
    }
}
