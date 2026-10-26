package seedu.address.ui;

import java.util.List;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Panel showing the full details of the patient being viewed.
 */
public class PatientDetailPanel extends UiPart<Region> {

    public static final String MESSAGE_NO_PATIENT =
            "Enter 'view INDEX' to see a patient's full details here.";
    public static final String MESSAGE_NONE_RECORDED = "None recorded";

    private static final String FXML = "PatientDetailPanel.fxml";
    private static final String ARCHIVED_MARKER = " (Archived)";
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
    private Label nric;
    @FXML
    private Label dob;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private VBox medications;
    @FXML
    private VBox allergens;
    @FXML
    private VBox careInstructions;
    @FXML
    private VBox contacts;
    @FXML
    private VBox appointments;

    private final Function<Patient, List<String>> appointmentLinesAsShown;

    /**
     * Creates a {@code PatientDetailPanel} that shows {@code viewedPatient} and updates whenever it changes.
     * Appointments are shown as given by {@code appointmentLinesAsShown}, in the order commands also number them.
     */
    public PatientDetailPanel(ObservableValue<Patient> viewedPatient,
            Function<Patient, List<String>> appointmentLinesAsShown) {
        super(FXML);
        this.appointmentLinesAsShown = appointmentLinesAsShown;
        placeholder.setText(MESSAGE_NO_PATIENT);
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
     * Fills the detail labels with the fields, care lists, contacts and appointments of {@code patient}.
     */
    private void fillDetails(Patient patient) {
        name.setText(patient.getName().fullName + (patient.isArchived() ? ARCHIVED_MARKER : ""));
        nric.setText("NRIC: " + patient.getNric());
        dob.setText("DOB: " + patient.getDob());
        phone.setText("Phone: " + patient.getPhone());
        address.setText("Address: " + patient.getAddress());
        fillEntries(medications, patient.getMedications().stream().map(Medication::toString).toList());
        fillEntries(allergens, patient.getAllergens());
        fillEntries(careInstructions, patient.getCareInstructions());
        fillEntries(contacts, patient.getContacts().stream().map(Contact::toString).toList());
        fillEntries(appointments, appointmentLinesAsShown.apply(patient));
    }

    /**
     * Replaces the contents of {@code container} with one numbered row ("1.", "2.", ...) per entry in
     * {@code entries}, or a single {@code MESSAGE_NONE_RECORDED} label if {@code entries} is empty.
     * Each entry is a separate row so that a long entry wraps beside its own number.
     */
    private static void fillEntries(VBox container, List<String> entries) {
        container.getChildren().clear();

        if (entries.isEmpty()) {
            container.getChildren().add(createLabel(MESSAGE_NONE_RECORDED));
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

    private static Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(ENTRY_STYLE_CLASS);
        label.setWrapText(true);
        return label;
    }

    private static void setShown(Region region, boolean isShown) {
        region.setVisible(isShown);
        region.setManaged(isShown);
    }
}
