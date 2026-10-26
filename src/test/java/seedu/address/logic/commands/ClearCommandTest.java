package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PatientBuilder;

public class ClearCommandTest {

    private static final String EXPECTED_CLEAR_SUCCESS_MESSAGE = "All patients have been cleared.";

    private static final String BACKUP_FILE_NAME = "mediconnect-20261018-120030.json";

    @TempDir
    private Path backupDir;

    // Checks the required wording independently of the production message constant.
    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(FIXED_CLOCK, backupDir), model, expectedSuccessMessage(), expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(FIXED_CLOCK, backupDir), model, expectedSuccessMessage(), expectedModel);
    }

    @Test
    public void execute_activeAndArchivedPatients_clearsAllPatients() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addPatient(new PatientBuilder().withArchived(true).build());

        assertCommandSuccess(new ClearCommand(FIXED_CLOCK, backupDir), model, expectedSuccessMessage(),
                new ModelManager());
    }

    @Test
    public void execute_viewedPatient_clearsPatientsAndViewedPatient() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.setViewedPatient(model.getFilteredPatientList().get(0));
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(FIXED_CLOCK, backupDir), model, expectedSuccessMessage(), expectedModel);
        assertNull(model.getViewedPatient().getValue());
    }

    @Test
    public void execute_activeAndArchivedPatients_backupCanRestoreAllData() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addPatient(new PatientBuilder().withArchived(true).build());
        AddressBook original = new AddressBook(model.getAddressBook());

        new ClearCommand(FIXED_CLOCK, backupDir).execute(model);

        assertEquals(original, new JsonAddressBookStorage(backupDir.resolve(BACKUP_FILE_NAME))
                .readAddressBook().orElseThrow());
        new RestoreCommand(BACKUP_FILE_NAME, FIXED_CLOCK, backupDir).execute(model);
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_repeatedClear_keepsOriginalBackup() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        AddressBook original = new AddressBook(model.getAddressBook());
        ClearCommand command = new ClearCommand(FIXED_CLOCK, backupDir);

        command.execute(model);
        command.execute(model);

        assertEquals(original, new JsonAddressBookStorage(backupDir.resolve(BACKUP_FILE_NAME))
                .readAddressBook().orElseThrow());
        assertTrue(new JsonAddressBookStorage(backupDir.resolve("mediconnect-20261018-120030-1.json"))
                .readAddressBook().orElseThrow().getPatientList().isEmpty());
    }

    @Test
    public void execute_backupFails_preservesPatientsFilterAndViewedPatient() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addPatient(new PatientBuilder().withArchived(true).build());
        AddressBook original = new AddressBook(model.getAddressBook());
        Patient viewedPatient = model.getFilteredPatientList().get(0);
        model.updateFilteredPatientList(patient -> patient.equals(viewedPatient));
        model.setViewedPatient(viewedPatient);
        Path blockedDirectory = Files.createFile(backupDir.resolve("not-a-directory"));

        assertThrows(CommandException.class, () ->
                new ClearCommand(FIXED_CLOCK, blockedDirectory).execute(model));

        assertEquals(original, model.getAddressBook());
        assertEquals(List.of(viewedPatient), model.getFilteredPatientList());
        assertEquals(viewedPatient, model.getViewedPatient().getValue());
    }

    private String expectedSuccessMessage() {
        return EXPECTED_CLEAR_SUCCESS_MESSAGE + "\n"
                + String.format(BackupCommand.MESSAGE_SUCCESS, backupDir.resolve(BACKUP_FILE_NAME));
    }

}
