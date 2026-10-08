package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.patient.NameContainsKeywordsPredicate;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPatient_nullPatient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPatient(null));
    }

    @Test
    public void hasPatient_patientNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPatient(ALICE));
    }

    @Test
    public void hasPatient_patientInAddressBook_returnsTrue() {
        modelManager.addPatient(ALICE);
        assertTrue(modelManager.hasPatient(ALICE));
    }

    @Test
    public void getFilteredPatientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPatientList().remove(0));
    }

    @Test
    public void getViewedPatient_noPatientViewed_returnsNull() {
        assertNull(modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setViewedPatient_nullPatient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setViewedPatient(null));
    }

    @Test
    public void setViewedPatient_patientNotInAddressBook_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> modelManager.setViewedPatient(ALICE));
    }

    @Test
    public void setViewedPatient_patientInAddressBook_setsViewedPatient() {
        modelManager.addPatient(ALICE);
        modelManager.addPatient(BENSON);

        modelManager.setViewedPatient(ALICE);
        assertEquals(ALICE, modelManager.getViewedPatient().getValue());

        // viewing another patient replaces the viewed patient
        modelManager.setViewedPatient(BENSON);
        assertEquals(BENSON, modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setPatient_viewedPatientReplaced_viewedPatientUpdated() {
        modelManager.addPatient(ALICE);
        modelManager.setViewedPatient(ALICE);

        Patient editedAlice = new PatientBuilder(ALICE).withMedications("Metformin 500mg").build();
        modelManager.setPatient(ALICE, editedAlice);
        assertEquals(editedAlice, modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setPatient_otherPatientReplaced_viewedPatientUnchanged() {
        modelManager.addPatient(ALICE);
        modelManager.addPatient(BENSON);
        modelManager.setViewedPatient(ALICE);

        Patient editedBenson = new PatientBuilder(BENSON).withMedications("Metformin 500mg").build();
        modelManager.setPatient(BENSON, editedBenson);
        assertEquals(ALICE, modelManager.getViewedPatient().getValue());
    }

    @Test
    public void deletePatient_viewedPatientDeleted_viewedPatientCleared() {
        modelManager.addPatient(ALICE);
        modelManager.setViewedPatient(ALICE);

        modelManager.deletePatient(ALICE);
        assertNull(modelManager.getViewedPatient().getValue());
    }

    @Test
    public void deletePatient_otherPatientDeleted_viewedPatientUnchanged() {
        modelManager.addPatient(ALICE);
        modelManager.addPatient(BENSON);
        modelManager.setViewedPatient(ALICE);

        modelManager.deletePatient(BENSON);
        assertEquals(ALICE, modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setAddressBook_emptyAddressBook_viewedPatientCleared() {
        modelManager.addPatient(ALICE);
        modelManager.setViewedPatient(ALICE);

        modelManager.setAddressBook(new AddressBook());
        assertNull(modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setAddressBook_viewedPatientDetailsChanged_viewedPatientCleared() {
        modelManager.addPatient(ALICE);
        modelManager.setViewedPatient(ALICE);

        Patient editedAlice = new PatientBuilder(ALICE).withPhone("91111111").build();
        modelManager.setAddressBook(new AddressBookBuilder().withPatient(editedAlice).build());
        assertNull(modelManager.getViewedPatient().getValue());
    }

    @Test
    public void setAddressBook_viewedPatientStillPresent_viewedPatientUnchanged() {
        modelManager.addPatient(ALICE);
        modelManager.setViewedPatient(ALICE);

        modelManager.setAddressBook(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build());
        assertEquals(ALICE, modelManager.getViewedPatient().getValue());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPatientList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void equals_viewedPatient() {
        AddressBook addressBook = new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        ModelManager modelManagerCopy = new ModelManager(addressBook, new UserPrefs());

        // different viewedPatient -> returns false
        modelManagerCopy.setViewedPatient(ALICE);
        assertFalse(modelManager.equals(modelManagerCopy));

        // same viewedPatient -> returns true
        modelManager.setViewedPatient(ALICE);
        assertTrue(modelManager.equals(modelManagerCopy));

        // different patients viewed -> returns false
        modelManager.setViewedPatient(BENSON);
        assertFalse(modelManager.equals(modelManagerCopy));
    }
}
