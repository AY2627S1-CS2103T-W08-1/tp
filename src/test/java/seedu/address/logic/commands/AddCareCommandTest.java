package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class AddCareCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_newEntries_updatesPatient() {
        Patient patientToEdit = model.getFilteredPatientList().get(INDEX_FIRST_PATIENT.getZeroBased());
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("Metformin 500mg twice daily"), List.of("Penicillin"),
                List.of("Check blood sugar before breakfast"));

        Patient editedPatient = new PatientBuilder(patientToEdit)
                .withMedications("Metformin 500mg twice daily")
                .withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar before breakfast")
                .build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patientToEdit, editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patientToEdit.getName(),
                "Metformin 500mg twice daily, Penicillin, Check blood sugar before breakfast");
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_partialDuplicates_addsUniqueEntriesAndReportsDuplicate() {
        Patient patient = new PatientBuilder(ALICE).withMedications("Metformin").build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of(" metformin ", " Atorvastatin ", "ATORVASTATIN"), List.of(), List.of());

        Patient editedPatient = new PatientBuilder(patient)
                .withMedications("Metformin", "Atorvastatin")
                .build();
        Model expectedModel = new ModelManager(actualModel.getAddressBook(), new UserPrefs());
        expectedModel.setPatient(patient, editedPatient);

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patient.getName(), "Atorvastatin")
                + "\n" + AddCareCommand.MESSAGE_DUPLICATE_MEDICATION;
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

        String expectedMessage = String.format(AddCareCommand.MESSAGE_SUCCESS, patientToEdit.getName(), "Shellfish");
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_allDuplicates_throwsCommandException() {
        Patient patient = new PatientBuilder(ALICE)
                .withMedications("Metformin")
                .withAllergens("Penicillin")
                .build();
        Model actualModel = new ModelManager();
        actualModel.addPatient(patient);
        AddCareCommand command = new AddCareCommand(INDEX_FIRST_PATIENT,
                List.of("METFORMIN"), List.of("penicillin"), List.of());

        String expectedMessage = AddCareCommand.MESSAGE_DUPLICATE_MEDICATION
                + "\n" + AddCareCommand.MESSAGE_DUPLICATE_ALLERGEN;
        assertCommandFailure(command, actualModel, expectedMessage);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPatientList().size() + 1);
        AddCareCommand command = new AddCareCommand(outOfBoundsIndex, List.of("Metformin"), List.of(), List.of());

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

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertFalse(command.equals(differentIndexCommand));
        assertFalse(command.equals(differentEntryCommand));
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
}
