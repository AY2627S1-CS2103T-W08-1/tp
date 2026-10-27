package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PatientUtil;
import seedu.address.ui.Ui;

public class MainAppTest {

    private static final String INVALID_DATA = "invalid data";

    @TempDir
    public Path temporaryFolder;

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
        assertEquals(String.format(MainApp.MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath()),
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
    public void start_failedStartup_presentsErrorBeforeExit() throws Exception {
        Path dataFile = Files.createDirectory(temporaryFolder.resolve("mediconnect.json"));
        List<String> events = new ArrayList<>();
        MainApp app = new MainApp(dataFile, temporaryFolder.resolve("preferences.json"),
                message -> events.add("error: " + message), () -> events.add("exit"));

        app.init();
        app.start(null);
        app.stop();

        String expectedMessage = String.format(MainApp.MESSAGE_STARTUP_FAILURE, dataFile.toAbsolutePath(),
                MainApp.MESSAGE_DATA_PATH_DIRECTORY);
        assertEquals(List.of("error: " + expectedMessage, "exit"), events);
        assertNull(app.model);
        assertNull(app.ui);
        assertTrue(Files.isDirectory(dataFile));
    }

    @Test
    public void init_dataPathIsDirectory_startShowsErrorAndStopIsSafe() throws Exception {
        Path dataFile = Files.createDirectory(temporaryFolder.resolve("mediconnect.json"));
        StartupAppStub app = new StartupAppStub(dataFile, temporaryFolder.resolve("preferences.json"), null);

        app.init();
        app.start(null);
        app.stop();

        assertEquals(String.format(MainApp.MESSAGE_STARTUP_FAILURE, dataFile.toAbsolutePath(),
                MainApp.MESSAGE_DATA_PATH_DIRECTORY), app.startupError);
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        assertTrue(Files.isDirectory(dataFile));
        assertTrue(getRecoveryFiles().isEmpty());
    }

    @Test
    public void init_dataAccessDenied_startExplainsPermissionsWithoutChangingData() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        Files.writeString(dataFile, INVALID_DATA);
        StartupAppStub app = new StartupAppStub(dataFile, temporaryFolder.resolve("preferences.json"),
                new AccessDeniedException(dataFile.toString()));

        app.init();
        app.start(null);
        app.stop();

        assertEquals(String.format(MainApp.MESSAGE_STARTUP_FAILURE, dataFile.toAbsolutePath(),
                MainApp.MESSAGE_DATA_ACCESS_DENIED), app.startupError);
        assertEquals(INVALID_DATA, Files.readString(dataFile));
        assertNull(app.ui);
    }

    @Test
    public void init_otherRecoveryFailure_startShowsSafeMessage() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        Files.writeString(dataFile, INVALID_DATA);
        StartupAppStub app = new StartupAppStub(dataFile, temporaryFolder.resolve("preferences.json"),
                new IOException("private exception details"));

        app.init();
        app.start(null);

        assertEquals(String.format(MainApp.MESSAGE_STARTUP_FAILURE, dataFile.toAbsolutePath(),
                MainApp.MESSAGE_DATA_PRESERVATION_FAILED), app.startupError);
        assertFalse(app.startupError.contains("private exception details"));
        assertEquals(INVALID_DATA, Files.readString(dataFile));
    }

    @Test
    public void init_validData_startOpensUiNormally() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        createStorage(dataFile).saveAddressBook(SampleDataUtil.getSampleAddressBook());
        StartupAppStub app = new StartupAppStub(dataFile, temporaryFolder.resolve("preferences.json"), null);

        app.init();
        UiStub ui = new UiStub();
        app.ui = ui;
        app.start(null);

        assertTrue(ui.isStarted);
        assertNull(app.startupError);
        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
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

    private StorageManager createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
    }

    private List<Path> getRecoveryFiles() throws IOException {
        try (var files = Files.list(temporaryFolder)) {
            return files.filter(path -> path.getFileName().toString().startsWith("mediconnect-unreadable-")).toList();
        }
    }

    /**
     * Captures startup errors without opening a dialog or exiting the test process.
     */
    private static class StartupAppStub extends MainApp {
        private final IOException failure;
        private String startupError;

        StartupAppStub(Path dataFile, Path prefsFile, IOException failure) {
            super(dataFile, prefsFile);
            this.failure = failure;
        }

        @Override
        protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
            if (failure != null) {
                throw failure;
            }
            return super.initModelManager(storage, userPrefs);
        }

        @Override
        protected void showStartupError(String message) {
            startupError = message;
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
