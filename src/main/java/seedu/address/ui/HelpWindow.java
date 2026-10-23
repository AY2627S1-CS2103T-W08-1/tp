package seedu.address.ui;

import java.util.logging.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.HelpCommand;

/**
 * Controller for a help page that lists every command and links to the User Guide.
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-w08-1.github.io/tp/UserGuide.html";
    public static final String USERGUIDE_MESSAGE = "For examples and details, see the User Guide:\n" + USERGUIDE_URL;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Label userGuideMessage;

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    /**
     * Creates a new HelpWindow, which stays on top of other windows, so that commands can be typed in the main
     * window while it is open. It is hidden again when Esc is pressed in it, or in the window that owns it, if any.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        userGuideMessage.setText(USERGUIDE_MESSAGE);
        helpMessage.setText(HelpCommand.COMMAND_SUMMARY);
        root.setAlwaysOnTop(true);

        EventHandler<KeyEvent> hideOnEscape = event -> {
            if (event.getCode() == KeyCode.ESCAPE && isShowing()) {
                hide();
                event.consume();
            }
        };
        root.addEventHandler(KeyEvent.KEY_PRESSED, hideOnEscape);
        // while shown, Esc in the owner, e.g., the main window, also hides it; a filter sees Esc before the
        // focused text box does
        root.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            if (root.getOwner() != null) {
                root.getOwner().addEventFilter(KeyEvent.KEY_PRESSED, hideOnEscape);
            }
        });
        root.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            if (root.getOwner() != null) {
                root.getOwner().removeEventFilter(KeyEvent.KEY_PRESSED, hideOnEscape);
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Copies the URL of the User Guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}
