package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

public class EditCareCommandTest {

    @Test
    public void execute_validIndex_replacesOnlySelectedInstruction() throws Exception {
        Medication timedMedication = new Medication("Metformin",
                List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)));
        Patient patient = new PatientBuilder().withMedications(timedMedication).withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar", "Walk after lunch").withContacts(GP).build();
        Patient previouslyViewed = new PatientBuilder().withNric("S1111111B").build();
        Model model = new ModelManager(
                new AddressBookBuilder().withPatient(patient).withPatient(previouslyViewed).build(), new UserPrefs());
        model.setViewedPatient(previouslyViewed);

        CommandResult result = new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                "  Walk   before lunch  ").execute(model);

        Patient expected = patient.withCareInstructions(List.of("Check blood sugar", "Walk before lunch"));
        assertEquals(String.format(EditCareCommand.MESSAGE_SUCCESS, patient.getName(), "Walk before lunch"),
                result.getFeedbackToUser());
        assertEquals(expected, model.getFilteredPatientList().get(0));
        assertEquals(expected, model.getViewedPatient().getValue());
        assertEquals(timedMedication.getTimes(),
                model.getViewedPatient().getValue().getMedications().get(0).getTimes());
        assertEquals(patient.getAllergens(), model.getViewedPatient().getValue().getAllergens());
        assertEquals(patient.getContacts(), model.getViewedPatient().getValue().getContacts());
    }

    @Test
    public void execute_duplicateIgnoringCaseAndSpaces_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder()
                .withCareInstructions("Morning walk", "  Check blood sugar  ").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        EditCareCommand command = new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                "  CHECK   BLOOD SUGAR ");

        assertCommandFailure(command, model, EditCareCommand.MESSAGE_DUPLICATE_CARE_INSTRUCTION);
    }

    @Test
    public void execute_sameInstruction_allowed() throws Exception {
        Patient patient = new PatientBuilder().withCareInstructions("Check blood sugar").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, " check BLOOD sugar ").execute(model);

        assertEquals(List.of("check BLOOD sugar"), model.getFilteredPatientList().get(0).getCareInstructions());
    }

    @Test
    public void execute_blankInstruction_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withCareInstructions("Morning walk").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        assertCommandFailure(new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, "   "), model,
                EditCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
    }

    @Test
    public void execute_overlongInstruction_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withCareInstructions("Morning walk").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        String overlongInstruction = "a".repeat(Patient.MAX_CARE_INSTRUCTION_LENGTH + 1);

        assertCommandFailure(new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, overlongInstruction),
                model, EditCareCommand.MESSAGE_CARE_INSTRUCTION_TOO_LONG);
    }

    @Test
    public void execute_invalidIndices_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withCareInstructions("Morning walk").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        assertCommandFailure(new EditCareCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT, "Updated"), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertCommandFailure(new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, "Updated"), model,
                Messages.MESSAGE_INVALID_CARE_INSTRUCTION_INDEX);
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        EditCareCommand command = new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, "Updated");

        assertTrue(command.equals(command));
        assertEquals(command, new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, "Updated"));
        assertEquals(command, new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, " Updated "));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new EditCareCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT, "Updated")));
        assertFalse(command.equals(new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, "Updated")));
        assertFalse(command.equals(new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, "Other")));
        assertTrue(command.toString().contains("careInstructionIndex"));
    }
}
