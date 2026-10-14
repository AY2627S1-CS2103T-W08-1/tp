package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;

public class MainAppTest {

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
    public void initModelManager_invalidDataFile_fileNotOverwritten() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        FileUtil.writeToFile(dataFile, "invalid data");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFile);
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals("invalid data", FileUtil.readFromFile(dataFile));
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
    }

    /**
     * A {@code Ui} stub that records the call to {@code start}.
     */
    private static class UiStub implements Ui {
        private boolean isStarted = false;
        private Stage startedStage = null;

        @Override
        public void start(Stage primaryStage) {
            isStarted = true;
            startedStage = primaryStage;
        }
    }
}
