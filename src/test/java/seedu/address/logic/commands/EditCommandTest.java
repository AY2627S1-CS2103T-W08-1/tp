package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DOB_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NRIC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.DANIEL;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPatientDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.EditPatientDescriptorBuilder;
import seedu.address.testutil.PatientBuilder;
import seedu.address.testutil.TypicalContacts;

public class EditCommandTest {

    @Test
    public void execute_allFields_preservesNonEditableFieldsAndRefreshesView() throws Exception {
        Medication medication = new Medication("Metformin", List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)));
        Patient original = new PatientBuilder(DANIEL).withMedications(medication).withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar").withContacts(TypicalContacts.GP)
                .withAppointments(CHECKUP).withArchived(true).build();
        Model model = new ModelManager(
                new AddressBookBuilder().withPatient(original).withPatient(ALICE).build(), new UserPrefs());
        model.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);
        model.setViewedPatient(ALICE);
        Patient edited = new PatientBuilder(original).withName(VALID_NAME_BOB).withNric(VALID_NRIC_BOB)
                .withDob(VALID_DOB_BOB).withPhone(VALID_PHONE_BOB).withAddress(VALID_ADDRESS_BOB).build();

        CommandResult result = new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder(edited).build()).execute(model);

        Patient stored = model.getAddressBook().getPatientList().get(0);
        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PATIENT_SUCCESS, Messages.format(edited)),
                result.getFeedbackToUser());
        assertEquals(edited, stored);
        assertEquals(List.of(medication), stored.getMedications());
        assertEquals(original.getAllergens(), stored.getAllergens());
        assertEquals(original.getCareInstructions(), stored.getCareInstructions());
        assertEquals(original.getContacts(), stored.getContacts());
        assertEquals(List.of(CHECKUP), stored.getAppointments());
        assertTrue(stored.isArchived());
        assertSame(stored, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_filteredList_editsDisplayedPatient() throws Exception {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build(),
                new UserPrefs());
        showPatientAtIndex(model, INDEX_SECOND_PATIENT);
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();

        new EditCommand(INDEX_FIRST_PATIENT, descriptor).execute(model);

        assertEquals(ALICE, model.getAddressBook().getPatientList().get(0));
        assertEquals(VALID_PHONE_BOB, model.getAddressBook().getPatientList().get(1).getPhone().value);
    }

    @Test
    public void execute_duplicateNric_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build(),
                new UserPrefs());
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder()
                .withNric(BENSON.getNric().value).build();

        assertCommandFailure(new EditCommand(INDEX_FIRST_PATIENT, descriptor), model,
                EditCommand.MESSAGE_DUPLICATE_PATIENT);
    }

    @Test
    public void execute_duplicateArchivedPatientNric_throwsArchivedCommandException() {
        Model model = new ModelManager(new AddressBookBuilder()
                .withPatient(ALICE).withPatient(BENSON.withArchived(true)).build(), new UserPrefs());
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder()
                .withNric(BENSON.getNric().value).build();

        assertCommandFailure(new EditCommand(INDEX_FIRST_PATIENT, descriptor), model,
                EditCommand.MESSAGE_DUPLICATE_ARCHIVED_PATIENT);
    }

    @Test
    public void execute_noFields_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).build(), new UserPrefs());

        assertCommandFailure(new EditCommand(INDEX_FIRST_PATIENT, new EditPatientDescriptor()), model,
                EditCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void execute_invalidFilteredIndex_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build(),
                new UserPrefs());
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder().withName(VALID_NAME_BOB).build();

        assertCommandFailure(new EditCommand(INDEX_SECOND_PATIENT, descriptor), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        EditPatientDescriptor name = new EditPatientDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditPatientDescriptor phone = new EditPatientDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand command = new EditCommand(INDEX_FIRST_PATIENT, name);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new EditCommand(INDEX_FIRST_PATIENT, name)));
        assertFalse(command.equals(new EditCommand(INDEX_SECOND_PATIENT, name)));
        assertFalse(command.equals(new EditCommand(INDEX_FIRST_PATIENT, phone)));
        assertFalse(command.equals(null));
    }

    @Test
    public void editPatientDescriptorEquals() {
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder(BENSON).build();

        assertTrue(descriptor.equals(descriptor));
        assertTrue(descriptor.equals(new EditPatientDescriptorBuilder(BENSON).build()));
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals(new Object()));
        assertFalse(descriptor.equals(new EditPatientDescriptorBuilder(BENSON)
                .withName(ALICE.getName().fullName).build()));
        assertFalse(descriptor.equals(new EditPatientDescriptorBuilder(BENSON)
                .withNric(ALICE.getNric().value).build()));
        assertFalse(descriptor.equals(new EditPatientDescriptorBuilder(BENSON)
                .withDob(VALID_DOB_BOB).build()));
        assertFalse(descriptor.equals(new EditPatientDescriptorBuilder(BENSON)
                .withPhone(ALICE.getPhone().value).build()));
        assertFalse(descriptor.equals(new EditPatientDescriptorBuilder(BENSON)
                .withAddress(ALICE.getAddress().value).build()));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder().withName(VALID_NAME_BOB).build();
        assertEquals(EditCommand.class.getCanonicalName() + "{index=" + index + ", editPatientDescriptor="
                + descriptor + "}", new EditCommand(index, descriptor).toString());
    }
}
