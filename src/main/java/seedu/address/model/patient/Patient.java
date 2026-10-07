package seedu.address.model.patient;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Patient in the address book.
 * Guarantees: details are present and not null, immutable. NRIC and DOB are plain values pending field validation.
 */
public class Patient {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final String nric;
    private final String dob;

    // Data fields
    private final Address address;
    private final List<String> medications;
    private final List<String> allergens;
    private final List<String> careInstructions;

    /**
     * Every field must be present and not null.
     */
    public Patient(Name name, String nric, String dob, Phone phone, Address address,
            List<String> medications, List<String> allergens, List<String> careInstructions) {
        requireAllNonNull(name, nric, dob, phone, address, medications, allergens, careInstructions);
        this.name = name;
        this.nric = nric;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.medications = List.copyOf(medications);
        this.allergens = List.copyOf(allergens);
        this.careInstructions = List.copyOf(careInstructions);
    }

    /**
     * Creates a patient with empty care lists.
     */
    public Patient(Name name, String nric, String dob, Phone phone, Address address) {
        this(name, nric, dob, phone, address, List.of(), List.of(), List.of());
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public String getNric() {
        return nric;
    }

    public String getDob() {
        return dob;
    }

    public Address getAddress() {
        return address;
    }

    public List<String> getMedications() {
        return medications;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public List<String> getCareInstructions() {
        return careInstructions;
    }

    /**
     * Returns true if both patients have the same NRIC (case-insensitive).
     * This defines a weaker notion of equality between two patients.
     */
    public boolean isSamePatient(Patient otherPatient) {
        if (otherPatient == this) {
            return true;
        }

        return otherPatient != null
                && otherPatient.getNric().equalsIgnoreCase(getNric());
    }

    /**
     * Returns true if both patients have the same identity and data fields.
     * This defines a stronger notion of equality between two patients.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Patient otherPatient)) {
            return false;
        }

        return name.equals(otherPatient.name)
                && phone.equals(otherPatient.phone)
                && nric.equals(otherPatient.nric)
                && dob.equals(otherPatient.dob)
                && address.equals(otherPatient.address)
                && medications.equals(otherPatient.medications)
                && allergens.equals(otherPatient.allergens)
                && careInstructions.equals(otherPatient.careInstructions);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, nric, dob, phone, address, medications, allergens, careInstructions);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("nric", nric)
                .add("dob", dob)
                .add("address", address)
                .add("medications", medications)
                .add("allergens", allergens)
                .add("careInstructions", careInstructions)
                .toString();
    }

}
