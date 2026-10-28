package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

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
 * Adds one contact to a patient in the displayed list.
 */
public class AddContactCommand extends Command {

    public static final String COMMAND_WORD = "addcontact";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX " + PREFIX_NAME + "NAME " + PREFIX_ROLE + "ROLE "
            + PREFIX_PHONE + "PHONE [" + PREFIX_NOTE + "NOTE]";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact to a patient.\n"
            + "Parameters: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_NAME + "Dr Tan " + PREFIX_ROLE + "GP "
            + PREFIX_PHONE + "+65 6123-4567 " + PREFIX_NOTE + "Call in the morning";
    public static final String MESSAGE_SUCCESS = "Added contact for %1$s: %2$s";

    private final Index patientIndex;
    private final Contact contact;

    /**
     * Creates a command to append the given contact.
     */
    public AddContactCommand(Index patientIndex, Contact contact) {
        requireAllNonNull(patientIndex, contact);
        this.patientIndex = patientIndex;
        this.contact = contact;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> patients = model.getFilteredPatientList();
        if (patientIndex.getZeroBased() >= patients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }
        Patient patient = patients.get(patientIndex.getZeroBased());
        if (patient.getContacts().stream().anyMatch(contact::isSameContact)) {
            throw new CommandException(Contact.MESSAGE_DUPLICATE);
        }
        List<Contact> contacts = new ArrayList<>(patient.getContacts());
        contacts.add(contact);
        Patient editedPatient = patient.withContacts(contacts);
        model.setPatient(patient, editedPatient);
        assert model.getAddressBook().getPatientList().contains(editedPatient)
                : "The model must store the patient with the added contact.";
        model.setViewedPatient(editedPatient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName(), contact));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddContactCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && contact.equals(otherCommand.contact);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("contact", contact)
                .toString();
    }
}
