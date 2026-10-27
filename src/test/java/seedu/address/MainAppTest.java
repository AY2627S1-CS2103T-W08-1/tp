package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.addUtf8ByteOrderMark;
import static seedu.address.testutil.TestUtil.replaceWithInvalidUtf8Byte;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.FileUtil;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.patient.Phone;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PatientUtil;
import seedu.address.ui.Ui;

public class MainAppTest {

    private static final String INVALID_DATA = "invalid data";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_nullPreferences_rejectedBeforeCreatingData() throws Exception {
        StorageManager storage = createStorage(temporaryFolder.resolve("mediconnect.json"));
        assertThrows(NullPointerException.class, () -> new MainApp().initModelManager(storage, null));
        assertTrue(storage.readAddressBook().isEmpty());
    }

    @Test
    public void initModelManager_storageReturnsNull_failsInternalAssertion() {
        JsonAddressBookStorage brokenStorage = new JsonAddressBookStorage(temporaryFolder.resolve("mediconnect.json")) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() {
                return null;
            }
        };
        StorageManager storage = new StorageManager(brokenStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        assertThrows(AssertionError.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));
    }

    @Test
    public void initModelManager_missingDataFile_createsFileWithSampleData() throws Exception {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("data").resolve("mediconnect.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), storage.readAddressBook().get());
    }

    @Test
    public void initModelManager_invalidDataFile_preservedAfterCommands() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        FileUtil.writeToFile(dataFile, INVALID_DATA);
        StorageManager storage = createStorage(dataFile);
        MainApp mainApp = new MainApp();
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        Path recoveryFile = getRecoveryFiles().get(0);

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(INVALID_DATA, FileUtil.readFromFile(dataFile));
        assertEquals(INVALID_DATA, FileUtil.readFromFile(recoveryFile));
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;
        mainApp.start(null);
        assertEquals(String.format(MainApp.MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath(),
                MainApp.MESSAGE_UNREADABLE_DATA),
                uiStub.warningMessage);

        LogicManager logic = new LogicManager(model, storage);
        // a command that changes no patient data leaves the unreadable file as it is
        logic.execute("list");
        assertEquals(INVALID_DATA, FileUtil.readFromFile(dataFile));
        logic.execute(PatientUtil.getAddCommand(ALICE));
        assertEquals(List.of(ALICE), storage.readAddressBook().get().getPatientList());
        assertEquals(INVALID_DATA, FileUtil.readFromFile(recoveryFile));
    }

    @Test
    public void initModelManager_invalidPatient_preservedAfterCommand() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        Path invalidPatientFile = Path.of("src", "test", "data", "JsonSerializableAddressBookTest",
                "invalidPatientAddressBook.json");
        Files.copy(invalidPatientFile, dataFile);
        StorageManager storage = createStorage(dataFile);
        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        new LogicManager(model, storage).execute("list");

        assertEquals(Files.readString(invalidPatientFile), Files.readString(getRecoveryFiles().get(0)));
    }

    @Test
    public void initModelManager_invalidSecondPatient_warnsWithPositionAndReason() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = createStorage(dataFile);
        storage.saveAddressBook(getTypicalAddressBook());
        String invalidData = Files.readString(dataFile)
                .replace("\"" + BENSON.getPhone().value + "\"", "\"123\"");
        Files.writeString(dataFile, invalidData);
        MainApp mainApp = new MainApp();

        Model model = mainApp.initModelManager(storage, new UserPrefs());
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;
        mainApp.start(null);

        assertEquals(new AddressBook(), model.getAddressBook());
        Path recoveryFile = getRecoveryFiles().get(0);
        assertEquals(invalidData, Files.readString(recoveryFile));
        assertEquals(String.format(MainApp.MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath(),
                "Patient entry 2: " + Phone.MESSAGE_CONSTRAINTS), uiStub.warningMessage);
    }

    @Test
    public void initModelManager_repeatedFailedLoads_keepsSeparateRecoveryCopies() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        FileUtil.writeToFile(dataFile, INVALID_DATA);
        StorageManager storage = createStorage(dataFile);

        new MainApp().initModelManager(storage, new UserPrefs());
        new MainApp().initModelManager(storage, new UserPrefs());

        List<Path> recoveryFiles = getRecoveryFiles();
        assertEquals(2, recoveryFiles.size());
        for (Path recoveryFile : recoveryFiles) {
            assertEquals(INVALID_DATA, FileUtil.readFromFile(recoveryFile));
        }
    }

    @Test
    public void initModelManager_validDataFile_noRecoveryCopy() throws Exception {
        StorageManager storage = createStorage(temporaryFolder.resolve("mediconnect.json"));
        storage.saveAddressBook(SampleDataUtil.getSampleAddressBook());

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
        assertTrue(getRecoveryFiles().isEmpty());
    }

    @Test
    public void initModelManager_unpairedSurrogateInDataFile_startsEmptyAndKeepsCopy() throws Exception {
        // Equivalence partitioning: an address edited by hand to hold half of a surrogate pair, which must
        // not be loaded and later saved as "?"
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = createStorageWithSampleData(dataFile);
        String editedData = Files.readString(dataFile).replace("Geylang", "\\ud800 Geylang");
        Files.writeString(dataFile, editedData);
        MainApp mainApp = new MainApp();
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;

        Model model = mainApp.initModelManager(storage, new UserPrefs());
        mainApp.start(null);

        assertEquals(new AddressBook(), model.getAddressBook());
        Path recoveryFile = getRecoveryFiles().get(0);
        assertEquals(editedData, Files.readString(recoveryFile));
        assertEquals(String.format(MainApp.MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath(),
                MainApp.MESSAGE_UNREADABLE_DATA), uiStub.warningMessage);
    }

    @Test
    public void initModelManager_dataFileWithByteOrderMark_loadsDataWithoutWarning() throws Exception {
        // Equivalence partitioning: a valid data file edited by hand in an editor that adds a UTF-8
        // byte-order mark
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = createStorageWithSampleData(dataFile);
        addUtf8ByteOrderMark(dataFile);
        MainApp mainApp = new MainApp();
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;

        Model model = mainApp.initModelManager(storage, new UserPrefs());
        mainApp.start(null);

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
        assertTrue(getRecoveryFiles().isEmpty());
        assertNull(uiStub.warningMessage);
    }

    @Test
    public void initModelManager_invalidUtf8DataFile_startsEmptyAndKeepsUnchangedCopy() throws Exception {
        // Equivalence partitioning: a data file with a byte in an address that is not valid UTF-8, which must
        // not be loaded, and later saved, with a replacement character in the address
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = createStorageWithInvalidUtf8Data(dataFile);
        byte[] invalidData = Files.readAllBytes(dataFile);
        MainApp mainApp = new MainApp();
        UiStub uiStub = new UiStub();
        mainApp.ui = uiStub;

        Model model = mainApp.initModelManager(storage, new UserPrefs());
        mainApp.start(null);

        assertEquals(new AddressBook(), model.getAddressBook());
        Path recoveryFile = getRecoveryFiles().get(0);
        assertArrayEquals(invalidData, Files.readAllBytes(dataFile));
        assertArrayEquals(invalidData, Files.readAllBytes(recoveryFile));
        assertEquals(String.format(MainApp.MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath(),
                MainApp.MESSAGE_UNREADABLE_DATA), uiStub.warningMessage);
    }

    @Test
    public void initModelManager_dataPathIsDirectory_stopsInitialization() throws Exception {
        Path dataFile = Files.createDirectory(temporaryFolder.resolve("mediconnect.json"));
        StorageManager storage = createStorage(dataFile);

        assertThrows(IOException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));

        assertTrue(Files.isDirectory(dataFile));
        assertTrue(getRecoveryFiles().isEmpty());
    }

    @Test
    public void initModelManager_saveFails_startsWithSampleData() throws Exception {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("mediconnect.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw new IOException("dummy IO exception");
                    }
                };
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
    }

    @Test
    public void stop_savesUserPrefs() throws Exception {
        Path prefsFile = temporaryFolder.resolve("userPrefs.json");
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("mediconnect.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(prefsFile);
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(800, 600, 10, 20));

        MainApp mainApp = new MainApp();
        mainApp.storage = new StorageManager(addressBookStorage, userPrefsStorage);
        mainApp.model = new ModelManager(new AddressBook(), userPrefs);

        mainApp.stop();

        assertTrue(prefsFile.toFile().exists());
        assertEquals(userPrefs, userPrefsStorage.readUserPrefs().get());
    }

    @Test
    public void start_callsUiStart() {
        UiStub uiStub = new UiStub();
        MainApp mainApp = new MainApp();
        mainApp.ui = uiStub;

        mainApp.start(null);

        assertTrue(uiStub.isStarted);
        assertNull(uiStub.startedStage);
        assertSame(uiStub, mainApp.ui);
        assertNull(uiStub.warningMessage);
    }

    /**
     * Returns storage for {@code dataFile}, after saving the sample data there.
     */
    private StorageManager createStorageWithSampleData(Path dataFile) throws IOException {
        StorageManager storage = createStorage(dataFile);
        storage.saveAddressBook(SampleDataUtil.getSampleAddressBook());
        return storage;
    }

    private StorageManager createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
    }

    /**
     * Returns storage for {@code dataFile}, after saving the sample data there with a byte in an address
     * that is not valid UTF-8.
     */
    private StorageManager createStorageWithInvalidUtf8Data(Path dataFile) throws IOException {
        StorageManager storage = createStorage(dataFile);
        storage.saveAddressBook(SampleDataUtil.getSampleAddressBook());
        replaceWithInvalidUtf8Byte(dataFile, "Geylang");
        return storage;
    }

    private List<Path> getRecoveryFiles() throws IOException {
        try (var files = Files.list(temporaryFolder)) {
            return files.filter(path -> path.getFileName().toString().startsWith("mediconnect-unreadable-")).toList();
        }
    }

    /**
     * A {@code Ui} stub that records the call to {@code start}.
     */
    private static class UiStub implements Ui {
        private boolean isStarted = false;
        private Stage startedStage = null;
        private String warningMessage;

        @Override
        public void start(Stage primaryStage) {
            isStarted = true;
            startedStage = primaryStage;
        }

        @Override
        public void showWarning(String message) {
            warningMessage = message;
        }
    }
}
