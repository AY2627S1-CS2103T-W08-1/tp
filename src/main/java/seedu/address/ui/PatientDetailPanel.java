package seedu.address.ui;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.patient.Patient;

/**
 * Panel showing the full details of the patient being viewed.
 */
public class PatientDetailPanel extends UiPart<Region> {

    public static final String MESSAGE_NO_PATIENT =
            "Enter 'view INDEX' to see a patient's full details here.";
    public static final String MESSAGE_NONE_RECORDED = "None recorded";

    private static final String FXML = "PatientDetailPanel.fxml";
    private static final String ENTRY_BULLET = "- ";

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
    private Label medications;
    @FXML
    private Label allergens;
    @FXML
    private Label careInstructions;

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
     * Fills the detail labels with the fields and care lists of {@code patient}.
     */
    private void fillDetails(Patient patient) {
        name.setText(patient.getName().fullName);
        nric.setText("NRIC: " + patient.getNric());
        dob.setText("DOB: " + patient.getDob());
        phone.setText("Phone: " + patient.getPhone());
        address.setText("Address: " + patient.getAddress());
        medications.setText(formatEntries(patient.getMedications()));
        allergens.setText(formatEntries(patient.getAllergens()));
        careInstructions.setText(formatEntries(patient.getCareInstructions()));
    }

    /**
     * Returns {@code entries} with one bulleted entry per line.
     * Returns {@code MESSAGE_NONE_RECORDED} instead if {@code entries} is empty.
     */
    static String formatEntries(List<String> entries) {
        if (entries.isEmpty()) {
            return MESSAGE_NONE_RECORDED;
        }

        return entries.stream()
                .map(entry -> ENTRY_BULLET + entry)
                .collect(Collectors.joining("\n"));
    }

    private static void setShown(Region region, boolean isShown) {
        region.setVisible(isShown);
        region.setManaged(isShown);
    }
}
