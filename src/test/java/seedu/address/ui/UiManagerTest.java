package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public class UiManagerTest {

    @Test
    public void showStartupError_beforeMainWindow_displaysErrorDialog() throws Exception {
        JavaFxTestUtil.startToolkit();
        assumeTrue(JavaFxTestUtil.isToolkitAvailable(), "JavaFX toolkit is not available");
        String message = "Data file: data/mediconnect.json\nReason: The data path is a folder.";
        List<String> messages = new ArrayList<>();

        runOnFxThread(() -> {
            // Close the modal dialog from its nested event loop after recording the rendered message.
            Platform.runLater(() -> {
                for (Window window : new ArrayList<>(Window.getWindows())) {
                    if (window.getScene().getRoot() instanceof DialogPane pane
                            && message.equals(pane.getContentText())) {
                        messages.add(pane.getContentText());
                        window.hide();
                    }
                }
            });
            UiManager.showStartupError(message);
            return null;
        });

        assertEquals(List.of(message), messages);
        assertThrows(NullPointerException.class, () -> UiManager.showStartupError(null));
    }

    @Test
    public void showWarning_displaysWarningMessage() {
        List<String> warnings = new ArrayList<>();
        // Intercept the dialog call so no window or logic is needed.
        UiManager ui = new UiManager(null, null) {
            @Override
            void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
                assertEquals(AlertType.WARNING, type);
                warnings.add(contentText);
            }
        };
        String message = "Recovery copy: data/recovery.json";

        ui.showWarning(message);

        assertEquals(List.of(message), warnings);
        assertThrows(NullPointerException.class, () -> ui.showWarning(null));
    }
}
