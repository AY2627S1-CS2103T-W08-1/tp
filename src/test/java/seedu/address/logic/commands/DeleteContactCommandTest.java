package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalContacts.GUARDIAN;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

public class DeleteContactCommandTest {

    @Test
    public void execute_validIndex_removesAndRenumbersContacts() throws Exception {
        Patient patient = new PatientBuilder().withContacts(GP, GUARDIAN)
                .withMedications(new Medication("Medication", List.of(LocalTime.of(8, 0)))).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);
        CommandResult result = new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model);
        assertEquals(String.format(DeleteContactCommand.MESSAGE_SUCCESS, patient.getName(), GP),
                result.getFeedbackToUser());
        assertEquals(patient.withContacts(List.of(GUARDIAN)), model.getViewedPatient().getValue());
        new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model);
        assertTrue(model.getFilteredPatientList().get(0).getContacts().isEmpty());
        assertEquals(patient.getMedications(), model.getFilteredPatientList().get(0).getMedications());
    }

    @Test
    public void execute_invalidIndices_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX, () ->
                new DeleteContactCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT).execute(model));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CONTACT_INDEX, () ->
                new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
    }

    @Test
    public void execute_filteredList_changesOnlyDisplayedPatient() throws Exception {
        Patient first = new PatientBuilder().withContacts(GP).build();
        Patient second = new PatientBuilder().withNric("S1111111B").withContacts(GUARDIAN).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.updateFilteredPatientList(candidate -> candidate.isSamePatient(second));
        new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.empty(),
                Optional.of("Emergency"), Optional.empty(), Optional.empty()).execute(model);
        assertEquals("Emergency", model.getFilteredPatientList().get(0).getContacts().get(0).getRole());
        new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT).execute(model);
        assertEquals(second.withContacts(List.of()), model.getFilteredPatientList().get(0));
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        DeleteContactCommand command = new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT);
        assertTrue(command.equals(command));
        assertEquals(command, new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new DeleteContactCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT)));
        assertFalse(command.equals(new DeleteContactCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));
        assertTrue(command.toString().contains("contactIndex"));
    }
}
