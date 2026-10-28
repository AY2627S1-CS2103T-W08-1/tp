package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
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
    /** Zero-based index of the clicked card: the second, since a list may treat the first specially. */
    private static final int CLICKED_CARD_INDEX = 1;

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
            return findShownHelpWindow().isPresent();
        });

        assertTrue(isHelpWindowShowing);
    }

    @Test
    public void handleHelp_helpWindowMinimized_showsHelpWindowNotMinimized() throws Exception {
        // Equivalence partitioning: a help window that is open but minimized, which still counts as shown
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        boolean isHelpWindowBack = runOnFxThread(() -> {
            MainWindow mainWindow = createMainWindow();
            mainWindow.handleHelp();
            findShownHelpWindow().orElseThrow().setIconified(true);
            mainWindow.handleHelp();
            return findShownHelpWindow().filter(helpWindow -> !helpWindow.isIconified()).isPresent();
        });

        assertTrue(isHelpWindowBack);
    }

    @Test
    public void clickPatientCard_commandBoxHasFocus_cardNotHighlightedAndFocusKept() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        CardClickResult result = runOnFxThread(this::clickPatientCard);

        assertEquals(result.lookBeforeClick(), result.lookAfterClick());
        assertTrue(result.isFocusKept());
    }

    /**
     * Shows a main window whose command box has the focus, and clicks a patient's card. Returns how the card
     * looks before and after the click, and whether the command box kept the focus.
     * Must be called on the JavaFX application thread.
     */
    private CardClickResult clickPatientCard() {
        MainWindow mainWindow = createMainWindow();
        mainWindow.show();
        mainWindow.fillInnerParts();
        Scene scene = mainWindow.getPrimaryStage().getScene();
        Node commandBox = scene.lookup("#commandTextField");
        commandBox.requestFocus();
        ListCell<?> card = findPatientCards(scene).get(CLICKED_CARD_INDEX);
        List<Object> lookBeforeClick = getLook(card);

        click(card);
        scene.getRoot().applyCss();
        return new CardClickResult(lookBeforeClick, getLook(card), scene.getFocusOwner() == commandBox);
    }

    /**
     * Returns the cells of the patient list in {@code scene} that show a patient, in list order.
     */
    private static List<ListCell<?>> findPatientCards(Scene scene) {
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        return scene.lookup("#patientListView").lookupAll(".list-cell").stream()
                .<ListCell<?>>map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty())
                .sorted(Comparator.comparingInt(ListCell::getIndex))
                .toList();
    }

    /**
     * Returns the background of {@code card} and the border of its card pane, which a highlight would change.
     */
    private static List<Object> getLook(ListCell<?> card) {
        Region cardPane = (Region) card.lookup("#cardPane");
        return Arrays.asList(card.getBackground(), cardPane.getBorder());
    }

    /**
     * Fires the events of a click with the primary mouse button at the center of {@code node}.
     */
    private static void click(Node node) {
        // the coordinates of a new event are in the scene, and a cell ignores a press outside its bounds
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        double x = bounds.getCenterX();
        double y = bounds.getCenterY();
        for (EventType<MouseEvent> type : List.of(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
                MouseEvent.MOUSE_CLICKED)) {
            Event.fireEvent(node, new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, true, null));
        }
    }

    /**
     * How a clicked patient card looks before and after the click, and whether the command box kept
     * the focus.
     */
    private record CardClickResult(List<Object> lookBeforeClick, List<Object> lookAfterClick,
            boolean isFocusKept) {}

    /**
     * Returns the help window if it is shown. Must be called on the JavaFX application thread.
     */
    private static Optional<Stage> findShownHelpWindow() {
        return Window.getWindows().stream()
                .filter(window -> window instanceof Stage stage && "Help".equals(stage.getTitle()))
                .map(window -> (Stage) window)
                .findFirst();
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
