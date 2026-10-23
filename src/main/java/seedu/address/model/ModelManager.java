package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Patient> filteredPatients;
    private final ObjectProperty<Patient> viewedPatient = new SimpleObjectProperty<>();
    private final Clock clock;
    private LocalDateTime viewedPatientShownAt;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs, using the system clock.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, Clock.systemDefaultZone());
    }

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs, using {@code clock} to tell which
     * appointments are upcoming.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, Clock clock) {
        requireAllNonNull(addressBook, userPrefs, clock);

        logger.fine("Initializing with " + addressBook.getPatientList().size() + " patient(s) and user prefs "
                + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        this.clock = clock;
        filteredPatients = new FilteredList<>(this.addressBook.getPatientList(), PREDICATE_SHOW_ACTIVE_PATIENTS);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);

        if (!this.addressBook.getPatientList().contains(viewedPatient.get())) {
            viewedPatient.set(null);
        }
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPatient(Patient patient) {
        requireNonNull(patient);
        return addressBook.hasPatient(patient);
    }

    @Override
    public void deletePatient(Patient target) {
        addressBook.removePatient(target);

        if (target.equals(viewedPatient.get())) {
            viewedPatient.set(null);
        }
    }

    @Override
    public void addPatient(Patient patient) {
        addressBook.addPatient(patient);
        updateFilteredPatientList(PREDICATE_SHOW_ACTIVE_PATIENTS);
    }

    @Override
    public void setPatient(Patient target, Patient editedPatient) {
        requireAllNonNull(target, editedPatient);

        addressBook.setPatient(target, editedPatient);

        if (target.equals(viewedPatient.get())) {
            viewedPatient.set(editedPatient);
        }
    }

    //=========== Filtered Patient List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Patient} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Patient> getFilteredPatientList() {
        return filteredPatients;
    }

    @Override
    public void updateFilteredPatientList(Predicate<Patient> predicate) {
        requireNonNull(predicate);
        filteredPatients.setPredicate(predicate);
    }

    //=========== Viewed Patient Accessors ===================================================================

    @Override
    public ObservableValue<Patient> getViewedPatient() {
        return viewedPatient;
    }

    @Override
    public void setViewedPatient(Patient patient) {
        requireNonNull(patient);
        assert addressBook.hasPatient(patient) : "Viewed patient must exist in MediConnect";

        viewedPatientShownAt = LocalDateTime.now(clock);
        if (patient.equals(viewedPatient.get())) {
            // clears first so that listeners redraw the same patient with the new order of appointments
            viewedPatient.set(null);
        }
        viewedPatient.set(patient);
    }

    @Override
    public LocalDateTime getAppointmentsOrderedAt(Patient patient) {
        requireNonNull(patient);
        if (!patient.equals(viewedPatient.get())) {
            return LocalDateTime.now(clock);
        }
        assert viewedPatientShownAt != null : "A viewed patient must have a time they were shown at";
        return viewedPatientShownAt;
    }

    @Override
    public List<Appointment> getAppointmentsAsShown(Patient patient) {
        requireNonNull(patient);
        return patient.getAppointmentsUpcomingFirst(getAppointmentsOrderedAt(patient));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPatients.equals(otherModelManager.filteredPatients)
                && Objects.equals(viewedPatient.get(), otherModelManager.viewedPatient.get());
    }

}
