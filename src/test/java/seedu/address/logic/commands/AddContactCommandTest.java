package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalContacts.GUARDIAN;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

public class AddContactCommandTest {

    @Test
    public void execute_modelIgnoresContactUpdate_failsInternalAssertion() {
        Patient patient = new PatientBuilder().withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs()) {
            @Override
            public void setPatient(Patient target, Patient editedPatient) {
                // Simulate a model implementation that silently drops the contact update.
            }
        };
        assertThrows(AssertionError.class, () -> new AddContactCommand(INDEX_FIRST_PATIENT, GUARDIAN).execute(model));
        assertEquals(patient, model.getAddressBook().getPatientList().get(0));
    }

    @Test
    public void execute_canonicallyEquivalentContact_rejectedWithoutChanges() {
        Contact original = new Contact("Caf\u00e9", "GP", "123", "");
        Patient patient = new PatientBuilder().withContacts(original).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);
        Contact duplicate = new Contact("Cafe\u0301", "Guardian", "123", "Different note");
        assertThrows(CommandException.class, Contact.MESSAGE_DUPLICATE, () ->
                new AddContactCommand(INDEX_FIRST_PATIENT, duplicate).execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
        assertEquals(patient, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_validContact_addedAndViewedPatientUpdated() throws Exception {
        Patient patient = new PatientBuilder()
                .withMedications(new Medication("Medication", List.of(LocalTime.of(8, 0))))
                .withContacts(GUARDIAN).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);
        CommandResult result = new AddContactCommand(INDEX_FIRST_PATIENT, GP).execute(model);
        Patient updated = model.getFilteredPatientList().get(0);
        assertEquals(patient.withContacts(List.of(GUARDIAN, GP)), updated);
        assertEquals(patient.getMedications(), updated.getMedications());
        assertEquals(updated, model.getViewedPatient().getValue());
        assertEquals(String.format(AddContactCommand.MESSAGE_SUCCESS, patient.getName(), GP),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_duplicateContact_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        Contact duplicate = new Contact("TAN   & PARTNERS CLINIC", "Other", "+6561234567", "");
        assertThrows(CommandException.class, Contact.MESSAGE_DUPLICATE, () ->
                new AddContactCommand(INDEX_FIRST_PATIENT, duplicate).execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
        assertNull(model.getViewedPatient().getValue());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        Patient first = new PatientBuilder().withContacts(GP).build();
        Patient second = new PatientBuilder().withNric("S1111111B").build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.updateFilteredPatientList(candidate -> candidate.isSamePatient(second));
        new AddContactCommand(INDEX_FIRST_PATIENT, GP).execute(model);
        assertEquals(second.withContacts(List.of(GP)), model.getFilteredPatientList().get(0));
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
        assertEquals(second.withContacts(List.of(GP)), model.getViewedPatient().getValue());
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX, () ->
                new AddContactCommand(INDEX_SECOND_PATIENT, GUARDIAN).execute(model));
    }

    @Test
    public void execute_otherPatientViewed_showsChangedPatient() throws Exception {
        Patient first = new PatientBuilder().build();
        Patient second = new PatientBuilder().withNric("S1111111B").withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.setViewedPatient(first);

        new AddContactCommand(INDEX_SECOND_PATIENT, GUARDIAN).execute(model);

        assertEquals(second.withContacts(List.of(GP, GUARDIAN)), model.getViewedPatient().getValue());
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
        assertEquals(2, model.getFilteredPatientList().size());
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        AddContactCommand command = new AddContactCommand(INDEX_FIRST_PATIENT, GP);
        assertTrue(command.equals(command));
        assertEquals(command, new AddContactCommand(INDEX_FIRST_PATIENT, GP));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new AddContactCommand(INDEX_SECOND_PATIENT, GP)));
        assertFalse(command.equals(new AddContactCommand(INDEX_FIRST_PATIENT, GUARDIAN)));
        assertTrue(command.toString().contains(GP.toString()));
        assertThrows(NullPointerException.class, () -> new AddContactCommand(null, GP));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
