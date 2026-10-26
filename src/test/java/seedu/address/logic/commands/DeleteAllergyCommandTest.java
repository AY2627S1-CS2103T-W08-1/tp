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

public class DeleteAllergyCommandTest {

    @Test
    public void execute_firstMiddleLastAllergens_removedAndRenumbered() throws Exception {
        Medication medication = new Medication("Metformin", List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)));
        Patient patient = new PatientBuilder().withMedications(medication)
                .withAllergens("Penicillin", "Latex", "Shellfish", "Peanuts")
                .withCareInstructions("Check blood sugar").withContacts(GP).build();
        Patient previouslyViewed = new PatientBuilder().withNric("S1111111B").build();
        Model model = new ModelManager(
                new AddressBookBuilder().withPatient(patient).withPatient(previouslyViewed).build(), new UserPrefs());
        model.setViewedPatient(previouslyViewed);

        CommandResult firstResult = new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT)
                .execute(model);
        Patient afterFirst = patient.withAllergens(List.of("Latex", "Shellfish", "Peanuts"));
        assertEquals(String.format(DeleteAllergyCommand.MESSAGE_SUCCESS, patient.getName(), "Penicillin"),
                firstResult.getFeedbackToUser());
        assertEquals(afterFirst, model.getViewedPatient().getValue());

        CommandResult middleResult = new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)
                .execute(model);
        Patient afterMiddle = patient.withAllergens(List.of("Latex", "Peanuts"));
        assertEquals(String.format(DeleteAllergyCommand.MESSAGE_SUCCESS, patient.getName(), "Shellfish"),
                middleResult.getFeedbackToUser());
        assertEquals(afterMiddle, model.getViewedPatient().getValue());

        CommandResult lastResult = new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)
                .execute(model);
        Patient afterLast = patient.withAllergens(List.of("Latex"));
        assertEquals(String.format(DeleteAllergyCommand.MESSAGE_SUCCESS, patient.getName(), "Peanuts"),
                lastResult.getFeedbackToUser());
        assertEquals(afterLast, model.getFilteredPatientList().get(0));
        assertEquals(afterLast, model.getViewedPatient().getValue());
        assertEquals(List.of(medication), afterLast.getMedications());
        assertEquals(List.of(GP), afterLast.getContacts());
    }

    @Test
    public void execute_invalidIndices_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withAllergens("Penicillin").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());

        assertCommandFailure(new DeleteAllergyCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertCommandFailure(new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), model,
                Messages.MESSAGE_INVALID_ALLERGEN_INDEX);

        Patient patientWithoutAllergies = new PatientBuilder(patient).withAllergens().build();
        Model emptyAllergyModel = new ModelManager(
                new AddressBookBuilder().withPatient(patientWithoutAllergies).build(), new UserPrefs());
        assertCommandFailure(new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT), emptyAllergyModel,
                Messages.MESSAGE_INVALID_ALLERGEN_INDEX);
    }

    @Test
    public void execute_filteredList_changesOnlyDisplayedPatient() throws Exception {
        Patient first = new PatientBuilder().withAllergens("Penicillin").build();
        Patient second = new PatientBuilder().withNric("S1111111B").withAllergens("Latex", "Shellfish").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.updateFilteredPatientList(candidate -> candidate.isSamePatient(second));

        new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT).execute(model);

        assertEquals(second.withAllergens(List.of("Latex")), model.getFilteredPatientList().get(0));
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        DeleteAllergyCommand command = new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT);

        assertTrue(command.equals(command));
        assertEquals(command, new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new DeleteAllergyCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT)));
        assertFalse(command.equals(new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));
        assertTrue(command.toString().contains("allergyIndex"));
    }
}
