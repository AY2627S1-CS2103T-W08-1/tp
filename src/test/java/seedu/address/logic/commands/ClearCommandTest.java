package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, "All patients have been cleared.", expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(), model, "All patients have been cleared.", expectedModel);
    }

    @Test
    public void execute_viewedPatient_clearsPatientsAndViewedPatient() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, "All patients have been cleared.", expectedModel);
        assertNull(model.getViewedPatient().getValue());
    }

}
