package seedu.address.logic;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Formats a patient's details and numbered lists as lines of text, worded the same as the patient detail panel.
 */
public class PatientFormatter {

    /** Shown in place of the entries of an empty care list. */
    public static final String NONE_RECORDED = "None recorded";

    /** Shown after the basic details of an archived patient. */
    public static final String ARCHIVED_STATUS = "Status: Archived";

    /** Heading of the list of medications. */
    public static final String HEADING_MEDICATIONS = "Medications";

    /** Heading of the list of allergens. */
    public static final String HEADING_ALLERGENS = "Allergens";

    /** Heading of the list of care instructions. */
    public static final String HEADING_CARE_INSTRUCTIONS = "Care Instructions";

    /** Heading of the list of contacts. */
    public static final String HEADING_CONTACTS = "Contacts";

    /** Heading of the list of appointments. */
    public static final String HEADING_APPOINTMENTS = "Appointments";

    /** Added after the date and time of each past appointment. */
    public static final String PAST_MARK = " (past)";

    /** A line break inside an entry. */
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\R");

    private PatientFormatter() {} // prevents instantiation

    /**
     * Returns the basic details of {@code patient} other than the name, one labeled line each,
     * e.g., "NRIC: S1234567A", followed by {@code ARCHIVED_STATUS} if the patient is archived.
     *
     * @param patient Patient whose details are formatted.
     * @return the NRIC, DOB, phone and address lines, in the order the detail panel shows them, and the status line
     *     of an archived patient.
     */
    public static List<String> formatDetails(Patient patient) {
        requireNonNull(patient);
        List<String> details = new ArrayList<>(List.of("NRIC: " + patient.getNric(), "DOB: " + patient.getDob(),
                "Phone: " + patient.getPhone(), "Address: " + patient.getAddress()));
        if (patient.isArchived()) {
            details.add(ARCHIVED_STATUS);
        }
        return details;
    }

    /**
     * Returns one numbered line per entry in {@code entries}, e.g., "1. Metformin 500mg",
     * or a single {@code NONE_RECORDED} line if there are no entries.
     * A line break inside an entry, which only a hand-edited data file can hold, becomes a single space,
     * so that each entry stays on one numbered line.
     *
     * @param entries Entries of one list, in the order to number them.
     * @return the lines to show for the list.
     */
    public static List<String> formatEntries(List<String> entries) {
        requireNonNull(entries);
        if (entries.isEmpty()) {
            return List.of(NONE_RECORDED);
        }

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String entry = LINE_BREAK_PATTERN.splitAsStream(entries.get(i)).map(String::strip)
                    .filter(line -> !line.isEmpty()).collect(Collectors.joining(" "));
            lines.add((i + 1) + ". " + entry);
        }
        return lines;
    }

    /**
     * Returns the text of each of {@code appointments}, in the same order, with {@code PAST_MARK} after the date
     * and time of each appointment before {@code now}, e.g., "2026-10-02 10:00 (past) with Dr Tan".
     *
     * @param appointments Appointments in the order they are numbered.
     * @param now Date and time that decides which appointments are past.
     * @return the entries to number, one per appointment.
     */
    public static List<String> formatAppointments(List<Appointment> appointments, LocalDateTime now) {
        requireAllNonNull(appointments, now);
        return appointments.stream()
                .map(appointment -> formatAppointment(appointment, now))
                .toList();
    }

    /**
     * Returns the text of {@code appointment}, with {@code PAST_MARK} after its date and time if it is before
     * {@code now}. Only the app writes the date and time, so a note or {@code WITH} text, which come after
     * them, cannot look like the mark.
     */
    private static String formatAppointment(Appointment appointment, LocalDateTime now) {
        String text = appointment.toString();
        if (!appointment.isBefore(now)) {
            return text;
        }
        String dateTime = appointment.getDate() + " " + appointment.getTime();
        assert text.startsWith(dateTime) : "an appointment's text must start with its date and time";
        return dateTime + PAST_MARK + text.substring(dateTime.length());
    }

    /**
     * Returns the full record of {@code patient}: the name and basic details, then each care list, the contacts
     * and the appointments under their headings, with a blank line before each heading.
     * Appointments are numbered as in the detail panel: upcoming ones (at or after {@code now}) first,
     * then past ones, each marked with {@code PAST_MARK} after its date and time.
     *
     * @param patient Patient whose record is formatted.
     * @param now Date and time that decides which appointments are upcoming.
     * @return the lines of the record, without line separators.
     */
    public static List<String> formatRecord(Patient patient, LocalDateTime now) {
        requireAllNonNull(patient, now);
        List<String> lines = new ArrayList<>();
        lines.add(patient.getName().fullName);
        lines.addAll(formatDetails(patient));

        for (Map.Entry<String, List<String>> section : createSections(patient, now).entrySet()) {
            lines.add("");
            lines.add(section.getKey());
            lines.addAll(formatEntries(section.getValue()));
        }
        return lines;
    }

    /**
     * Returns the entries of each numbered list in the record of {@code patient}, keyed by the list's heading, in the
     * order the record shows them. Appointments are ordered and marked as past at {@code now}.
     */
    private static Map<String, List<String>> createSections(Patient patient, LocalDateTime now) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put(HEADING_MEDICATIONS, patient.getMedications().stream().map(Medication::toString).toList());
        sections.put(HEADING_ALLERGENS, patient.getAllergens());
        sections.put(HEADING_CARE_INSTRUCTIONS, patient.getCareInstructions());
        sections.put(HEADING_CONTACTS, patient.getContacts().stream().map(Contact::toString).toList());
        sections.put(HEADING_APPOINTMENTS, formatAppointments(patient.getAppointmentsUpcomingFirst(now), now));
        return sections;
    }
}
