package seedu.address;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
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
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5.0";
    public static final String MESSAGE_DATA_RECOVERY =
            "The data file could not be loaded. Starting with no patients.\nRecovery copy: %1$s\nDetails: %2$s";
    public static final String MESSAGE_UNREADABLE_DATA =
            "The file could not be read as MediConnect patient data. Check its JSON format and file permissions.";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "mediconnect.json");
    private static final String RECOVERY_FILE_PREFIX = "mediconnect-unreadable-";

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private String dataLoadWarning = "";

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing MediConnect ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead, and saved to a new data file,
     * if {@code storage}'s address book is not found,
     * or an empty address book will be used after preserving an unreadable data file.
     *
     * @throws IOException If an unreadable data file cannot be preserved.
     */
    protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
        requireAllNonNull(storage, userPrefs);
        Path dataFilePath = storage.getAddressBookFilePath();
        logger.info("Using data file : " + dataFilePath);

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            assert addressBookOptional != null : "Storage must return an Optional instead of null.";
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + dataFilePath
                        + " populated with sample data.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            if (!Files.isRegularFile(dataFilePath)) {
                throw new IOException("Cannot preserve data file: " + dataFilePath, e);
            }
            Path recoveryFile = dataFilePath.resolveSibling(RECOVERY_FILE_PREFIX + UUID.randomUUID() + ".json");
            Files.copy(dataFilePath, recoveryFile);
            String reason = e.getCause() instanceof IllegalValueException
                    ? e.getCause().getMessage() : MESSAGE_UNREADABLE_DATA;
            dataLoadWarning = String.format(MESSAGE_DATA_RECOVERY, recoveryFile.toAbsolutePath(), reason);
            logger.warning(dataLoadWarning);
            initialData = new AddressBook();
        }

        createDataFileIfMissing(storage, initialData);
        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Saves {@code data} to {@code storage}'s data file if the file does not exist yet.
     */
    private void createDataFileIfMissing(Storage storage, ReadOnlyAddressBook data) {
        if (Files.exists(storage.getAddressBookFilePath())) {
            return;
        }
        try {
            storage.saveAddressBook(data);
        } catch (IOException e) {
            logger.warning("Could not create the data file " + storage.getAddressBookFilePath()
                    + ": " + StringUtil.getDetails(e));
        }
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting MediConnect " + MainApp.VERSION);
        ui.start(primaryStage);
        if (!dataLoadWarning.isEmpty()) {
            ui.showWarning(dataLoadWarning);
        }
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping MediConnect ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
