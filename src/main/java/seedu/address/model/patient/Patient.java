package seedu.address.model.patient;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;

/**
 * Represents a Patient in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Patient {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Nric nric;
    private final Dob dob;

    // Data fields
    private final Address address;
    private final List<String> medications;
    private final List<String> allergens;
    private final List<String> careInstructions;
    private final List<Appointment> appointments;

    /**
     * Every field must be present and not null.
     * Appointments are kept sorted by date, then time.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address, List<String> medications,
            List<String> allergens, List<String> careInstructions, List<Appointment> appointments) {
        requireAllNonNull(name, nric, dob, phone, address, medications, allergens, careInstructions,
                appointments);
        this.name = name;
        this.nric = nric;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.medications = List.copyOf(medications);
        this.allergens = List.copyOf(allergens);
        this.careInstructions = List.copyOf(careInstructions);
        List<Appointment> sortedAppointments = new ArrayList<>(appointments);
        Collections.sort(sortedAppointments);
        this.appointments = List.copyOf(sortedAppointments);
    }

    /**
     * Creates a patient with empty care lists and no appointments.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address) {
        this(name, nric, dob, phone, address, List.of(), List.of(), List.of(), List.of());
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Nric getNric() {
        return nric;
    }

    public Dob getDob() {
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

    public List<Appointment> getAppointments() {
        return appointments;
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
                && otherPatient.getNric().equals(getNric());
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
                && careInstructions.equals(otherPatient.careInstructions)
                && appointments.equals(otherPatient.appointments);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, nric, dob, phone, address, medications, allergens, careInstructions,
                appointments);
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
                .add("appointments", appointments)
                .toString();
    }

}
