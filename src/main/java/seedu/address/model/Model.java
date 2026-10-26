package seedu.address.model;

import java.util.List;
import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Patient> PREDICATE_SHOW_ALL_PATIENTS = unused -> true;

    /** {@code Predicate} that evaluates to true for active patients */
    Predicate<Patient> PREDICATE_SHOW_ACTIVE_PATIENTS = patient -> !patient.isArchived();

    /** {@code Predicate} that evaluates to true for archived patients */
    Predicate<Patient> PREDICATE_SHOW_ARCHIVED_PATIENTS = Patient::isArchived;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a patient with the same identity as {@code patient} exists in the address book.
     */
    boolean hasPatient(Patient patient);

    /**
     * Deletes the given patient.
     * The patient must exist in the address book.
     */
    void deletePatient(Patient target);

    /**
     * Adds the given patient.
     * {@code patient} must not already exist in the address book.
     */
    void addPatient(Patient patient);

    /**
     * Replaces the given patient {@code target} with {@code editedPatient}.
     * {@code target} must exist in the address book.
     * The patient identity of {@code editedPatient} must not be the same as another existing patient in the
     * address book. If a viewed active patient is archived, the viewed patient is cleared.
     */
    void setPatient(Patient target, Patient editedPatient);

    /** Returns an unmodifiable view of the filtered patient list */
    ObservableList<Patient> getFilteredPatientList();

    /**
     * Updates the filter of the filtered patient list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPatientList(Predicate<Patient> predicate);

    /**
     * Returns the patient whose full details are being viewed.
     * The value is null when no patient is being viewed.
     */
    ObservableValue<Patient> getViewedPatient();

    /**
     * Sets the patient whose full details are being viewed, and shows them again even if they already are,
     * with upcoming and past appointments decided at the current time.
     * {@code patient} must exist in the address book.
     */
    void setViewedPatient(Patient patient);

    /**
     * Returns the appointments of {@code patient} in the order the detail panel numbers them:
     * upcoming ones first, then past ones. For the viewed patient, upcoming and past are decided at the
     * time they were last shown, so the numbers stay the same as the panel's even after an appointment
     * passes. For any other patient, they are decided at the current time.
     */
    List<Appointment> getAppointmentsAsShown(Patient patient);
}
