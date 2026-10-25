package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {

    private static final List<String> PLACEHOLDER_IDS = List.of("#commandBoxPlaceholder",
            "#resultDisplayPlaceholder", "#patientListPanelPlaceholder", "#patientDetailPanelPlaceholder",
            "#statusbarPlaceholder");

    private static boolean isToolkitAvailable;

    @TempDir
    private Path temporaryFolder;

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        JavaFxTestUtil.startToolkit();
        isToolkitAvailable = JavaFxTestUtil.isToolkitAvailable();
    }

    /**
     * Closes the windows that a test opened, so that they do not stay open for later tests.
     */
    @AfterEach
    public void closeWindows() throws Exception {
        if (isToolkitAvailable) {
            runOnFxThread(() -> {
                List.copyOf(Window.getWindows()).forEach(Window::hide);
                return null;
            });
        }
    }

    @Test
    public void fillInnerParts_newWindow_fillsEveryPlaceholder() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        Scene scene = runOnFxThread(() -> {
            MainWindow mainWindow = createMainWindow();
            mainWindow.fillInnerParts();
            // creates the skin of the split pane, which adds the panel placeholders to the scene
            mainWindow.getPrimaryStage().getScene().getRoot().applyCss();
            return mainWindow.getPrimaryStage().getScene();
        });

        for (String placeholderId : PLACEHOLDER_IDS) {
            assertEquals(1, ((StackPane) scene.lookup(placeholderId)).getChildren().size(), placeholderId);
        }
    }

    @Test
    public void handleHelp_helpWindowClosed_showsHelpWindow() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        boolean isHelpWindowShowing = runOnFxThread(() -> {
            createMainWindow().handleHelp();
            return Window.getWindows().stream()
                    .anyMatch(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()));
        });

        assertTrue(isHelpWindowShowing);
    }

    /**
     * Returns a main window for MediConnect with the typical patients, saving its data in the temporary folder.
     */
    private MainWindow createMainWindow() {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        LogicManager logic = new LogicManager(new ModelManager(getTypicalAddressBook(), new UserPrefs()), storage);
        return new MainWindow(new Stage(), logic, dataFile);
    }
}
