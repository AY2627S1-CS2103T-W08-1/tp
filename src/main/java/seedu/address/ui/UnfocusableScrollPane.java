package seedu.address.ui;

import javafx.scene.control.ScrollPane;

/**
 * Scroll pane that never takes the keyboard focus, so that clicking it, e.g., to copy a line of the detail
 * panel, leaves the focus in the command box. A plain scroll pane takes the focus on every mouse press, even
 * when {@code Tab} cannot reach it.
 */
public class UnfocusableScrollPane extends ScrollPane {

    @Override
    public void requestFocus() {
        // the command box keeps the focus, so that the user can go on typing after a click
    }
}
