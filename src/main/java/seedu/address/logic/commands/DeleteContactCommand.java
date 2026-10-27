package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Patient;

/**
 * Removes a contact identified by its number in the detail panel.
 */
public class DeleteContactCommand extends Command {

    public static final String COMMAND_WORD = "deletecontact";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX CONTACT_INDEX";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes a patient's contact.\n"
            + "Parameters: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_SUCCESS = "Deleted contact for %1$s: %2$s";

    private final Index patientIndex;
    private final Index contactIndex;

    /**
     * Creates a command for the two displayed indices.
     */
    public DeleteContactCommand(Index patientIndex, Index contactIndex) {
        requireAllNonNull(patientIndex, contactIndex);
        this.patientIndex = patientIndex;
        this.contactIndex = contactIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> patients = model.getFilteredPatientList();
        if (patientIndex.getZeroBased() >= patients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patient = patients.get(patientIndex.getZeroBased());
        if (contactIndex.getZeroBased() >= patient.getContacts().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_CONTACT_INDEX);
        }
        List<Contact> contacts = new ArrayList<>(patient.getContacts());
        Contact removed = contacts.remove(contactIndex.getZeroBased());
        Patient updatedPatient = patient.withContacts(contacts);
        model.setPatient(patient, updatedPatient);
        assert model.getAddressBook().getPatientList().contains(updatedPatient)
                : "The model must store the patient with the contact removed.";
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName(), removed));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteContactCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && contactIndex.equals(otherCommand.contactIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("contactIndex", contactIndex)
                .toString();
    }
}
