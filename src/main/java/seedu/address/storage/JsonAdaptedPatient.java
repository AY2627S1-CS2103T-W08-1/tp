package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
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
 * Jackson-friendly version of {@link Patient}.
 */
class JsonAdaptedPatient {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Patient's %s field is missing!";
    public static final String MESSAGE_NULL_APPOINTMENT = "Appointments must not be null.";

    private final String name;
    private final String phone;
    private final String nric;
    private final String dob;
    private final String address;
    private final List<JsonAdaptedMedication> medications = new ArrayList<>();
    private final List<String> allergens = new ArrayList<>();
    private final List<String> careInstructions = new ArrayList<>();
    private final List<JsonAdaptedContact> contacts = new ArrayList<>();
    private final List<JsonAdaptedAppointment> appointments = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPatient} with the given patient details.
     */
    @JsonCreator
    public JsonAdaptedPatient(@JsonProperty("name") String name, @JsonProperty("nric") String nric,
            @JsonProperty("dob") String dob, @JsonProperty("phone") String phone,
            @JsonProperty("address") String address,
            @JsonProperty("medications") List<JsonAdaptedMedication> medications,
            @JsonProperty("allergens") List<String> allergens,
            @JsonProperty("careInstructions") List<String> careInstructions,
            @JsonProperty("contacts") List<JsonAdaptedContact> contacts,
            @JsonProperty("appointments") List<JsonAdaptedAppointment> appointments) {
        this.name = name;
        this.nric = nric;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        if (contacts != null) {
            this.contacts.addAll(contacts);
        }
        if (medications != null) {
            this.medications.addAll(medications);
        }
        if (allergens != null) {
            this.allergens.addAll(allergens);
        }
        if (careInstructions != null) {
            this.careInstructions.addAll(careInstructions);
        }
        if (appointments != null) {
            this.appointments.addAll(appointments);
        }
    }

    /**
     * Converts a given {@code Patient} into this class for Jackson use.
     */
    public JsonAdaptedPatient(Patient source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        nric = source.getNric().value;
        dob = source.getDob().toString();
        address = source.getAddress().value;
        source.getMedications().stream().map(JsonAdaptedMedication::new).forEach(medications::add);
        allergens.addAll(source.getAllergens());
        careInstructions.addAll(source.getCareInstructions());
        source.getContacts().stream().map(JsonAdaptedContact::new).forEach(contacts::add);
        source.getAppointments().stream().map(JsonAdaptedAppointment::new).forEach(appointments::add);
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
        if (!Nric.isValidNric(nric)) {
            throw new IllegalValueException(Nric.MESSAGE_CONSTRAINTS);
        }
        final Nric modelNric = new Nric(nric);

        if (dob == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "DOB"));
        }
        if (!Dob.isValidDob(dob)) {
            throw new IllegalValueException(Dob.MESSAGE_CONSTRAINTS);
        }
        final Dob modelDob = new Dob(dob);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final List<Medication> modelMedications = new ArrayList<>();
        for (JsonAdaptedMedication medication : medications) {
            if (medication == null) {
                throw new IllegalValueException("Medication entries must not be null.");
            }
            modelMedications.add(medication.toModelType());
        }

        List<Contact> modelContacts = new ArrayList<>();
        for (JsonAdaptedContact contact : contacts) {
            if (contact == null) {
                throw new IllegalValueException("Contact entry must not be null.");
            }
            modelContacts.add(contact.toModelType());
        }

        List<Appointment> modelAppointments = new ArrayList<>();
        for (JsonAdaptedAppointment appointment : appointments) {
            if (appointment == null) {
                throw new IllegalValueException(MESSAGE_NULL_APPOINTMENT);
            }
            modelAppointments.add(appointment.toModelType());
        }

        try {
            return new Patient(modelName, modelNric, modelDob, modelPhone, modelAddress,
                    modelMedications, allergens, careInstructions, modelContacts, modelAppointments);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        } catch (NullPointerException e) {
            throw new IllegalValueException("Care entries must not be null.");
        }
    }

}
