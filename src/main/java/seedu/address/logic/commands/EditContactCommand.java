package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Patient;

/**
 * Updates selected fields of a contact without changing its list position.
 */
public class EditContactCommand extends Command {

    public static final String COMMAND_WORD = "editcontact";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX CONTACT_INDEX [" + PREFIX_NAME + "NAME] ["
            + PREFIX_ROLE + "ROLE] [" + PREFIX_PHONE + "PHONE] [" + PREFIX_NOTE + "NOTE]";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a patient's contact.\n"
            + "Format: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 1 " + PREFIX_NOTE + "Call after lunch";
    public static final String MESSAGE_NOT_EDITED = "Specify at least one contact field to edit.";
    public static final String MESSAGE_SUCCESS = "Updated contact for %1$s: %2$s";

    private final Index patientIndex;
    private final Index contactIndex;
    private final Optional<String> name;
    private final Optional<String> role;
    private final Optional<String> phone;
    private final Optional<String> note;

    /**
     * Creates a command where an empty Optional preserves a field and a present empty note clears the note.
     */
    public EditContactCommand(Index patientIndex, Index contactIndex, Optional<String> name, Optional<String> role,
            Optional<String> phone, Optional<String> note) {
        requireAllNonNull(patientIndex, contactIndex, name, role, phone, note);
        this.patientIndex = patientIndex;
        this.contactIndex = contactIndex;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.note = note;
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
        Contact original = patient.getContacts().get(contactIndex.getZeroBased());
        Contact edited;
        try {
            edited = new Contact(name.orElse(original.getName()), role.orElse(original.getRole()),
                    phone.orElse(original.getPhone()), note.orElse(original.getNote()));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
        List<Contact> contacts = new ArrayList<>(patient.getContacts());
        for (int i = 0; i < contacts.size(); i++) {
            if (i != contactIndex.getZeroBased() && contacts.get(i).isSameContact(edited)) {
                throw new CommandException(Contact.MESSAGE_DUPLICATE);
            }
        }
        contacts.set(contactIndex.getZeroBased(), edited);
        model.setPatient(patient, patient.withContacts(contacts));
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName(), edited));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditContactCommand otherCommand)) {
            return false;
        }

        return patientIndex.equals(otherCommand.patientIndex)
                && contactIndex.equals(otherCommand.contactIndex)
                && name.equals(otherCommand.name)
                && role.equals(otherCommand.role)
                && phone.equals(otherCommand.phone)
                && note.equals(otherCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("patientIndex", patientIndex)
                .add("contactIndex", contactIndex)
                .add("name", name)
                .add("role", role)
                .add("phone", phone)
                .add("note", note)
                .toString();
    }
}
