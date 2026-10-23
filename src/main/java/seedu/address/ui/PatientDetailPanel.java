package seedu.address.ui;

import java.util.List;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.PatientFormatter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Panel showing the full details of the patient being viewed.
 */
public class PatientDetailPanel extends UiPart<Region> {

    public static final String MESSAGE_NO_PATIENT =
            "Enter 'view INDEX' to see a patient's full details here.";
    /** Text of the menu item, shown on right-clicking a line of the panel, that copies the line. */
    public static final String COPY_MENU_TEXT = "Copy";

    private static final String FXML = "PatientDetailPanel.fxml";
    private static final String ENTRY_NUMBER_SUFFIX = ".";
    private static final double ENTRY_NUMBER_GAP = 6;
    private static final String ENTRY_STYLE_CLASS = "label-bright";

    @FXML
    private Label placeholder;
    @FXML
    private VBox details;
    @FXML
    private Label name;
    @FXML
    private VBox basicDetails;
    @FXML
    private Label medicationsHeading;
    @FXML
    private VBox medications;
    @FXML
    private Label allergensHeading;
    @FXML
    private VBox allergens;
    @FXML
    private Label careInstructionsHeading;
    @FXML
    private VBox careInstructions;
    @FXML
    private Label contactsHeading;
    @FXML
    private VBox contacts;
    @FXML
    private Label appointmentsHeading;
    @FXML
    private VBox appointments;

    private final Function<Patient, List<Appointment>> appointmentsAsShown;

    /**
     * Creates a {@code PatientDetailPanel} that shows {@code viewedPatient} and updates whenever it changes.
     * Appointments are numbered in the order given by {@code appointmentsAsShown}, which commands also use.
     * The details and headings are worded by {@code PatientFormatter}, as in exported records.
     */
    public PatientDetailPanel(ObservableValue<Patient> viewedPatient,
            Function<Patient, List<Appointment>> appointmentsAsShown) {
        super(FXML);
        this.appointmentsAsShown = appointmentsAsShown;
        placeholder.setText(MESSAGE_NO_PATIENT);
        medicationsHeading.setText(PatientFormatter.HEADING_MEDICATIONS);
        allergensHeading.setText(PatientFormatter.HEADING_ALLERGENS);
        careInstructionsHeading.setText(PatientFormatter.HEADING_CARE_INSTRUCTIONS);
        contactsHeading.setText(PatientFormatter.HEADING_CONTACTS);
        appointmentsHeading.setText(PatientFormatter.HEADING_APPOINTMENTS);
        addCopyMenu(name);
        viewedPatient.addListener((observable, oldPatient, newPatient) -> showPatient(newPatient));
        showPatient(viewedPatient.getValue());
    }

    /**
     * Shows the details of {@code patient}, or the placeholder message if {@code patient} is null.
     */
    private void showPatient(Patient patient) {
        boolean hasPatient = patient != null;
        setShown(placeholder, !hasPatient);
        setShown(details, hasPatient);

        if (hasPatient) {
            fillDetails(patient);
        }
    }

    /**
     * Fills the panel with the name, basic details, care lists, contacts and appointments of {@code patient}.
     */
    private void fillDetails(Patient patient) {
        name.setText(patient.getName().fullName);
        basicDetails.getChildren().setAll(PatientFormatter.formatDetails(patient).stream()
                .map(PatientDetailPanel::createLabel).toList());
        fillEntries(medications, patient.getMedications().stream().map(Medication::toString).toList());
        fillEntries(allergens, patient.getAllergens());
        fillEntries(careInstructions, patient.getCareInstructions());
        fillEntries(contacts, patient.getContacts().stream().map(Contact::toString).toList());
        fillEntries(appointments, appointmentsAsShown.apply(patient).stream()
                .map(Appointment::toString).toList());
    }

    /**
     * Replaces the contents of {@code container} with one numbered row ("1.", "2.", ...) per entry in
     * {@code entries}, or a single {@code PatientFormatter.NONE_RECORDED} label if {@code entries} is empty.
     * Each entry is a separate row so that a long entry wraps beside its own number.
     */
    private static void fillEntries(VBox container, List<String> entries) {
        container.getChildren().clear();

        if (entries.isEmpty()) {
            container.getChildren().add(createLabel(PatientFormatter.NONE_RECORDED));
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            Label numberLabel = createLabel((i + 1) + ENTRY_NUMBER_SUFFIX);
            numberLabel.setMinWidth(Region.USE_PREF_SIZE);
            Label entryLabel = createLabel(entries.get(i));
            entryLabel.setMinWidth(0);
            HBox.setHgrow(entryLabel, Priority.ALWAYS);
            container.getChildren().add(new HBox(ENTRY_NUMBER_GAP, numberLabel, entryLabel));
        }
    }

    /**
     * Returns a wrapping label showing {@code text}, which can be copied with a right-click.
     */
    private static Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(ENTRY_STYLE_CLASS);
        label.setWrapText(true);
        addCopyMenu(label);
        return label;
    }

    /**
     * Gives {@code label} a right-click menu with {@code COPY_MENU_TEXT}, which copies its text to the clipboard,
     * since the text of a label cannot be selected.
     */
    private static void addCopyMenu(Label label) {
        MenuItem copyItem = new MenuItem(COPY_MENU_TEXT);
        copyItem.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(label.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        label.setContextMenu(new ContextMenu(copyItem));
    }

    private static void setShown(Region region, boolean isShown) {
        region.setVisible(isShown);
        region.setManaged(isShown);
    }
}
