package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Alert.AlertType;

public class UiManagerTest {

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
