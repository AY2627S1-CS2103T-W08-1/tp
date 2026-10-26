package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code ViewCommand}.
 */
public class ViewCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        assertViewSuccess(INDEX_FIRST_PATIENT, new ModelManager(model.getAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_validIndex_showsPatientName() throws Exception {
        CommandResult result = new ViewCommand(INDEX_FIRST_PATIENT).execute(model);
        assertEquals("Showing patient: Alice Pauline", result.getFeedbackToUser());
    }

    @Test
    public void execute_lastIndexUnfilteredList_success() {
        Index lastIndex = Index.fromOneBased(model.getFilteredPatientList().size());

        assertViewSuccess(lastIndex, new ModelManager(model.getAddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPatientList().size() + 1);
        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPatientAtIndex(expectedModel, INDEX_FIRST_PATIENT);

        assertViewSuccess(INDEX_FIRST_PATIENT, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);

        Index outOfBoundIndex = INDEX_SECOND_PATIENT;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPatientList().size());

        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexWhileViewingPatient_viewedPatientUnchanged() {
        Patient viewedPatient = model.getFilteredPatientList().get(INDEX_FIRST_PATIENT.getZeroBased());
        model.setViewedPatient(viewedPatient);

        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPatientList().size() + 1);
        ViewCommand viewCommand = new ViewCommand(outOfBoundIndex);

        assertCommandFailure(viewCommand, model, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertEquals(viewedPatient, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PATIENT);

        assertCommandFailure(viewCommand, emptyModel, Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PATIENT);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PATIENT);

        // same object -> returns true
        assertTrue(viewFirstCommand.equals(viewFirstCommand));

        // same values -> returns true
        ViewCommand viewFirstCommandCopy = new ViewCommand(INDEX_FIRST_PATIENT);
        assertTrue(viewFirstCommand.equals(viewFirstCommandCopy));

        // different types -> returns false
        assertFalse(viewFirstCommand.equals(1));

        // null -> returns false
        assertFalse(viewFirstCommand.equals(null));

        // different patient -> returns false
        assertFalse(viewFirstCommand.equals(viewSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        ViewCommand viewCommand = new ViewCommand(targetIndex);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, viewCommand.toString());
    }

    /**
     * Confirms that viewing the patient at {@code index} of {@code model}'s displayed list succeeds, and leaves
     * {@code model} the same as {@code expectedModel} with that patient viewed.
     *
     * @param index Index of the patient in the displayed list of {@code model}.
     * @param expectedModel Model with the same patients and displayed list as {@code model}, before the patient is
     *     viewed.
     */
    private void assertViewSuccess(Index index, Model expectedModel) {
        Patient patientToView = model.getFilteredPatientList().get(index.getZeroBased());
        String expectedMessage = String.format(ViewCommand.MESSAGE_VIEW_PATIENT_SUCCESS, patientToView.getName());
        expectedModel.setViewedPatient(patientToView);

        assertCommandSuccess(new ViewCommand(index), model, expectedMessage, expectedModel);
    }
}
