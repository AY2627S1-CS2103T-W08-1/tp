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
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
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

public class EditContactCommandTest {

    private EditContactCommand edit(Index patientIndex, Index contactIndex, Optional<String> note) {
        return new EditContactCommand(patientIndex, contactIndex,
                Optional.empty(), Optional.empty(), Optional.empty(), note);
    }

    @Test
    public void execute_canonicallyEquivalentContact_rejectedWithoutChanges() throws Exception {
        Contact original = new Contact("Caf\u00e9", "GP", "123", "");
        Contact other = new Contact("Other clinic", "GP", "123", "");
        Patient patient = new PatientBuilder().withContacts(original, other).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);
        EditContactCommand duplicate = new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of("Cafe\u0301"), Optional.empty(), Optional.empty(), Optional.empty());
        assertThrows(CommandException.class, Contact.MESSAGE_DUPLICATE, () -> duplicate.execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
        assertEquals(patient, model.getViewedPatient().getValue());

        EditContactCommand sameContact = new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of("Cafe\u0301"), Optional.empty(), Optional.empty(), Optional.empty());
        sameContact.execute(model);
        assertEquals("Cafe\u0301", model.getFilteredPatientList().get(0).getContacts().get(0).getName());
    }

    @Test
    public void execute_noteChangedAndCleared_otherFieldsPreserved() throws Exception {
        Patient patient = new PatientBuilder().withContacts(GP, GUARDIAN).withAllergens("Allergen")
                .withMedications(new Medication("Medication", List.of(LocalTime.of(8, 0)))).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        model.setViewedPatient(patient);
        edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of("Call first")).execute(model);
        Contact updated = model.getFilteredPatientList().get(0).getContacts().get(0);
        assertEquals(new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "Call first"), updated);
        edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of("")).execute(model);
        updated = new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "");
        Patient expected = patient.withContacts(List.of(updated, GUARDIAN));
        assertEquals(expected, model.getFilteredPatientList().get(0));
        assertEquals(expected, model.getViewedPatient().getValue());
        assertEquals(patient.getMedications(), model.getViewedPatient().getValue().getMedications());
    }

    @Test
    public void execute_allFieldsChanged_positionPreserved() throws Exception {
        Patient patient = new PatientBuilder().withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        EditContactCommand command = new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of(GUARDIAN.getName()), Optional.of(GUARDIAN.getRole()),
                Optional.of(GUARDIAN.getPhone()), Optional.of(GUARDIAN.getNote()));
        assertEquals(String.format(EditContactCommand.MESSAGE_SUCCESS, patient.getName(), GUARDIAN),
                command.execute(model).getFeedbackToUser());
        assertEquals(List.of(GUARDIAN), model.getFilteredPatientList().get(0).getContacts());
        assertEquals(model.getFilteredPatientList().get(0), model.getViewedPatient().getValue());
    }

    @Test
    public void execute_duplicateOrInvalidFields_rejectedWithoutChanges() throws Exception {
        Patient patient = new PatientBuilder().withContacts(GP, GUARDIAN).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        EditContactCommand duplicate = new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT,
                Optional.of(GP.getName().toUpperCase()), Optional.empty(),
                Optional.of("+6561234567"), Optional.empty());
        assertThrows(CommandException.class, Contact.MESSAGE_DUPLICATE, () -> duplicate.execute(model));
        EditContactCommand invalid = new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.empty(), Optional.empty(), Optional.of("12"), Optional.empty());
        assertThrows(CommandException.class, Contact.MESSAGE_PHONE_CONSTRAINTS, () -> invalid.execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
        assertNull(model.getViewedPatient().getValue());
        edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of(GP.getNote())).execute(model);
        assertEquals(patient, model.getFilteredPatientList().get(0));
    }

    @Test
    public void execute_invalidIndices_rejectedWithoutChanges() {
        Patient patient = new PatientBuilder().build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(patient).build(), new UserPrefs());
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX, () ->
                edit(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT, Optional.of("")).execute(model));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_CONTACT_INDEX, () ->
                edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of("")).execute(model));
        assertEquals(patient, model.getFilteredPatientList().get(0));
    }

    @Test
    public void execute_otherPatientViewed_showsChangedPatient() throws Exception {
        Patient first = new PatientBuilder().build();
        Patient second = new PatientBuilder().withNric("S1111111B").withContacts(GP).build();
        Model model = new ModelManager(new AddressBookBuilder().withPatient(first).withPatient(second).build(),
                new UserPrefs());
        model.setViewedPatient(first);

        edit(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT, Optional.of("Call first")).execute(model);
        Contact expected = new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "Call first");

        assertEquals(second.withContacts(List.of(expected)), model.getViewedPatient().getValue());
        assertEquals(first, model.getAddressBook().getPatientList().get(0));
        assertEquals(2, model.getFilteredPatientList().size());
    }

    @Test
    public void equals_andToString_fieldsCompared() {
        EditContactCommand command = edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of(""));
        assertTrue(command.equals(command));
        assertEquals(command, edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of("")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(edit(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT, Optional.of(""))));
        assertFalse(command.equals(edit(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, Optional.of(""))));
        assertFalse(command.equals(edit(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.empty())));
        assertFalse(command.equals(new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.of("Other"), Optional.empty(), Optional.empty(), Optional.of(""))));
        assertFalse(command.equals(new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.empty(), Optional.of("Other"), Optional.empty(), Optional.of(""))));
        assertFalse(command.equals(new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                Optional.empty(), Optional.empty(), Optional.of("123"), Optional.of(""))));
        assertTrue(command.toString().contains("contactIndex"));
    }
}
