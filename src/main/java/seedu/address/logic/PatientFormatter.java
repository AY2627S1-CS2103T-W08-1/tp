package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;

/**
 * Formats a patient's details and care lists as lines of text, worded the same as the patient detail panel.
 */
public class PatientFormatter {

    /** Shown in place of the entries of an empty care list. */
    public static final String NONE_RECORDED = "None recorded";

    /** Heading of the list of medications. */
    public static final String HEADING_MEDICATIONS = "Medications";

    /** Heading of the list of allergens. */
    public static final String HEADING_ALLERGENS = "Allergens";

    /** Heading of the list of care instructions. */
    public static final String HEADING_CARE_INSTRUCTIONS = "Care Instructions";

    /** A line break inside an entry, with any spaces around it. */
    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\s*\\R\\s*");

    private PatientFormatter() {} // prevents instantiation

    /**
     * Returns the basic details of {@code patient} other than the name, one labeled line each,
     * e.g., "NRIC: S1234567A".
     *
     * @param patient Patient whose details are formatted.
     * @return the NRIC, DOB, phone and address lines, in the order the detail panel shows them.
     */
    public static List<String> formatDetails(Patient patient) {
        requireNonNull(patient);
        return List.of("NRIC: " + patient.getNric(), "DOB: " + patient.getDob(),
                "Phone: " + patient.getPhone(), "Address: " + patient.getAddress());
    }

    /**
     * Returns one numbered line per entry in {@code entries}, e.g., "1. Metformin 500mg",
     * or a single {@code NONE_RECORDED} line if there are no entries.
     * A line break inside an entry, which only a hand-edited data file can hold, becomes a single space,
     * so that each entry stays on one numbered line.
     *
     * @param entries Entries of one care list, in the order they were added.
     * @return the lines to show for the care list.
     */
    public static List<String> formatEntries(List<String> entries) {
        requireNonNull(entries);
        if (entries.isEmpty()) {
            return List.of(NONE_RECORDED);
        }

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String entry = LINE_BREAK_PATTERN.matcher(entries.get(i)).replaceAll(" ").strip();
            lines.add((i + 1) + ". " + entry);
        }
        return lines;
    }

    /**
     * Returns the full record of {@code patient}: the name and basic details, then each care list under its
     * heading, with a blank line before each heading.
     *
     * @param patient Patient whose record is formatted.
     * @return the lines of the record, without line separators.
     */
    public static List<String> formatRecord(Patient patient) {
        requireNonNull(patient);
        List<String> lines = new ArrayList<>();
        lines.add(patient.getName().fullName);
        lines.addAll(formatDetails(patient));

        for (Map.Entry<String, List<String>> careList : List.of(
                Map.entry(HEADING_MEDICATIONS, patient.getMedications().stream().map(Medication::toString).toList()),
                Map.entry(HEADING_ALLERGENS, patient.getAllergens()),
                Map.entry(HEADING_CARE_INSTRUCTIONS, patient.getCareInstructions()))) {
            lines.add("");
            lines.add(careList.getKey());
            lines.addAll(formatEntries(careList.getValue()));
        }
        return lines;
    }
}
