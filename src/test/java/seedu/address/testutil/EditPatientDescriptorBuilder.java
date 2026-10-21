package seedu.address.testutil;

import seedu.address.logic.commands.EditCommand.EditPatientDescriptor;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Builds edit-patient descriptors for tests.
 */
public class EditPatientDescriptorBuilder {

    private final EditPatientDescriptor descriptor;

    public EditPatientDescriptorBuilder() {
        descriptor = new EditPatientDescriptor();
    }

    /**
     * Creates a builder containing all editable fields of {@code patient}.
     */
    public EditPatientDescriptorBuilder(Patient patient) {
        this();
        descriptor.setName(patient.getName());
        descriptor.setNric(patient.getNric());
        descriptor.setDob(patient.getDob());
        descriptor.setPhone(patient.getPhone());
        descriptor.setAddress(patient.getAddress());
    }

    /** Sets the replacement name. */
    public EditPatientDescriptorBuilder withName(String name) {
        descriptor.setName(new Name(name));
        return this;
    }

    /** Sets the replacement NRIC. */
    public EditPatientDescriptorBuilder withNric(String nric) {
        descriptor.setNric(new Nric(nric));
        return this;
    }

    /** Sets the replacement date of birth. */
    public EditPatientDescriptorBuilder withDob(String dob) {
        descriptor.setDob(new Dob(dob));
        return this;
    }

    /** Sets the replacement phone number. */
    public EditPatientDescriptorBuilder withPhone(String phone) {
        descriptor.setPhone(new Phone(phone));
        return this;
    }

    /** Sets the replacement address. */
    public EditPatientDescriptorBuilder withAddress(String address) {
        descriptor.setAddress(new Address(address));
        return this;
    }

    public EditPatientDescriptor build() {
        return descriptor;
    }
}
