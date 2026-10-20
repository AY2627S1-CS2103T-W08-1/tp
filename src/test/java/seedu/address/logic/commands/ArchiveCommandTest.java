package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ARCHIVED_PATIENTS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.ARCHIVED_ALICE;
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

public class ArchiveCommandTest {

    @Test
    public void execute_validIndex_preservesAllFieldsAndRefreshesViewedPatient() throws Exception {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(DANIEL).build(), new UserPrefs());
        model.setViewedPatient(DANIEL);

        CommandResult result = new ArchiveCommand(INDEX_FIRST_PATIENT).execute(model);

        Patient archivedDaniel = model.getAddressBook().getPatientList().get(0);
        assertEquals(String.format(ArchiveCommand.MESSAGE_ARCHIVE_PATIENT_SUCCESS,
                DANIEL.getName(), DANIEL.getNric()), result.getFeedbackToUser());
        assertEquals(DANIEL.withArchived(true), archivedDaniel);
        assertTrue(model.getFilteredPatientList().isEmpty());
        assertSame(archivedDaniel, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_filteredList_archivesDisplayedPatient() throws Exception {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build(),
                new UserPrefs());
        showPatientAtIndex(model, INDEX_SECOND_PATIENT);

        new ArchiveCommand(INDEX_FIRST_PATIENT).execute(model);

        assertFalse(model.getAddressBook().getPatientList().get(0).isArchived());
        assertTrue(model.getAddressBook().getPatientList().get(1).isArchived());
        assertEquals(List.of(model.getAddressBook().getPatientList().get(1)), model.getFilteredPatientList());
    }

    @Test
    public void execute_outOfRangeIndex_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ALICE).build(), new UserPrefs());

        assertCommandFailure(new ArchiveCommand(INDEX_SECOND_PATIENT), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PATIENT), new ModelManager(),
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyArchivedPatient_throwsCommandException() {
        Model model = new ModelManager(new AddressBookBuilder().withPatient(ARCHIVED_ALICE).build(), new UserPrefs());
        model.updateFilteredPatientList(PREDICATE_SHOW_ARCHIVED_PATIENTS);

        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PATIENT), model,
                ArchiveCommand.MESSAGE_PATIENT_ALREADY_ARCHIVED);
    }

    @Test
    public void equals() {
        ArchiveCommand archiveFirst = new ArchiveCommand(INDEX_FIRST_PATIENT);
        ArchiveCommand archiveFirstCopy = new ArchiveCommand(INDEX_FIRST_PATIENT);
        ArchiveCommand archiveSecond = new ArchiveCommand(INDEX_SECOND_PATIENT);

        assertTrue(archiveFirst.equals(archiveFirst));
        assertTrue(archiveFirst.equals(archiveFirstCopy));
        assertFalse(archiveFirst.equals(archiveSecond));
        assertFalse(archiveFirst.equals(null));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        assertEquals(ArchiveCommand.class.getCanonicalName() + "{targetIndex=" + index + "}",
                new ArchiveCommand(index).toString());
    }
}
