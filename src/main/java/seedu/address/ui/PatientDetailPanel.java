package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.index.Index;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Panel showing the full details of the patient being viewed.
 */
public class PatientDetailPanel extends UiPart<ScrollPane> {

    public static final String MESSAGE_NO_PATIENT =
            "Enter 'view INDEX' to see a patient's full details here.";
    public static final String MESSAGE_NONE_RECORDED = "None recorded";
    public static final String MESSAGE_LIST_INDEX = "List index: %1$d";
    public static final String MESSAGE_NOT_IN_LIST = "Not in the displayed list";

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
    private Label listIndex;
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

    private final ObservableList<Patient> displayedPatients;
    private final Function<Patient, List<String>> appointmentLinesAsShown;

    /**
     * The last patient shown. It is kept while no patient is shown, so that the same patient shown again, e.g.,
     * after an edit, keeps the scroll position.
     */
    private Patient lastShownPatient;

    /**
     * Creates a {@code PatientDetailPanel} that shows {@code viewedPatient} and updates whenever it changes.
     * The patient's index in {@code displayedPatients} is shown under their name, and updates whenever that list
     * changes. Appointments are shown as given by {@code appointmentLinesAsShown}, in the order commands also
     * number them.
     */
    public PatientDetailPanel(ObservableValue<Patient> viewedPatient, ObservableList<Patient> displayedPatients,
            Function<Patient, List<String>> appointmentLinesAsShown) {
        super(FXML);
        requireAllNonNull(viewedPatient, displayedPatients, appointmentLinesAsShown);
        this.displayedPatients = displayedPatients;
        this.appointmentLinesAsShown = appointmentLinesAsShown;
        placeholder.setText(MESSAGE_NO_PATIENT);
        viewedPatient.addListener((observable, oldPatient, newPatient) -> showPatient(newPatient));
        displayedPatients.addListener((ListChangeListener<Patient>) change -> showListIndex(viewedPatient.getValue()));
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
            scrollToTopIfNewPatient(patient);
            fillDetails(patient);
            showListIndex(patient);
        }
    }

    /**
     * Scrolls the panel back to the top if {@code patient} is not the same patient, by NRIC, as the one shown last,
     * so that a newly viewed patient is shown from their name. The same patient, e.g., redrawn after an edit, keeps
     * the scroll position.
     */
    private void scrollToTopIfNewPatient(Patient patient) {
        if (!patient.isSamePatient(lastShownPatient)) {
            getRoot().setVvalue(getRoot().getVmin());
        }
        lastShownPatient = patient;
    }

    /**
     * Shows the one-based index of {@code patient} in the displayed patient list, the number that commands use,
     * or {@code MESSAGE_NOT_IN_LIST} if the list does not show them. Does nothing if {@code patient} is null.
     */
    private void showListIndex(Patient patient) {
        if (patient == null) {
            return;
        }

        int position = displayedPatients.indexOf(patient);
        // Locale.ROOT writes the index with the digits 0 to 9, as the list numbers its patients, whatever the
        // computer's language settings are
        listIndex.setText(position < 0
                ? MESSAGE_NOT_IN_LIST
                : String.format(Locale.ROOT, MESSAGE_LIST_INDEX, Index.fromZeroBased(position).getOneBased()));
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
