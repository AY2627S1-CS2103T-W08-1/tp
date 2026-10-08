package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPatient.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Phone;
import seedu.address.testutil.PatientBuilder;

public class JsonAdaptedPatientTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_NRIC = "S123A";
    private static final String INVALID_DOB = "2021-02-30";
    private static final String INVALID_ADDRESS = " ";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_NRIC = BENSON.getNric().toString();
    private static final String VALID_DOB = BENSON.getDob().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();

    @Test
    public void toModelType_validPatientDetails_returnsPatient() throws Exception {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(BENSON);
        assertEquals(BENSON, patient.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(INVALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of());
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(null, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, INVALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of());
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, null,
                VALID_ADDRESS, List.of(), List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidNric_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, INVALID_NRIC, VALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of());
        String expectedMessage = Nric.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullNric_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, null, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "NRIC");
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE, INVALID_ADDRESS,
                List.of(), List.of(), List.of());
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB,
                VALID_PHONE, null, List.of(), List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }


    @Test
    public void toModelType_invalidDob_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, INVALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of());
        String expectedMessage = Dob.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullDob_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, null, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of());
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "DOB"),
                patient::toModelType);
    }

    @Test
    public void toModelType_careLists_preserved() throws Exception {
        var patient = new PatientBuilder(BENSON).withMedications("Metformin")
                .withAllergens("Penicillin").withCareInstructions("Check blood sugar").build();
        assertEquals(patient, new JsonAdaptedPatient(patient).toModelType());
        JsonAdaptedPatient missingLists = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, null, null, null);
        assertEquals(BENSON, missingLists.toModelType());
    }
}
