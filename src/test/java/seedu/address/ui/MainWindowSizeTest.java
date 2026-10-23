package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests the size and position that a new {@code MainWindow} takes from the saved GUI settings.
 */
public class MainWindowSizeTest {

    private static boolean isToolkitAvailable;

    @TempDir
    public Path temporaryFolder;

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        isToolkitAvailable = JavaFxTestUtil.startToolkit();
    }

    @Test
    public void constructor_savedSizeFitsScreen_keepsSavedSizeAndPosition() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Rectangle2D screen = getMainScreen();
        int x = (int) screen.getMinX() + 10;
        int y = (int) screen.getMinY() + 10;

        Stage stage = createMainWindowStage(new GuiSettings(300, 200, x, y));

        assertWindowBounds(new Rectangle2D(x, y, 300, 200), stage);
    }

    @Test
    public void constructor_savedSizeLargerThanScreen_shrunkToFitScreen() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Rectangle2D screen = getMainScreen();

        Stage stage = createMainWindowStage(new GuiSettings(screen.getWidth() + 1000, screen.getHeight() + 1000,
                (int) screen.getMinX(), (int) screen.getMinY()));

        assertWindowBounds(screen, stage);
    }

    @Test
    public void constructor_savedPositionPartlyOffScreen_movedOntoScreen() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Rectangle2D screen = getMainScreen();

        // the top-left corner is on the screen, but the rest of the window would be past its bottom-right corner
        Stage stage = createMainWindowStage(new GuiSettings(300, 200, (int) screen.getMaxX() - 1,
                (int) screen.getMaxY() - 1));

        assertWindowBounds(new Rectangle2D(screen.getMaxX() - 300, screen.getMaxY() - 200, 300, 200), stage);
    }

    @Test
    public void constructor_savedPositionOnNoScreen_movedOntoMainScreen() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Rectangle2D screen = getMainScreen();

        // e.g., the window was last on a second screen that is no longer connected
        Stage stage = createMainWindowStage(new GuiSettings(300, 200, -100_000, -100_000));

        assertWindowBounds(new Rectangle2D(screen.getMinX(), screen.getMinY(), 300, 200), stage);
    }

    @Test
    public void constructor_noSavedPosition_keepsSizeWithinScreenAndLeavesPositionUnset() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Rectangle2D screen = getMainScreen();
        GuiSettings defaultSettings = new GuiSettings();

        Stage stage = createMainWindowStage(defaultSettings);

        assertEquals(Math.min(defaultSettings.getWindowWidth(), screen.getWidth()), stage.getWidth());
        assertEquals(Math.min(defaultSettings.getWindowHeight(), screen.getHeight()), stage.getHeight());
        assertEquals(Double.NaN, stage.getX());
        assertEquals(Double.NaN, stage.getY());
    }

    private static Rectangle2D getMainScreen() throws Exception {
        return runOnFxThread(() -> Screen.getPrimary().getVisualBounds());
    }

    private static void assertWindowBounds(Rectangle2D expectedBounds, Stage stage) {
        assertEquals(expectedBounds.getMinX(), stage.getX());
        assertEquals(expectedBounds.getMinY(), stage.getY());
        assertEquals(expectedBounds.getWidth(), stage.getWidth());
        assertEquals(expectedBounds.getHeight(), stage.getHeight());
    }

    /**
     * Returns the stage of a new main window whose saved GUI settings are {@code guiSettings}.
     */
    private Stage createMainWindowStage(GuiSettings guiSettings) throws Exception {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(guiSettings);
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(new ModelManager(new AddressBook(), userPrefs), storage);
        return runOnFxThread(() -> new MainWindow(new Stage(), logic, dataFile).getPrimaryStage());
    }
}
