package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import seedu.address.testutil.PatientBuilder;

public class PatientCardTest {

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
    public void constructor_activePatient_showsNameWithoutArchivedMarker() {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        PatientCard card = new PatientCard(new PatientBuilder().build(), 1);

        assertEquals(PatientBuilder.DEFAULT_NAME, ((Label) card.getRoot().lookup("#name")).getText());
    }

    @Test
    public void constructor_archivedPatient_showsArchivedMarker() {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        PatientCard card = new PatientCard(new PatientBuilder().withArchived(true).build(), 1);
        Label name = (Label) card.getRoot().lookup("#name");

        assertEquals(PatientBuilder.DEFAULT_NAME + " (Archived)", name.getText());
    }
}
