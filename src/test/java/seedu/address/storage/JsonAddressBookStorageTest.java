package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.addUtf8ByteOrderMark;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.HOON;
import static seedu.address.testutil.TypicalPatients.IDA;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPatientAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPatientAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPatientAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPatientAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidMedicationsInFile_throwDataLoadingException() throws Exception {
        String[] invalidMedications = {
            "[ \"Metformin\" ]",
            "[ { \"times\" : [ \"08:00\" ] } ]",
            "[ { \"description\" : \" \" } ]",
            "[ { \"description\" : \"Metformin\", \"times\" : [ \"8:00\" ] } ]",
            "[ { \"description\" : \"Metformin\", \"times\" : [ \"24:00\" ] } ]",
            "[ { \"description\" : \"Metformin\", \"times\" : [ null ] } ]",
            "[ { \"description\" : \"Metformin\", \"times\" : \"08:00\" } ]",
            "[ null ]",
            "\"Metformin\"",
        };
        for (String medications : invalidMedications) {
            Path filePath = writePatientWithMedications(medications);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath)
                    .readAddressBook(filePath));
        }
    }

    @Test
    public void readAddressBook_medicationWithoutTimesInFile_loadsWithNoTimes() throws Exception {
        Path filePath = writePatientWithMedications("[ { \"description\" : \"Metformin\" } ]");
        ReadOnlyAddressBook readBack = new JsonAddressBookStorage(filePath).readAddressBook(filePath).get();
        assertEquals(List.of(new Medication("Metformin")), readBack.getPatientList().get(0).getMedications());
    }

    private Path writePatientWithMedications(String medicationsJson) throws IOException {
        Path filePath = Files.createTempFile(testFolder, "medications", ".json");
        Files.writeString(filePath, "{ \"persons\" : [ { \"name\" : \"Amy Bee\", \"phone\" : \"85355255\", "
                + "\"nric\" : \"S9999999A\", \"dob\" : \"1950-01-01\", \"address\" : \"123 Jurong West\", "
                + "\"medications\" : " + medicationsJson + " } ] }");
        return filePath;
    }

    @Test
    public void readAddressBook_contentAfterSavedData_throwsDataLoadingException() throws Exception {
        // Equivalence partitioning: a saved data file damaged later by text added after its data
        Path filePath = testFolder.resolve("ContentAfterData.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        Files.writeString(filePath, "\nTHIS IS NOT JSON", StandardOpenOption.APPEND);

        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
    }

    @Test
    public void readAddressBook_byteOrderMarkBeforeSavedData_readsData() throws Exception {
        // Equivalence partitioning: a saved data file edited by hand and saved again as "UTF-8 with BOM"
        Path filePath = testFolder.resolve("ByteOrderMark.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        addUtf8ByteOrderMark(filePath);

        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPatient(HOON);
        original.removePatient(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPatient(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_archivedPatient_preservesAllFields() throws Exception {
        Medication medication = new Medication("Metformin 500mg", List.of(LocalTime.of(8, 0)));
        Patient archivedPatient = new PatientBuilder(ALICE).withMedications(medication).withContacts(GP)
                .withArchived(true).build();
        AddressBook original = new AddressBook();
        original.addPatient(archivedPatient);
        Path filePath = testFolder.resolve("ArchivedPatient.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);

        assertEquals(original, new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
