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

public class DeleteCareCommandTest {

    @Test
    public void execute_validIndex_removesInstructionAndPreservesOtherFields() throws Exception {
        Medication timedMedication = new Medication("Metformin", List.of(LocalTime.of(8, 0)));
        Patient patient = new PatientBuilder().withMedications(timedMedication).withAllergens("Penicillin")
                .withCareInstructions("First", "Remove me", "Third").withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);

        CommandResult result = new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT).execute(model);

        Patient expected = patient.withCareInstructions(List.of("First", "Third"));
        assertEquals(String.format(DeleteCareCommand.MESSAGE_SUCCESS, patient.getName(), "Remove me"),
                result.getFeedbackToUser());
        assertEquals(expected, model.getFilteredPatientList().get(0));
        assertEquals(expected, model.getViewedPatient().getValue());
        assertEquals(List.of("First", "Third"), model.getViewedPatient().getValue().getCareInstructions());
        assertEquals(timedMedication.getTimes(),
                model.getViewedPatient().getValue().getMedications().get(0).getTimes());
        assertEquals(patient.getAllergens(), model.getViewedPatient().getValue().getAllergens());
        assertEquals(patient.getContacts(), model.getViewedPatient().getValue().getContacts());
    }

    @Test
    public void execute_invalidIndices_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withCareInstructions("Only instruction").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        assertCommandFailure(new DeleteCareCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertCommandFailure(new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), model,
                Messages.MESSAGE_INVALID_CARE_INSTRUCTION_INDEX);
    }

    @Test
    public void execute_filteredList_changesOnlyDisplayedPatient() throws Exception {
        Patient first = new PatientBuilder().withCareInstructions("First patient instruction").build();
        Patient second = new PatientBuilder().withNric("S1111111B")
                .withCareInstructions("Second patient instruction").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.updateFilteredPatientList(candidate -> candidate.isSamePatient(second));

        new EditCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, "Updated instruction").execute(model);
        new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model);

        assertTrue(model.getFilteredPatientList().get(0).getCareInstructions().isEmpty());
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        DeleteCareCommand command = new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT);

        assertTrue(command.equals(command));
        assertEquals(command, new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new DeleteCareCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT)));
        assertFalse(command.equals(new DeleteCareCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));
        assertTrue(command.toString().contains("careInstructionIndex"));
    }
}
