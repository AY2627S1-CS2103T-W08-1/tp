package seedu.address.testutil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Builds patients for tests.
 */
public class PatientBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_NRIC = "S9999999A";
    public static final String DEFAULT_DOB = "1950-01-01";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private Name name;
    private Nric nric;
    private Dob dob;
    private Phone phone;
    private Address address;
    private List<Medication> medications = List.of();
    private List<String> allergens = List.of();
    private List<String> careInstructions = List.of();
    private List<Contact> contacts = List.of();
    private List<Appointment> appointments = List.of();

    /**
     * Creates a builder with default patient details and empty care lists.
     */
    public PatientBuilder() {
        name = new Name(DEFAULT_NAME);
        nric = new Nric(DEFAULT_NRIC);
        dob = new Dob(DEFAULT_DOB);
        phone = new Phone(DEFAULT_PHONE);
        address = new Address(DEFAULT_ADDRESS);
    }

    /**
     * Copies the details of a patient.
     */
    public PatientBuilder(Patient patientToCopy) {
        name = patientToCopy.getName();
        nric = patientToCopy.getNric();
        dob = patientToCopy.getDob();
        phone = patientToCopy.getPhone();
        address = patientToCopy.getAddress();
        medications = patientToCopy.getMedications();
        allergens = patientToCopy.getAllergens();
        careInstructions = patientToCopy.getCareInstructions();
        contacts = patientToCopy.getContacts();
        appointments = patientToCopy.getAppointments();
    }

    /**
     * Sets the patient's name.
     */
    public PatientBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Sets the patient's nric.
     */
    public PatientBuilder withNric(String nric) {
        this.nric = new Nric(nric);
        return this;
    }

    /**
     * Sets the patient's dob.
     */
    public PatientBuilder withDob(String dob) {
        this.dob = new Dob(dob);
        return this;
    }

    /**
     * Sets the patient's phone.
     */
    public PatientBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the patient's address.
     */
    public PatientBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the patient's medications to ones with the given descriptions and no times.
     */
    public PatientBuilder withMedications(String... descriptions) {
        this.medications = Arrays.stream(descriptions).map(Medication::new).collect(Collectors.toList());
        return this;
    }

    /**
     * Sets the patient's medications.
     */
    public PatientBuilder withMedications(Medication... medications) {
        this.medications = List.of(medications);
        return this;
    }

    /**
     * Sets the patient's allergens.
     */
    public PatientBuilder withAllergens(String... entries) {
        this.allergens = List.of(entries);
        return this;
    }

    /**
     * Sets the patient's careInstructions.
     */
    public PatientBuilder withCareInstructions(String... entries) {
        this.careInstructions = List.of(entries);
        return this;
    }

    /**
     * Sets the patient's contacts in insertion order.
     */
    public PatientBuilder withContacts(Contact... contacts) {
        this.contacts = List.of(contacts);
        return this;
    }

    /**
     * Sets the patient's appointments.
     */
    public PatientBuilder withAppointments(Appointment... appointments) {
        this.appointments = List.of(appointments);
        return this;
    }

    /**
     * Builds an immutable patient.
     */
    public Patient build() {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments);
    }
}
