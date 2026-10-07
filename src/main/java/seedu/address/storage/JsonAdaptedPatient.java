package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Jackson-friendly version of {@link Patient}.
 */
class JsonAdaptedPatient {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String nric;
    private final String dob;
    private final String address;
    private final List<String> medications = new ArrayList<>();
    private final List<String> allergens = new ArrayList<>();
    private final List<String> careInstructions = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPatient} with the given patient details.
     */
    @JsonCreator
    public JsonAdaptedPatient(@JsonProperty("name") String name, @JsonProperty("nric") String nric,
            @JsonProperty("dob") String dob, @JsonProperty("phone") String phone,
            @JsonProperty("address") String address, @JsonProperty("medications") List<String> medications,
            @JsonProperty("allergens") List<String> allergens,
            @JsonProperty("careInstructions") List<String> careInstructions) {
        this.name = name;
        this.nric = nric;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        if (medications != null) {
            this.medications.addAll(medications);
        }
        if (allergens != null) {
            this.allergens.addAll(allergens);
        }
        if (careInstructions != null) {
            this.careInstructions.addAll(careInstructions);
        }
    }

    /**
     * Converts a given {@code Patient} into this class for Jackson use.
     */
    public JsonAdaptedPatient(Patient source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        nric = source.getNric();
        dob = source.getDob();
        address = source.getAddress().value;
        medications.addAll(source.getMedications());
        allergens.addAll(source.getAllergens());
        careInstructions.addAll(source.getCareInstructions());
    }

    /**
     * Converts this Jackson-friendly adapted patient object into the model's {@code Patient} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted patient.
     */
    public Patient toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (nric == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "NRIC"));
        }
        if (dob == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "DOB"));
        }

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        try {
            return new Patient(modelName, nric, dob, modelPhone, modelAddress,
                    medications, allergens, careInstructions);
        } catch (NullPointerException e) {
            throw new IllegalValueException("Care entries must not be null.");
        }
    }

}
