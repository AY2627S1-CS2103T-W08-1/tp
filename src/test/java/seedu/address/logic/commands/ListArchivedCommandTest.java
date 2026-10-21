package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.AddressBookBuilder;

public class ListArchivedCommandTest {

    @Test
    public void execute_filteredMixedList_resetsToArchivedOnlyAndDefinesIndices() throws Exception {
        Patient archivedBenson = BENSON.withArchived(true);
        Patient archivedCarl = CARL.withArchived(true);
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE)
                .withPatient(archivedBenson).withPatient(archivedCarl).build(), new UserPrefs());
        model.updateFilteredPatientList(patient -> false);

        CommandResult result = new ListArchivedCommand().execute(model);

        assertEquals(ListArchivedCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(archivedBenson, archivedCarl), model.getFilteredPatientList());

        new UnarchiveCommand(INDEX_SECOND_PATIENT).execute(model);
        assertEquals(List.of(archivedBenson), model.getFilteredPatientList());
        assertFalse(model.getAddressBook().getPatientList().get(2).isArchived());
        assertTrue(model.getAddressBook().getPatientList().get(1).isArchived());
    }

    @Test
    public void execute_noArchivedPatients_showsEmptyMessage() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).build(), new UserPrefs());

        CommandResult result = new ListArchivedCommand().execute(model);

        assertEquals(ListArchivedCommand.MESSAGE_NO_PATIENTS, result.getFeedbackToUser());
        assertTrue(model.getFilteredPatientList().isEmpty());
    }
}
