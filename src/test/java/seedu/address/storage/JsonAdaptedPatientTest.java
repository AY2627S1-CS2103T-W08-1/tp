package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.patient.Contact.MESSAGE_DUPLICATE;
import static seedu.address.storage.JsonAdaptedPatient.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalPatients.BENSON;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Medication;
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
    public void toModelType_missingArchived_defaultsToFalse() throws Exception {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(), List.of(), null);

        assertFalse(patient.toModelType().isArchived());
    }

    @Test
    public void toModelType_archivedPatient_roundTrips() throws Exception {
        Medication medication = new Medication("Metformin 500mg", List.of(LocalTime.of(8, 0)));
        var patient = new PatientBuilder(BENSON).withMedications(medication).withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar").withContacts(GP).withArchived(true).build();

        var readBack = new JsonAdaptedPatient(patient).toModelType();

        assertEquals(patient, readBack);
        assertTrue(readBack.isArchived());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(INVALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of(), null, null);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(null, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(), null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, INVALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of(), null, null);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, null,
                VALID_ADDRESS, List.of(), List.of(), List.of(), null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidNric_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, INVALID_NRIC, VALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of(), null, null);
        String expectedMessage = Nric.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullNric_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, null, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(), null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "NRIC");
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE, INVALID_ADDRESS,
                List.of(), List.of(), List.of(), null, null);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB,
                VALID_PHONE, null, List.of(), List.of(), List.of(), null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }


    @Test
    public void toModelType_invalidDob_throwsIllegalValueException() {
        JsonAdaptedPatient patient =
                new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, INVALID_DOB, VALID_PHONE, VALID_ADDRESS,
                List.of(), List.of(), List.of(), null, null);
        String expectedMessage = Dob.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, patient::toModelType);
    }

    @Test
    public void toModelType_nullDob_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, null, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(), null, null);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "DOB"),
                patient::toModelType);
    }

    @Test
    public void toModelType_careLists_preserved() throws Exception {
        var patient = new PatientBuilder(BENSON).withMedications("Metformin")
                .withAllergens("Penicillin").withCareInstructions("Check blood sugar").build();
        assertEquals(patient, new JsonAdaptedPatient(patient).toModelType());
        JsonAdaptedPatient missingLists = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, null, null, null, null, null);
        assertEquals(BENSON, missingLists.toModelType());
    }

    @Test
    public void toModelType_medicationWithTimes_roundTrips() throws Exception {
        Medication medication = new Medication("Metformin 500mg", List.of(LocalTime.of(20, 0), LocalTime.of(8, 0)));
        var patient = new PatientBuilder(BENSON).withMedications(medication).build();
        assertEquals(patient, new JsonAdaptedPatient(patient).toModelType());
    }

    @Test
    public void toModelType_medicationWithMissingTimes_loadsWithNoTimes() throws Exception {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(new JsonAdaptedMedication("Metformin", null)), null, null, null, null);
        assertEquals(new PatientBuilder(BENSON).withMedications("Metformin").build(), patient.toModelType());
    }

    @Test
    public void toModelType_nullMedicationEntry_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, Arrays.asList((JsonAdaptedMedication) null), List.of(), List.of(), null, null);
        assertThrows(IllegalValueException.class, "Medication entries must not be null.", patient::toModelType);
    }

    @Test
    public void toModelType_invalidMedication_throwsIllegalValueException() {
        JsonAdaptedPatient patient = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(new JsonAdaptedMedication(" ", List.of())), List.of(), List.of(), null, null);
        assertThrows(IllegalValueException.class, Medication.MESSAGE_DESCRIPTION_CONSTRAINTS,
                patient::toModelType);
    }

    @Test
    public void toModelType_contacts_preservedOrRejected() throws Exception {
        var contact = GP;
        var patient = new PatientBuilder(BENSON).withContacts(contact)
                .withMedications(new Medication("Metformin", List.of(LocalTime.of(8, 0)))).build();
        assertEquals(patient, new JsonAdaptedPatient(patient).toModelType());
        JsonAdaptedPatient nullEntry = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(), Arrays.asList((JsonAdaptedContact) null), null);
        assertThrows(IllegalValueException.class, "Contact entry must not be null.", nullEntry::toModelType);
        JsonAdaptedPatient duplicates = new JsonAdaptedPatient(VALID_NAME, VALID_NRIC, VALID_DOB, VALID_PHONE,
                VALID_ADDRESS, List.of(), List.of(), List.of(),
                List.of(new JsonAdaptedContact(contact), new JsonAdaptedContact(contact)), null);
        assertThrows(IllegalValueException.class, MESSAGE_DUPLICATE,
                duplicates::toModelType);
    }
}
