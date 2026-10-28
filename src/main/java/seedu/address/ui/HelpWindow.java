package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
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
    private Label helpMessage;

    /** True while the window that owns the help window, if any, has the focus. */
    private final BooleanProperty isOwnerFocused = new SimpleBooleanProperty();

    /**
     * True while the help window or its owner has the focus. It is kept in a field, as a binding is only weakly
     * referenced by the properties it depends on, and would otherwise stop updating once garbage collected.
     */
    private final BooleanBinding isAppFocused;

    /** Hides the help window on Esc. The same handler is added to and removed from the owner, so it is kept. */
    private final EventHandler<KeyEvent> escapeHandler = this::hideOnEscape;

    /**
     * Creates a new HelpWindow. While it or the window that owns it, e.g., the main window, has the focus, it stays
     * in front of other windows, so that commands can be typed in the main window while it is open. It is hidden
     * again when Esc is pressed in it, or in its owner, if any.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, requireNonNull(root));
        userGuideMessage.setText(USERGUIDE_MESSAGE);
        helpMessage.setText(HelpCommand.COMMAND_SUMMARY);
        root.addEventHandler(KeyEvent.KEY_PRESSED, escapeHandler);
        isAppFocused = root.focusedProperty().or(isOwnerFocused);
        keepInFrontWhileAppFocused();
        followOwnerWhileShown();
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Hides the help window if {@code event} is a press of Esc, and stops the event from going further.
     */
    private void hideOnEscape(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            hide();
            event.consume();
        }
    }

    /**
     * Keeps the help window in front of other windows only while MediConnect has the focus, so that it stays above
     * the main window, but goes behind the windows of other apps, e.g., a browser, when the user switches to them.
     */
    private void keepInFrontWhileAppFocused() {
        isAppFocused.addListener((observable, wasAppFocused, isNowAppFocused) ->
                getRoot().setAlwaysOnTop(isNowAppFocused));
    }

    /**
     * Attaches the help window to its owner, if any, each time it is shown, and detaches it each time it is hidden.
     */
    private void followOwnerWhileShown() {
        getRoot().addEventHandler(WindowEvent.WINDOW_SHOWN, event -> attachToOwner());
        getRoot().addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> detachFromOwner());
    }

    /**
     * Lets Esc in the owner, e.g., the main window, also hide the help window, and lets the owner's focus keep the
     * help window in front. A filter is used, as it sees Esc before the focused text box of the owner does.
     */
    private void attachToOwner() {
        Window owner = getRoot().getOwner();
        if (owner != null) {
            owner.addEventFilter(KeyEvent.KEY_PRESSED, escapeHandler);
            isOwnerFocused.bind(owner.focusedProperty());
        }
        getRoot().setAlwaysOnTop(isAppFocused.get());
    }

    /**
     * Stops the owner, if any, from hiding the help window on Esc or keeping it in front while it is hidden.
     */
    private void detachFromOwner() {
        Window owner = getRoot().getOwner();
        if (owner != null) {
            owner.removeEventFilter(KeyEvent.KEY_PRESSED, escapeHandler);
        }
        isOwnerFocused.unbind();
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
        // a window hidden while minimized, e.g., by Esc in the main window, would be shown minimized again
        getRoot().setIconified(false);
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
     * Returns true if the help window is shown, but minimized, e.g., to the Dock or the taskbar.
     */
    public boolean isMinimized() {
        return getRoot().isShowing() && getRoot().isIconified();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
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
