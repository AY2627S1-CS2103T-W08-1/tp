package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.patient.Patient;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PATIENT = "Patients list contains duplicate patient(s).";
    public static final String MESSAGE_NULL_PATIENT = "Patients list contains an empty patient entry.";
    private static final String MESSAGE_INVALID_PATIENT = "Patient entry %1$d: %2$s";

    @JsonProperty("persons")
    private final List<JsonAdaptedPatient> patients = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given patients.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPatient> patients) {
        this.patients.addAll(patients);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        patients.addAll(source.getPatientList().stream().map(JsonAdaptedPatient::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (int i = 0; i < patients.size(); i++) {
            try {
                JsonAdaptedPatient jsonAdaptedPatient = patients.get(i);
                if (jsonAdaptedPatient == null) {
                    throw new IllegalValueException(MESSAGE_NULL_PATIENT);
                }
                Patient patient = jsonAdaptedPatient.toModelType();
                if (addressBook.hasPatient(patient)) {
                    throw new IllegalValueException(MESSAGE_DUPLICATE_PATIENT);
                }
                addressBook.addPatient(patient);
            } catch (IllegalValueException e) {
                throw new IllegalValueException(
                        String.format(Locale.ROOT, MESSAGE_INVALID_PATIENT, i + 1, e.getMessage()), e);
            }
        }
        return addressBook;
    }

}
