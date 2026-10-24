package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import seedu.address.logic.commands.HelpCommand;

public class HelpWindowTest {

    private static final double STAGE_SIZE = 200;
    private static final long FOCUS_TIMEOUT_MILLIS = 3000;
    private static final long FOCUS_POLL_MILLIS = 50;

    private static boolean isToolkitAvailable;

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        isToolkitAvailable = JavaFxTestUtil.startToolkit();
    }

    @Test
    public void constructor_nullStage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HelpWindow(null));
    }

    @Test
    public void constructor_newWindow_showsUserGuideLinkAndCommandSummary() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        HelpWindow helpWindow = runOnFxThread(() -> {
            HelpWindow newHelpWindow = new HelpWindow();
            // creates the skin of the scroll pane, which adds the command summary to the scene
            newHelpWindow.getRoot().getScene().getRoot().applyCss();
            return newHelpWindow;
        });

        assertEquals(HelpWindow.USERGUIDE_MESSAGE, getLabelText(helpWindow, "#userGuideMessage"));
        assertEquals(HelpCommand.COMMAND_SUMMARY, getLabelText(helpWindow, "#helpMessage"));
    }

    @Test
    public void escapePressed_inHelpWindow_hidesHelpWindow() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        boolean isShowing = runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            helpWindow.show();
            pressKey(helpWindow.getRoot(), KeyCode.ESCAPE);
            return helpWindow.isShowing();
        });

        assertFalse(isShowing);
    }

    @Test
    public void otherKeyPressed_inHelpWindow_keepsHelpWindowShowing() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        boolean isShowing = runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            helpWindow.show();
            pressKey(helpWindow.getRoot(), KeyCode.A);
            boolean isStillShowing = helpWindow.isShowing();
            helpWindow.hide();
            return isStillShowing;
        });

        assertTrue(isShowing);
    }

    @Test
    public void escapePressed_inOwnerWindow_hidesHelpWindow() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        boolean isShowing = runOnFxThread(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new Pane()));
            HelpWindow helpWindow = new HelpWindow();
            helpWindow.getRoot().initOwner(owner);
            helpWindow.show();
            pressKey(owner, KeyCode.ESCAPE);
            return helpWindow.isShowing();
        });

        assertFalse(isShowing);
    }

    @Test
    public void focusChanged_shownWithOwner_inFrontOnlyWhileHelpWindowOrOwnerFocused() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        Stage owner = runOnFxThread(HelpWindowTest::createStage);
        // stands for a window of another app, e.g., a browser
        Stage otherWindow = runOnFxThread(HelpWindowTest::createStage);
        Stage root = runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            helpWindow.getRoot().initOwner(owner);
            return helpWindow.getRoot();
        });

        try {
            // e.g., opening the help window
            showAndWaitForFocus(root);
            assertTrue(runOnFxThread(root::isAlwaysOnTop));

            // e.g., typing in the main window
            showAndWaitForFocus(owner);
            assertTrue(runOnFxThread(root::isAlwaysOnTop));

            // e.g., switching to a browser
            showAndWaitForFocus(otherWindow);
            assertFalse(runOnFxThread(root::isAlwaysOnTop));
        } finally {
            runOnFxThread(() -> {
                root.hide();
                owner.hide();
                otherWindow.hide();
                return null;
            });
        }
    }

    @Test
    public void copyButton_clicked_copiesUserGuideUrl() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        String copiedText = runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow();
            Button copyButton = (Button) helpWindow.getRoot().getScene().lookup("#copyButton");
            copyButton.fire();
            return Clipboard.getSystemClipboard().getString();
        });

        assertEquals(HelpWindow.USERGUIDE_URL, copiedText);
    }

    private static Stage createStage() {
        Stage stage = new Stage();
        stage.setScene(new Scene(new Pane(), STAGE_SIZE, STAGE_SIZE));
        return stage;
    }

    /**
     * Shows {@code stage}, and waits until the operating system gives it the focus, which happens after it is shown.
     * A window is given the focus by showing it, since some displays, e.g., a virtual display with no window manager,
     * ignore later requests for the focus. Skips the test if the focus is not given within
     * {@code FOCUS_TIMEOUT_MILLIS}.
     */
    private static void showAndWaitForFocus(Stage stage) throws Exception {
        runOnFxThread(() -> {
            stage.show();
            return null;
        });
        long deadline = System.currentTimeMillis() + FOCUS_TIMEOUT_MILLIS;
        while (!runOnFxThread(stage::isFocused)) {
            assumeTrue(System.currentTimeMillis() < deadline, "The display did not give the window the focus");
            Thread.sleep(FOCUS_POLL_MILLIS);
        }
    }

    private static String getLabelText(HelpWindow helpWindow, String selector) {
        return ((Label) helpWindow.getRoot().getScene().lookup(selector)).getText();
    }

    /**
     * Sends a press of {@code keyCode} to {@code target}, as if the key were pressed while it has the focus.
     */
    private static void pressKey(EventTarget target, KeyCode keyCode) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false));
    }
}
