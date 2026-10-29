package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import seedu.address.testutil.PatientBuilder;

public class PatientListPanelTest {

    private static final String LONG_NAME =
            "Muhammad Abdullah bin Rahman Iskandar Shah Wijaya Kusuma Tan Ah Kow s/o Lim Boon Keng";
    private static final double NARROW_LIST_WIDTH = 260;
    private static final double LIST_HEIGHT = 600;
    private static final double CARD_BOTTOM_PADDING = 5;
    private static final long TIMEOUT_SECONDS = 10;

    private static boolean isToolkitAvailable;

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        JavaFxTestUtil.startToolkit();
        isToolkitAvailable = JavaFxTestUtil.isToolkitAvailable();
    }

    @Test
    public void card_longWrappedName_isAsTallAsItsContent() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        double[] gapUnderNric = new double[1];
        runAndWaitOnFxThread(() -> {
            ListCell<?> cell = showPanelAndGetFirstCell(LONG_NAME);
            Node nric = cell.lookup("#nric");
            double nricBottom = nric.localToScene(nric.getLayoutBounds()).getMaxY() - cell.localToScene(0, 0).getY();
            gapUnderNric[0] = cell.getHeight() - nricBottom;
        });

        assertTrue(gapUnderNric[0] <= CARD_BOTTOM_PADDING + 1,
                "the card has an empty gap of " + gapUnderNric[0] + " under the NRIC");
    }

    @Test
    public void card_longWrappedName_indexIsOnFirstLineOfName() throws Exception {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");

        double[] tops = new double[2];
        runAndWaitOnFxThread(() -> {
            ListCell<?> cell = showPanelAndGetFirstCell(LONG_NAME);
            tops[0] = cell.lookup("#id").localToScene(0, 0).getY();
            tops[1] = cell.lookup("#name").localToScene(0, 0).getY();
        });

        assertEquals(tops[1], tops[0]);
    }

    /**
     * Shows a patient list with one patient named {@code name}, laid out at the width of a narrow window, and
     * returns the cell of the patient. Must be called on the JavaFX application thread.
     */
    private static ListCell<?> showPanelAndGetFirstCell(String name) {
        PatientListPanel panel = new PatientListPanel(FXCollections.observableArrayList(
                new PatientBuilder().withName(name).build()));
        Scene scene = new Scene(panel.getRoot(), NARROW_LIST_WIDTH, LIST_HEIGHT);
        scene.getStylesheets().add(PatientListPanelTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        scene.getStylesheets().add(PatientListPanelTest.class.getResource("/view/Extensions.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        try {
            // a few passes, as the height of a cell is known only after the list has given it its width
            for (int i = 0; i < 3; i++) {
                panel.getRoot().applyCss();
                panel.getRoot().layout();
            }
            return (ListCell<?>) panel.getRoot().lookupAll(".list-cell").stream()
                    .filter(cell -> cell.lookup("#name") != null).findFirst().get();
        } finally {
            stage.hide();
        }
    }

    private static void runAndWaitOnFxThread(Runnable task) throws Exception {
        CompletableFuture<Void> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                task.run();
                done.complete(null);
            } catch (RuntimeException | Error e) {
                done.completeExceptionally(e);
            }
        });
        done.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
