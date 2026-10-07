package seedu.address.testutil;

import java.util.List;

import seedu.address.model.patient.Address;
import seedu.address.model.patient.Name;
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
    private String nric;
    private String dob;
    private Phone phone;
    private Address address;
    private List<String> medications = List.of();
    private List<String> allergens = List.of();
    private List<String> careInstructions = List.of();

    /**
     * Creates a builder with default patient details and empty care lists.
     */
    public PatientBuilder() {
        name = new Name(DEFAULT_NAME);
        nric = DEFAULT_NRIC;
        dob = DEFAULT_DOB;
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
        this.nric = nric;
        return this;
    }

    /**
     * Sets the patient's dob.
     */
    public PatientBuilder withDob(String dob) {
        this.dob = dob;
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
     * Sets the patient's medications.
     */
    public PatientBuilder withMedications(String... entries) {
        this.medications = List.of(entries);
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
     * Builds an immutable patient.
     */
    public Patient build() {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions);
    }
}
