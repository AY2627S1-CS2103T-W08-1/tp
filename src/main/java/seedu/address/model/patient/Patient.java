package seedu.address.model.patient;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.appointment.Appointment;

/**
 * Represents a Patient in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Patient {

    public static final int MAX_CARE_INSTRUCTION_LENGTH = 500;
    public static final int MAX_ALLERGEN_LENGTH = 500;
    public static final String MESSAGE_ALLERGEN_TOO_LONG =
            "Allergen entries cannot exceed " + MAX_ALLERGEN_LENGTH + " characters.";
    public static final String MESSAGE_CARE_INSTRUCTION_TOO_LONG =
            "Care instructions cannot exceed " + MAX_CARE_INSTRUCTION_LENGTH + " characters.";
    public static final String MESSAGE_BLANK_ALLERGEN = "Allergen entries cannot be blank.";
    public static final String MESSAGE_BLANK_CARE_INSTRUCTION = "Care instructions cannot be blank.";
    public static final String MESSAGE_DUPLICATE_ALLERGEN =
            "A patient cannot have duplicate allergens, ignoring letter case and whitespace differences.";
    public static final String MESSAGE_DUPLICATE_CARE_INSTRUCTION =
            "A patient cannot have duplicate care instructions, ignoring letter case and whitespace differences.";

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Nric nric;
    private final Dob dob;

    // Data fields
    private final Address address;
    private final List<Medication> medications;
    private final List<String> allergens;
    private final List<String> careInstructions;
    private final List<Contact> contacts;
    private final List<Appointment> appointments;
    private final boolean archived;

    /**
     * Every field must be present and not null.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address,
            List<Medication> medications, List<String> allergens, List<String> careInstructions) {
        this(name, nric, dob, phone, address, medications, allergens, careInstructions, List.of());
    }

    /**
     * Creates a patient with contacts in the order added and no appointments.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address,
            List<Medication> medications, List<String> allergens, List<String> careInstructions,
            List<Contact> contacts) {
        this(name, nric, dob, phone, address, medications, allergens, careInstructions, contacts, List.of(), false);
    }

    /**
     * Creates a patient with contacts and the requested archived state.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address,
            List<Medication> medications, List<String> allergens, List<String> careInstructions,
            List<Contact> contacts, boolean archived) {
        this(name, nric, dob, phone, address, medications, allergens, careInstructions, contacts, List.of(), archived);
    }

    /**
     * Creates a patient with contacts in the order added and appointments sorted by date, then time.
     * Duplicate contact identities and null appointments are rejected.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address,
            List<Medication> medications, List<String> allergens, List<String> careInstructions,
            List<Contact> contacts, List<Appointment> appointments) {
        this(name, nric, dob, phone, address, medications, allergens, careInstructions, contacts, appointments, false);
    }

    /**
     * Creates a patient with contacts, appointments, and the requested archived state.
     * Duplicate contact identities and null appointments are rejected.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address,
            List<Medication> medications, List<String> allergens, List<String> careInstructions,
            List<Contact> contacts, List<Appointment> appointments, boolean archived) {
        requireAllNonNull(name, nric, dob, phone, address, medications, allergens, careInstructions, contacts,
                appointments);
        this.name = name;
        this.nric = nric;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.medications = List.copyOf(medications);
        this.allergens = List.copyOf(allergens);
        this.careInstructions = List.copyOf(careInstructions);
        this.contacts = List.copyOf(contacts);
        this.archived = archived;
        verifyCareEntries(this.allergens, MESSAGE_BLANK_ALLERGEN, MESSAGE_DUPLICATE_ALLERGEN);
        verifyCareEntries(this.careInstructions, MESSAGE_BLANK_CARE_INSTRUCTION,
                MESSAGE_DUPLICATE_CARE_INSTRUCTION);
        verifyCareEntryLengths(this.allergens, MAX_ALLERGEN_LENGTH, MESSAGE_ALLERGEN_TOO_LONG);
        verifyCareInstructionLengths(this.careInstructions);
        verifyNoDuplicateContacts(this.contacts);
        // List.copyOf rejects null appointments before they are sorted.
        this.appointments = List.copyOf(appointments).stream().sorted().toList();
        verifyNoDuplicateAppointments(this.appointments);
    }

    /**
     * Creates a patient with empty care lists.
     */
    public Patient(Name name, Nric nric, Dob dob, Phone phone, Address address) {
        this(name, nric, dob, phone, address, List.of(), List.of(), List.of());
    }

    /**
     * Rejects contacts with duplicate name-and-phone identities.
     */
    private static void verifyNoDuplicateContacts(List<Contact> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            checkArgument(contacts.subList(0, i).stream().noneMatch(contact::isSameContact),
                    Contact.MESSAGE_DUPLICATE);
        }
    }

    /**
     * Rejects blank or duplicate care entries. Duplicate checks ignore letter case and whitespace differences,
     * matching the care commands.
     */
    private static void verifyCareEntries(List<String> entries, String blankMessage, String duplicateMessage) {
        for (int i = 0; i < entries.size(); i++) {
            String normalizedEntry = normalizeCareEntry(entries.get(i));
            checkArgument(!normalizedEntry.isEmpty(), blankMessage);
            checkArgument(entries.subList(0, i).stream()
                    .map(Patient::normalizeCareEntry)
                    .noneMatch(normalizedEntry::equalsIgnoreCase), duplicateMessage);
        }
    }

    /**
     * Returns a care entry in NFC form, with Unicode whitespace collapsed and removed from both ends.
     */
    public static String normalizeCareEntry(String entry) {
        Objects.requireNonNull(entry);
        String normalizedWhitespace = entry.replaceAll("(?U)\\s+", " ").trim();
        return Normalizer.normalize(normalizedWhitespace, Normalizer.Form.NFC);
    }

    /**
     * Rejects appointments at the same date and time with the same provider, as {@code addappt} does, so that
     * a data file cannot hold them either.
     */
    private static void verifyNoDuplicateAppointments(List<Appointment> appointments) {
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            checkArgument(appointments.subList(0, i).stream().noneMatch(appointment::isSameAppointment),
                    Appointment.MESSAGE_DUPLICATE);
        }
    }

    /**
     * Rejects care instructions that are too long to display and edit safely.
     */
    private static void verifyCareInstructionLengths(List<String> careInstructions) {
        verifyCareEntryLengths(careInstructions, MAX_CARE_INSTRUCTION_LENGTH,
                MESSAGE_CARE_INSTRUCTION_TOO_LONG);
    }

    private static void verifyCareEntryLengths(List<String> entries, int maxLength, String message) {
        checkArgument(entries.stream()
                .map(Patient::normalizeCareEntry)
                .allMatch(entry -> isWithinMaxLength(entry, maxLength)), message);
    }

    /**
     * Returns true if {@code careInstruction} does not exceed the maximum care-instruction length.
     */
    public static boolean isCareInstructionWithinMaxLength(String careInstruction) {
        return isWithinMaxLength(careInstruction, MAX_CARE_INSTRUCTION_LENGTH);
    }

    /**
     * Returns true if {@code allergen} does not exceed the maximum allergen length.
     */
    public static boolean isAllergenWithinMaxLength(String allergen) {
        return isWithinMaxLength(allergen, MAX_ALLERGEN_LENGTH);
    }

    private static boolean isWithinMaxLength(String entry, int maxLength) {
        return entry.codePointCount(0, entry.length()) <= maxLength;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Nric getNric() {
        return nric;
    }

    public Dob getDob() {
        return dob;
    }

    public Address getAddress() {
        return address;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    /**
     * Returns a copy with replacement medications, preserving every other field.
     */
    public Patient withMedications(List<Medication> medications) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    public List<String> getAllergens() {
        return allergens;
    }

    /**
     * Returns a copy with replacement allergens, preserving every other field.
     */
    public Patient withAllergens(List<String> allergens) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    public List<String> getCareInstructions() {
        return careInstructions;
    }

    /**
     * Returns the immutable contacts in insertion order.
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    public boolean isArchived() {
        return archived;
    }

    /**
     * Returns a copy with replacement core details, preserving every care-related field and state.
     */
    public Patient withDetails(Name name, Nric nric, Dob dob, Phone phone, Address address) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    /**
     * Returns the immutable appointments, sorted by date, then time.
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Returns a copy with replacement appointments, preserving every other field.
     */
    public Patient withAppointments(List<Appointment> appointments) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    /**
     * Returns the appointments in the order the detail panel numbers them: upcoming ones (at or after
     * {@code now}) first, then past ones, each sorted by date, then time.
     */
    public List<Appointment> getAppointmentsUpcomingFirst(LocalDateTime now) {
        Objects.requireNonNull(now);
        Stream<Appointment> upcoming = appointments.stream()
                .filter(appointment -> !appointment.isBefore(now));
        Stream<Appointment> past = appointments.stream()
                .filter(appointment -> appointment.isBefore(now));
        return Stream.concat(upcoming, past).toList();
    }

    /**
     * Returns a copy with replacement care instructions, preserving every other field.
     */
    public Patient withCareInstructions(List<String> careInstructions) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    /**
     * Returns a copy with replacement contacts, preserving every other field.
     */
    public Patient withContacts(List<Contact> contacts) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    /**
     * Returns a copy with the requested archived state, preserving every other field.
     */
    public Patient withArchived(boolean archived) {
        return new Patient(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    /**
     * Returns true if both patients have the same NRIC (case-insensitive).
     * This defines a weaker notion of equality between two patients.
     */
    public boolean isSamePatient(Patient otherPatient) {
        if (otherPatient == this) {
            return true;
        }

        return otherPatient != null
                && otherPatient.getNric().equals(getNric());
    }

    /**
     * Returns true if both patients have the same identity and data fields.
     * This defines a stronger notion of equality between two patients.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Patient otherPatient)) {
            return false;
        }

        return name.equals(otherPatient.name)
                && phone.equals(otherPatient.phone)
                && nric.equals(otherPatient.nric)
                && dob.equals(otherPatient.dob)
                && address.equals(otherPatient.address)
                && medications.equals(otherPatient.medications)
                && allergens.equals(otherPatient.allergens)
                && careInstructions.equals(otherPatient.careInstructions)
                && contacts.equals(otherPatient.contacts)
                && appointments.equals(otherPatient.appointments)
                && archived == otherPatient.archived;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, nric, dob, phone, address, medications, allergens, careInstructions,
                contacts, appointments, archived);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("nric", nric)
                .add("dob", dob)
                .add("address", address)
                .add("medications", medications)
                .add("allergens", allergens)
                .add("careInstructions", careInstructions)
                .add("contacts", contacts)
                .add("appointments", appointments)
                .add("archived", archived)
                .toString();
    }

}
