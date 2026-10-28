package seedu.address;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
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
    public static final String MESSAGE_DATA_CREATION_FAILED =
            "The data file could not be created. The sample patients are not saved.\nData file: %1$s\nReason: %2$s.\n"
            + "Close MediConnect, check that the parent path is a writable folder with free disk space, then restart.";

    public static final String MESSAGE_STARTUP_FAILURE =
            "MediConnect could not start safely.\nData file: %1$s\nReason: %2$s\n"
            + "Check that this path is a file and that you can read it and write to its folder, then try again. "
            + "Keep your original data; no patient data was changed.";
    public static final String MESSAGE_DATA_PATH_DIRECTORY = "The data path is a folder instead of a file.";
    public static final String MESSAGE_DATA_ACCESS_DENIED =
            "MediConnect does not have permission to read the data file or create its recovery copy.";
    public static final String MESSAGE_DATA_PRESERVATION_FAILED =
            "The data file could not be read or copied for recovery. Check permissions and available disk space.";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "mediconnect.json");
    private static final String RECOVERY_FILE_PREFIX = "mediconnect-unreadable-";

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private final Path dataFilePath;
    private final Path prefsFilePath;
    private final Consumer<String> startupErrorPresenter;
    private final Runnable exitAction;
    private String dataLoadWarning = "";
    private String startupFailure = "";

    /**
     * Creates the application with its standard data and preferences paths.
     */
    public MainApp() {
        this(ADDRESS_BOOK_FILE_PATH, USER_PREFS_FILE_PATH);
    }

    /**
     * Creates the application with the given paths so startup can be tested in a temporary folder.
     */
    MainApp(Path dataFilePath, Path prefsFilePath) {
        this(dataFilePath, prefsFilePath, UiManager::showStartupError, Platform::exit);
    }

    /**
     * Creates the application with startup actions that tests can observe without exiting JavaFX.
     */
    MainApp(Path dataFilePath, Path prefsFilePath, Consumer<String> startupErrorPresenter, Runnable exitAction) {
        requireAllNonNull(dataFilePath, prefsFilePath, startupErrorPresenter, exitAction);
        this.dataFilePath = dataFilePath;
        this.prefsFilePath = prefsFilePath;
        this.startupErrorPresenter = startupErrorPresenter;
        this.exitAction = exitAction;
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing MediConnect ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(prefsFilePath);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFilePath);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        try {
            model = initModelManager(storage, userPrefs);
        } catch (IOException e) {
            startupFailure = String.format(MESSAGE_STARTUP_FAILURE, dataFilePath.toAbsolutePath(),
                    describeDataStartupFailure(dataFilePath, e));
            logger.warning(startupFailure);
            return;
        }

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    private static String describeDataStartupFailure(Path path, IOException cause) {
        if (Files.isDirectory(path)) {
            return MESSAGE_DATA_PATH_DIRECTORY;
        }
        if (cause instanceof AccessDeniedException) {
            return MESSAGE_DATA_ACCESS_DENIED;
        }
        return MESSAGE_DATA_PRESERVATION_FAILED;
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * Sample data is used if {@code storage}'s address book is not found, with an initial save attempt.
     * A failed initial save produces a startup warning. An empty address book is used after preserving
     * an unreadable data file.
     *
     * @throws IOException If an unreadable data file cannot be preserved.
     */
    protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) throws IOException {
        Path dataFilePath = storage.getAddressBookFilePath();
        logger.info("Using data file : " + dataFilePath);

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
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
     * Records a startup warning if the initial data cannot be saved.
     */
    private void createDataFileIfMissing(Storage storage, ReadOnlyAddressBook data) {
        if (Files.exists(storage.getAddressBookFilePath())) {
            return;
        }
        try {
            storage.saveAddressBook(data);
        } catch (IOException e) {
            dataLoadWarning = String.format(MESSAGE_DATA_CREATION_FAILED,
                    storage.getAddressBookFilePath().toAbsolutePath(), Messages.describeWriteFailure(e));
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
        if (!startupFailure.isEmpty()) {
            showStartupError(startupFailure);
            return;
        }
        ui.start(primaryStage);
        if (!dataLoadWarning.isEmpty()) {
            ui.showWarning(dataLoadWarning);
        }
    }

    /**
     * Shows a startup failure on the JavaFX application thread, then exits without opening the main window.
     */
    protected void showStartupError(String message) {
        startupErrorPresenter.accept(message);
        exitAction.run();
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping MediConnect ] =============================");
        if (model == null) {
            return;
        }
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
