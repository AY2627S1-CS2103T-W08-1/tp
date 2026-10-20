package seedu.address.ui;

import javafx.stage.Stage;

/**
 * API of the UI component
 */
public interface Ui {

    /** Starts the UI (and the App).  */
    void start(Stage primaryStage);

    /**
     * Shows a warning after the UI has started.
     */
    void showWarning(String message);

}
