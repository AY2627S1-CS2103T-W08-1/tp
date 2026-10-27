package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Phone;
import seedu.address.testutil.TypicalPatients;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PATIENTS_FILE = TEST_DATA_FOLDER.resolve("typicalPatientsAddressBook.json");
    private static final Path INVALID_PATIENT_FILE = TEST_DATA_FOLDER.resolve("invalidPatientAddressBook.json");
    private static final Path DUPLICATE_PATIENT_FILE = TEST_DATA_FOLDER.resolve("duplicatePatientAddressBook.json");
    private static final Path NULL_PATIENT_FILE = TEST_DATA_FOLDER.resolve("nullPatientAddressBook.json");

    @Test
    public void toModelType_typicalPatientsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PATIENTS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPatientsAddressBook = TypicalPatients.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPatientsAddressBook);
        assertTrue(addressBookFromFile.getPatientList().stream().noneMatch(patient -> patient.isArchived()));
    }

    @Test
    public void toModelType_invalidPatientFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PATIENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, "Patient entry 1: " + Name.MESSAGE_CONSTRAINTS,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePatients_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PATIENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, "Patient entry 2: "
                + JsonSerializableAddressBook.MESSAGE_DUPLICATE_PATIENT,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nullPatient_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NULL_PATIENT_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, "Patient entry 1: "
                + JsonSerializableAddressBook.MESSAGE_NULL_PATIENT,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_invalidSecondPatient_reportsFilePositionAndReason() throws Exception {
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(getTypicalAddressBook()))
                .replace("\"" + BENSON.getPhone().value + "\"", "\"123\"");
        JsonSerializableAddressBook dataFromFile = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertThrows(IllegalValueException.class, "Patient entry 2: " + Phone.MESSAGE_CONSTRAINTS,
                dataFromFile::toModelType);
    }

}
