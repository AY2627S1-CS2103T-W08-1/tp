package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.ARCHIVED_ALICE;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PatientBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsNoPatientsMessage() {
        Model emptyModel = new ModelManager();
        Model expectedEmptyModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel, ListCommand.MESSAGE_NO_PATIENTS, expectedEmptyModel);
    }

    @Test
    public void execute_listContainsArchivedPatient_showsOnlyActivePatients() {
        var addressBook = new AddressBookBuilder().withPatient(ALICE)
                .withPatient(new PatientBuilder(ARCHIVED_ALICE).withNric("S1111111A").build())
                .build();
        Model modelWithArchivedPatient = new ModelManager(addressBook, new UserPrefs());
        modelWithArchivedPatient.updateFilteredPatientList(patient -> true);
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());

        assertCommandSuccess(new ListCommand(), modelWithArchivedPatient, ListCommand.MESSAGE_SUCCESS, expectedModel);
        assertEquals(List.of(ALICE), modelWithArchivedPatient.getFilteredPatientList());
    }

    @Test
    public void execute_onlyArchivedPatients_showsNoActivePatientsMessage() {
        var addressBook = new AddressBookBuilder().withPatient(ARCHIVED_ALICE).build();
        Model modelWithArchivedPatient = new ModelManager(addressBook, new UserPrefs());

        assertCommandSuccess(new ListCommand(), modelWithArchivedPatient, ListCommand.MESSAGE_NO_PATIENTS,
                new ModelManager(addressBook, new UserPrefs()));
    }
}
