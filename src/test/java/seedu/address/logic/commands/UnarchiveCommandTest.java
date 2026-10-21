package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.DANIEL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;

public class UnarchiveCommandTest {

    @Test
    public void execute_validIndex_preservesAllFieldsAndRefreshesViewedPatient() throws Exception {
        Patient archivedDaniel = DANIEL.withArchived(true);
        Model model = new ModelManager(new AddressBookBuilder().withPatient(archivedDaniel).build(), new UserPrefs());
        new ListArchivedCommand().execute(model);
        model.setViewedPatient(archivedDaniel);

        CommandResult result = new UnarchiveCommand(INDEX_FIRST_PATIENT).execute(model);

        Patient activeDaniel = model.getAddressBook().getPatientList().get(0);
        assertEquals(String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PATIENT_SUCCESS,
                DANIEL.getName(), DANIEL.getNric()), result.getFeedbackToUser());
        assertEquals(DANIEL, activeDaniel);
        assertTrue(model.getFilteredPatientList().isEmpty());
        assertSame(activeDaniel, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_filteredList_unarchivesDisplayedPatient() throws Exception {
        Patient archivedAlice = ALICE.withArchived(true);
        Patient archivedBenson = BENSON.withArchived(true);
        Model model = new ModelManager(new AddressBookBuilder()
                .withPatient(archivedAlice).withPatient(archivedBenson).build(), new UserPrefs());
        model.updateFilteredPatientList(patient -> patient.getName().equals(BENSON.getName()));

        new UnarchiveCommand(INDEX_FIRST_PATIENT).execute(model);

        assertTrue(model.getAddressBook().getPatientList().get(0).isArchived());
        assertFalse(model.getAddressBook().getPatientList().get(1).isArchived());
        assertEquals(List.of(model.getAddressBook().getPatientList().get(1)), model.getFilteredPatientList());
    }

    @Test
    public void execute_outOfRangeIndex_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE.withArchived(true)).build(),
                new UserPrefs());
        new ListArchivedCommand().execute(model);

        assertCommandFailure(new UnarchiveCommand(INDEX_SECOND_PATIENT), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PATIENT), new ModelManager(),
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyActivePatient_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).build(), new UserPrefs());

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PATIENT), model,
                UnarchiveCommand.MESSAGE_PATIENT_ALREADY_ACTIVE);
    }

    @Test
    public void equals() {
        UnarchiveCommand unarchiveFirst = new UnarchiveCommand(INDEX_FIRST_PATIENT);
        UnarchiveCommand unarchiveFirstCopy = new UnarchiveCommand(INDEX_FIRST_PATIENT);
        UnarchiveCommand unarchiveSecond = new UnarchiveCommand(INDEX_SECOND_PATIENT);

        assertTrue(unarchiveFirst.equals(unarchiveFirst));
        assertTrue(unarchiveFirst.equals(unarchiveFirstCopy));
        assertFalse(unarchiveFirst.equals(unarchiveSecond));
        assertFalse(unarchiveFirst.equals(null));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        assertEquals(UnarchiveCommand.class.getCanonicalName() + "{targetIndex=" + index + "}",
                new UnarchiveCommand(index).toString());
    }
}
