package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CancelApptCommand.MESSAGE_SUCCESS;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.createModelWithAppointments;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CARDIOLOGY;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;

public class CancelApptCommandTest {

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(null, INDEX_FIRST_PATIENT));
        assertThrows(NullPointerException.class, () -> new CancelApptCommand(INDEX_FIRST_PATIENT, null));
    }

    @Test
    public void execute_validIndexes_removesAppointment() {
        Model model = createModelWithAppointments(CHECKUP, CARDIOLOGY);
        Model expectedModel = createModelWithAppointments(CHECKUP);

        assertCommandSuccess(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), model,
                String.format(MESSAGE_SUCCESS, ALICE.getName(), CARDIOLOGY), expectedModel);
    }

    @Test
    public void execute_appointmentIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
        assertCommandFailure(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(), Messages.MESSAGE_INVALID_APPOINTMENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_patientIndexOutOfRange_throwsCommandException() {
        assertCommandFailure(new CancelApptCommand(INDEX_SECOND_PATIENT, INDEX_FIRST_PATIENT),
                createModelWithAppointments(CHECKUP), Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        CancelApptCommand command = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);

        // same values -> returns true
        assertTrue(command.equals(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT)));

        // same object -> returns true
        assertTrue(command.equals(command));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different patient or appointment index -> returns false
        assertFalse(command.equals(new CancelApptCommand(INDEX_SECOND_PATIENT, INDEX_SECOND_PATIENT)));
        assertFalse(command.equals(new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT)));
    }

    @Test
    public void toStringMethod() {
        CancelApptCommand command = new CancelApptCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT);
        String expected = CancelApptCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PATIENT + ", appointmentIndex=" + INDEX_SECOND_PATIENT + "}";
        assertEquals(expected, command.toString());
    }
}
