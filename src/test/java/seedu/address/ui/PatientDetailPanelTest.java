package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalContacts.GUARDIAN;
import static seedu.address.ui.PatientDetailPanel.MESSAGE_NONE_RECORDED;
import static seedu.address.ui.PatientDetailPanel.MESSAGE_NO_PATIENT;

import java.awt.GraphicsEnvironment;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class PatientDetailPanelTest {

    private static final long TOOLKIT_TIMEOUT_SECONDS = 5;

    private static boolean isToolkitAvailable;

    private final SimpleObjectProperty<Patient> viewedPatient = new SimpleObjectProperty<>();

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        // A failed startup makes later JavaFX calls in the same JVM wait forever,
        // so do not attempt one when there is no display, e.g. on a headless CI runner.
        if (GraphicsEnvironment.isHeadless()) {
            isToolkitAvailable = false;
            return;
        }

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit was already started earlier in this JVM; checked below
        } catch (RuntimeException | LinkageError e) {
            isToolkitAvailable = false;
            return;
        }

        isToolkitAvailable = isToolkitRunning();
    }

    /**
     * Returns true if the JavaFX application thread runs a task within {@code TOOLKIT_TIMEOUT_SECONDS}.
     */
    private static boolean isToolkitRunning() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.runLater(latch::countDown);
            return latch.await(TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Test
    public void constructor_noPatientViewed_showsPlaceholder() {
        VBox content = createPanelContent();

        assertTrue(lookup(content, "#placeholder").isVisible());
        assertEquals(MESSAGE_NO_PATIENT, ((Label) lookup(content, "#placeholder")).getText());
        assertFalse(lookup(content, "#details").isVisible());
    }

    @Test
    public void viewedPatientSet_showsPatientFields() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());

        assertFalse(lookup(content, "#placeholder").isVisible());
        assertTrue(lookup(content, "#details").isVisible());
        assertEquals(PatientBuilder.DEFAULT_NAME, ((Label) lookup(content, "#name")).getText());
        assertEquals("NRIC: " + PatientBuilder.DEFAULT_NRIC, ((Label) lookup(content, "#nric")).getText());
        assertEquals("DOB: " + PatientBuilder.DEFAULT_DOB, ((Label) lookup(content, "#dob")).getText());
        assertEquals("Phone: " + PatientBuilder.DEFAULT_PHONE, ((Label) lookup(content, "#phone")).getText());
        Label address = (Label) lookup(content, "#address");
        assertEquals("Address: " + PatientBuilder.DEFAULT_ADDRESS, address.getText());
    }

    @Test
    public void viewedPatientSet_emptyCareLists_showsNoneRecorded() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());

        for (String id : List.of("#medications", "#allergens", "#careInstructions")) {
            VBox entries = (VBox) lookup(content, id);
            assertEquals(1, entries.getChildren().size());
            assertEquals(MESSAGE_NONE_RECORDED, ((Label) entries.getChildren().get(0)).getText());
        }
    }

    @Test
    public void viewedPatientSet_careEntries_showsOneNumberedWrappingRowPerEntryInOrder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder()
                .withMedications("Metformin 500mg", "Amlodipine 5mg")
                .withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar before breakfast")
                .build());

        assertEquals(List.of("Metformin 500mg", "Amlodipine 5mg"), getEntryTexts(content, "#medications"));
        assertEquals(List.of("Penicillin"), getEntryTexts(content, "#allergens"));
        assertEquals(List.of("Check blood sugar before breakfast"), getEntryTexts(content, "#careInstructions"));

        assertEquals(List.of("1.", "2."), getNumberTexts(content, "#medications"));
        assertEquals(List.of("1."), getNumberTexts(content, "#allergens"));
        assertEquals(List.of("1."), getNumberTexts(content, "#careInstructions"));

        HBox row = (HBox) ((VBox) lookup(content, "#medications")).getChildren().get(0);
        assertTrue(((Label) row.getChildren().get(1)).isWrapText());
    }

    @Test
    public void viewedPatientSet_medicationsWithTimes_showsTimesBesideDescription() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder()
                .withMedications(new Medication("Metformin 500mg", List.of(LocalTime.of(20, 0), LocalTime.of(8, 0))),
                        new Medication("Aspirin"))
                .build());

        assertEquals(List.of("Metformin 500mg (08:00, 20:00)", "Aspirin"), getEntryTexts(content, "#medications"));
    }

    @Test
    public void viewedPatientSet_manyEntries_numbersContinueInOrder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withAllergens("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")
                .build());

        assertEquals("10.", getNumberTexts(content, "#allergens").get(9));
    }

    @Test
    public void viewedPatientChanged_showsNewPatientWithoutOldEntries() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withMedications("Metformin 500mg", "Amlodipine 5mg").build());
        viewedPatient.set(new PatientBuilder().withName("Bob Choo").withMedications("Aspirin").build());

        assertEquals("Bob Choo", ((Label) lookup(content, "#name")).getText());
        assertEquals(List.of("Aspirin"), getEntryTexts(content, "#medications"));
    }

    @Test
    public void viewedPatientCleared_showsPlaceholder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());
        viewedPatient.set(null);

        assertTrue(lookup(content, "#placeholder").isVisible());
        assertFalse(lookup(content, "#details").isVisible());
    }

    @Test
    public void viewedPatient_contacts_numberedInOrderAndRenumbered() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withContacts(GP, GUARDIAN).build());
        assertEquals(List.of(GP.toString(), GUARDIAN.toString()), getEntryTexts(content, "#contacts"));
        VBox entries = (VBox) lookup(content, "#contacts");
        for (int i = 0; i < entries.getChildren().size(); i++) {
            HBox row = (HBox) entries.getChildren().get(i);
            assertEquals((i + 1) + ".", ((Label) row.getChildren().get(0)).getText());
        }
        viewedPatient.set(new PatientBuilder().withContacts(GUARDIAN).build());
        assertEquals(List.of(GUARDIAN.toString()), getEntryTexts(content, "#contacts"));
        HBox remainingRow = (HBox) entries.getChildren().get(0);
        assertEquals("1.", ((Label) remainingRow.getChildren().get(0)).getText());
        viewedPatient.set(new PatientBuilder().build());
        assertEquals(1, entries.getChildren().size());
        assertEquals(MESSAGE_NONE_RECORDED, ((Label) entries.getChildren().get(0)).getText());
    }

    /**
     * Creates a panel bound to {@code viewedPatient} and returns the content of its scroll pane.
     */
    private VBox createPanelContent() {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        PatientDetailPanel panel = new PatientDetailPanel(viewedPatient);
        return (VBox) ((ScrollPane) panel.getRoot()).getContent();
    }

    private static Node lookup(VBox content, String selector) {
        return content.lookup(selector);
    }

    /**
     * Returns the numbers shown beside the entries of the care list with the given {@code selector}.
     */
    private static List<String> getNumberTexts(VBox content, String selector) {
        return ((VBox) lookup(content, selector)).getChildren().stream()
                .map(row -> ((Label) ((HBox) row).getChildren().get(0)).getText())
                .collect(Collectors.toList());
    }

    /**
     * Returns the entry texts shown in the care list with the given {@code selector}, without their numbers.
     */
    private static List<String> getEntryTexts(VBox content, String selector) {
        return ((VBox) lookup(content, selector)).getChildren().stream()
                .map(row -> ((Label) ((HBox) row).getChildren().get(1)).getText())
                .collect(Collectors.toList());
    }
}
