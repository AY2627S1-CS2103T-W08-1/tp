package seedu.address.ui;

import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * Panel showing the full details of the patient being viewed.
 */
public class PatientDetailPanel extends UiPart<Region> {

    public static final String MESSAGE_NO_PATIENT =
            "Enter 'view INDEX' to see a patient's full details here.";
    public static final String MESSAGE_NONE_RECORDED = "None recorded";

    private static final String FXML = "PatientDetailPanel.fxml";
    private static final String ENTRY_BULLET = "\u2022";
    private static final double ENTRY_BULLET_GAP = 6;
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
    private VBox appointments;

    /**
     * Creates a {@code PatientDetailPanel} that shows {@code viewedPatient} and updates whenever it changes.
     */
    public PatientDetailPanel(ObservableValue<Patient> viewedPatient) {
        super(FXML);
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
     * Fills the detail labels with the fields, care lists and appointments of {@code patient}.
     */
    private void fillDetails(Patient patient) {
        name.setText(patient.getName().fullName);
        nric.setText("NRIC: " + patient.getNric());
        dob.setText("DOB: " + patient.getDob());
        phone.setText("Phone: " + patient.getPhone());
        address.setText("Address: " + patient.getAddress());
        fillEntries(medications, patient.getMedications());
        fillEntries(allergens, patient.getAllergens());
        fillEntries(careInstructions, patient.getCareInstructions());
        fillEntries(appointments, patient.getAppointments().stream().map(Appointment::toString).toList());
    }

    /**
     * Replaces the contents of {@code container} with one bulleted row per entry in {@code entries},
     * or a single {@code MESSAGE_NONE_RECORDED} label if {@code entries} is empty.
     * Each entry is a separate row so that a long entry wraps under its own bullet.
     */
    private static void fillEntries(VBox container, List<String> entries) {
        container.getChildren().clear();

        if (entries.isEmpty()) {
            container.getChildren().add(createLabel(MESSAGE_NONE_RECORDED));
            return;
        }

        for (String entry : entries) {
            Label entryLabel = createLabel(entry);
            entryLabel.setMinWidth(0);
            HBox.setHgrow(entryLabel, Priority.ALWAYS);
            container.getChildren().add(new HBox(ENTRY_BULLET_GAP, createLabel(ENTRY_BULLET), entryLabel));
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
