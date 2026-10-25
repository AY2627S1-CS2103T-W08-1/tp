package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalContacts.GUARDIAN;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.PatientDetailPanel.COPY_MENU_TEXT;
import static seedu.address.ui.PatientDetailPanel.MESSAGE_NO_PATIENT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.logic.PatientFormatter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class PatientDetailPanelTest {

    /** Time at which the panel's appointments are split into upcoming and past ones. */
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 12, 0);

    private static boolean isToolkitAvailable;

    private final SimpleObjectProperty<Patient> viewedPatient = new SimpleObjectProperty<>();

    /**
     * Starts the JavaFX toolkit. The tests in this class are skipped in environments without a display.
     */
    @BeforeAll
    public static void startToolkit() {
        JavaFxTestUtil.startToolkit();
        isToolkitAvailable = JavaFxTestUtil.isToolkitAvailable();
    }

    @Test
    public void constructor_noPatientViewed_showsPlaceholder() {
        VBox content = createPanelContent();

        assertTrue(lookup(content, "#placeholder").isVisible());
        assertEquals(MESSAGE_NO_PATIENT, ((Label) lookup(content, "#placeholder")).getText());
        assertFalse(lookup(content, "#details").isVisible());
    }

    @Test
    public void viewedPatient_patientSet_showsPatientFields() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());

        assertFalse(lookup(content, "#placeholder").isVisible());
        assertTrue(lookup(content, "#details").isVisible());
        assertEquals(PatientBuilder.DEFAULT_NAME, ((Label) lookup(content, "#name")).getText());
        assertEquals(List.of("NRIC: " + PatientBuilder.DEFAULT_NRIC, "DOB: " + PatientBuilder.DEFAULT_DOB,
                "Phone: " + PatientBuilder.DEFAULT_PHONE, "Address: " + PatientBuilder.DEFAULT_ADDRESS),
                getLabelTexts(content, "#basicDetails"));
    }

    @Test
    public void constructor_headings_wordedAsExportedRecords() {
        VBox content = createPanelContent();

        assertEquals(PatientFormatter.HEADING_MEDICATIONS, getLabelText(content, "#medicationsHeading"));
        assertEquals(PatientFormatter.HEADING_ALLERGENS, getLabelText(content, "#allergensHeading"));
        assertEquals(PatientFormatter.HEADING_CARE_INSTRUCTIONS, getLabelText(content, "#careInstructionsHeading"));
        assertEquals(PatientFormatter.HEADING_CONTACTS, getLabelText(content, "#contactsHeading"));
        assertEquals(PatientFormatter.HEADING_APPOINTMENTS, getLabelText(content, "#appointmentsHeading"));
    }

    @Test
    public void copyMenu_numbersAndNoneRecorded_notGivenMenu() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withAllergens("Penicillin").build());
        HBox allergenRow = (HBox) ((VBox) lookup(content, "#allergens")).getChildren().get(0);
        Label noMedications = (Label) ((VBox) lookup(content, "#medications")).getChildren().get(0);

        assertNull(((Label) allergenRow.getChildren().get(0)).getContextMenu());
        assertNull(noMedications.getContextMenu());
    }

    @Test
    public void copyMenu_entryAndName_copiesTheirText() throws Exception {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withAllergens("Penicillin").build());
        HBox allergenRow = (HBox) ((VBox) lookup(content, "#allergens")).getChildren().get(0);

        assertEquals("Penicillin", copyWithMenu((Label) allergenRow.getChildren().get(1)));
        assertEquals(PatientBuilder.DEFAULT_NAME, copyWithMenu((Label) lookup(content, "#name")));
        assertEquals("NRIC: " + PatientBuilder.DEFAULT_NRIC,
                copyWithMenu((Label) ((VBox) lookup(content, "#basicDetails")).getChildren().get(0)));
    }

    @Test
    public void viewedPatient_emptyCareLists_showsNoneRecorded() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());

        for (String id : List.of("#medications", "#allergens", "#careInstructions", "#appointments")) {
            VBox entries = (VBox) lookup(content, id);
            assertEquals(1, entries.getChildren().size());
            assertEquals(PatientFormatter.NONE_RECORDED, ((Label) entries.getChildren().get(0)).getText());
        }
    }

    @Test
    public void viewedPatient_appointments_numberedUpcomingFirst() {
        VBox content = createPanelContent();
        Appointment past = new Appointment(LocalDate.of(2000, 1, 1), LocalTime.of(9, 0), "Dr Lee", "");
        Appointment upcoming = new Appointment(LocalDate.of(2999, 1, 1), LocalTime.of(9, 0), "Dr Tan", "");
        viewedPatient.set(new PatientBuilder().withAppointments(past, upcoming).build());

        assertEquals(List.of(upcoming.toString(), past.toString()), getEntryTexts(content, "#appointments"));
        assertEquals(List.of("1.", "2."), getNumberTexts(content, "#appointments"));
    }

    @Test
    public void viewedPatient_careEntries_showsOneNumberedWrappingRowPerEntryInOrder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder()
                .withMedications("Metformin 500mg", "Amlodipine 5mg")
                .withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar before breakfast")
                .withAppointments(CHECKUP)
                .build());

        assertEquals(List.of("Metformin 500mg", "Amlodipine 5mg"), getEntryTexts(content, "#medications"));
        assertEquals(List.of("Penicillin"), getEntryTexts(content, "#allergens"));
        assertEquals(List.of("Check blood sugar before breakfast"), getEntryTexts(content, "#careInstructions"));
        assertEquals(List.of(CHECKUP.toString()), getEntryTexts(content, "#appointments"));

        assertEquals(List.of("1.", "2."), getNumberTexts(content, "#medications"));
        assertEquals(List.of("1."), getNumberTexts(content, "#allergens"));
        assertEquals(List.of("1."), getNumberTexts(content, "#careInstructions"));

        HBox row = (HBox) ((VBox) lookup(content, "#medications")).getChildren().get(0);
        assertTrue(((Label) row.getChildren().get(1)).isWrapText());
    }

    @Test
    public void viewedPatient_medicationsWithTimes_showsTimesBesideDescription() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder()
                .withMedications(new Medication("Metformin 500mg", List.of(LocalTime.of(20, 0), LocalTime.of(8, 0))),
                        new Medication("Aspirin"))
                .build());

        assertEquals(List.of("Metformin 500mg (08:00, 20:00)", "Aspirin"), getEntryTexts(content, "#medications"));
    }

    @Test
    public void viewedPatient_manyEntries_numbersContinueInOrder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withAllergens("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")
                .build());

        assertEquals("10.", getNumberTexts(content, "#allergens").get(9));
    }

    @Test
    public void viewedPatient_patientChanged_showsNewPatientWithoutOldEntries() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().withMedications("Metformin 500mg", "Amlodipine 5mg").build());
        viewedPatient.set(new PatientBuilder().withName("Bob Choo").withMedications("Aspirin").build());

        assertEquals("Bob Choo", ((Label) lookup(content, "#name")).getText());
        assertEquals(List.of("Aspirin"), getEntryTexts(content, "#medications"));
    }

    @Test
    public void viewedPatient_patientCleared_showsPlaceholder() {
        VBox content = createPanelContent();
        viewedPatient.set(new PatientBuilder().build());
        viewedPatient.set(null);

        assertTrue(lookup(content, "#placeholder").isVisible());
        assertFalse(lookup(content, "#details").isVisible());
    }

    @Test
    public void viewedPatient_contacts_numberedInOrderAndRenumbered() {
        VBox content = createPanelContent();
        Patient patient = new PatientBuilder().withContacts(GP, GUARDIAN)
                .withMedications(new Medication("Metformin", List.of(LocalTime.of(8, 0)))).build();
        viewedPatient.set(patient);
        assertEquals(List.of(GP.toString(), GUARDIAN.toString()), getEntryTexts(content, "#contacts"));
        assertEquals(List.of("Metformin (08:00)"), getEntryTexts(content, "#medications"));
        assertEquals(List.of("1."), getNumberTexts(content, "#medications"));
        VBox entries = (VBox) lookup(content, "#contacts");
        for (int i = 0; i < entries.getChildren().size(); i++) {
            HBox row = (HBox) entries.getChildren().get(i);
            assertEquals((i + 1) + ".", ((Label) row.getChildren().get(0)).getText());
        }
        viewedPatient.set(patient.withContacts(List.of(GUARDIAN)));
        assertEquals(List.of(GUARDIAN.toString()), getEntryTexts(content, "#contacts"));
        assertEquals(List.of("Metformin (08:00)"), getEntryTexts(content, "#medications"));
        HBox remainingRow = (HBox) entries.getChildren().get(0);
        assertEquals("1.", ((Label) remainingRow.getChildren().get(0)).getText());
        viewedPatient.set(new PatientBuilder().build());
        assertEquals(1, entries.getChildren().size());
        assertEquals(PatientFormatter.NONE_RECORDED, ((Label) entries.getChildren().get(0)).getText());
    }

    /**
     * Creates a panel bound to {@code viewedPatient} and returns the content of its scroll pane.
     */
    private VBox createPanelContent() {
        assumeTrue(isToolkitAvailable, "JavaFX toolkit is not available");
        PatientDetailPanel panel = new PatientDetailPanel(viewedPatient,
                patient -> patient.getAppointmentsUpcomingFirst(NOW));
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

    private static String getLabelText(VBox content, String selector) {
        return ((Label) lookup(content, selector)).getText();
    }

    /**
     * Returns the texts of the labels in the box with the given {@code selector}.
     */
    private static List<String> getLabelTexts(VBox content, String selector) {
        return ((VBox) lookup(content, selector)).getChildren().stream()
                .map(label -> ((Label) label).getText())
                .collect(Collectors.toList());
    }

    /**
     * Clicks the Copy item of the right-click menu of {@code label}, and returns the text then on the clipboard.
     */
    private static String copyWithMenu(Label label) throws Exception {
        assertEquals(COPY_MENU_TEXT, label.getContextMenu().getItems().get(0).getText());
        return runOnFxThread(() -> {
            label.getContextMenu().getItems().get(0).fire();
            return Clipboard.getSystemClipboard().getString();
        });
    }
}
