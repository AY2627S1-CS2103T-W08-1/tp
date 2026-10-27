package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PATIENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ARCHIVED_PATIENTS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Edits the core details of a patient identified using its displayed index.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX [" + PREFIX_NAME + "NAME] ["
            + PREFIX_NRIC + "NRIC] [" + PREFIX_DOB + "DOB] [" + PREFIX_PHONE + "PHONE] ["
            + PREFIX_ADDRESS + "ADDRESS]";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a patient's details.\n"
            + "Parameters: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_PHONE + "91234567";
    public static final String MESSAGE_EDIT_PATIENT_SUCCESS = "Edited patient: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PATIENT = AddCommand.MESSAGE_DUPLICATE_PATIENT;
    public static final String MESSAGE_DUPLICATE_ARCHIVED_PATIENT = AddCommand.MESSAGE_DUPLICATE_ARCHIVED_PATIENT;

    private final Index index;
    private final EditPatientDescriptor editPatientDescriptor;

    /**
     * Creates an edit command for the patient at {@code index}.
     */
    public EditCommand(Index index, EditPatientDescriptor editPatientDescriptor) {
        requireNonNull(index);
        requireNonNull(editPatientDescriptor);
        this.index = index;
        this.editPatientDescriptor = new EditPatientDescriptor(editPatientDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!editPatientDescriptor.isAnyFieldEdited()) {
            throw new CommandException(MESSAGE_NOT_EDITED);
        }

        List<Patient> lastShownList = model.getFilteredPatientList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }

        Patient patientToEdit = lastShownList.get(index.getZeroBased());
        Patient editedPatient = createEditedPatient(patientToEdit, editPatientDescriptor);
        if (!patientToEdit.isSamePatient(editedPatient) && model.hasPatient(editedPatient)) {
            String message = AddCommand.hasArchivedDuplicate(model, editedPatient)
                    ? MESSAGE_DUPLICATE_ARCHIVED_PATIENT
                    : MESSAGE_DUPLICATE_PATIENT;
            throw new CommandException(message);
        }

        model.setPatient(patientToEdit, editedPatient);
        model.updateFilteredPatientList(editedPatient.isArchived()
                ? PREDICATE_SHOW_ARCHIVED_PATIENTS : PREDICATE_SHOW_ACTIVE_PATIENTS);
        model.setViewedPatient(editedPatient);
        return new CommandResult(String.format(MESSAGE_EDIT_PATIENT_SUCCESS, Messages.format(editedPatient)));
    }

    private static Patient createEditedPatient(Patient patient, EditPatientDescriptor descriptor) {
        Name name = descriptor.getName().orElse(patient.getName());
        Nric nric = descriptor.getNric().orElse(patient.getNric());
        Dob dob = descriptor.getDob().orElse(patient.getDob());
        Phone phone = descriptor.getPhone().orElse(patient.getPhone());
        Address address = descriptor.getAddress().orElse(patient.getAddress());
        return patient.withDetails(name, nric, dob, phone, address);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof EditCommand otherEditCommand
                && index.equals(otherEditCommand.index)
                && editPatientDescriptor.equals(otherEditCommand.editPatientDescriptor));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPatientDescriptor", editPatientDescriptor)
                .toString();
    }

    /**
     * Stores the patient fields to replace. Null fields are left unchanged.
     */
    public static class EditPatientDescriptor {
        private Name name;
        private Nric nric;
        private Dob dob;
        private Phone phone;
        private Address address;

        public EditPatientDescriptor() {}

        /**
         * Creates a defensive copy of {@code toCopy}.
         */
        public EditPatientDescriptor(EditPatientDescriptor toCopy) {
            requireNonNull(toCopy);
            name = toCopy.name;
            nric = toCopy.nric;
            dob = toCopy.dob;
            phone = toCopy.phone;
            address = toCopy.address;
        }

        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, nric, dob, phone, address);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setNric(Nric nric) {
            this.nric = nric;
        }

        public Optional<Nric> getNric() {
            return Optional.ofNullable(nric);
        }

        public void setDob(Dob dob) {
            this.dob = dob;
        }

        public Optional<Dob> getDob() {
            return Optional.ofNullable(dob);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        @Override
        public boolean equals(Object other) {
            return other == this
                    || (other instanceof EditPatientDescriptor otherDescriptor
                    && Objects.equals(name, otherDescriptor.name)
                    && Objects.equals(nric, otherDescriptor.nric)
                    && Objects.equals(dob, otherDescriptor.dob)
                    && Objects.equals(phone, otherDescriptor.phone)
                    && Objects.equals(address, otherDescriptor.address));
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("nric", nric)
                    .add("dob", dob)
                    .add("phone", phone)
                    .add("address", address)
                    .toString();
        }
    }
}
