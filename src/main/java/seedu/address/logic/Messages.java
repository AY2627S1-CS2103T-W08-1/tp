package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.patient.Patient;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_PREFIX =
            "Unrecognized parameter: %1$s. Please check the command format.";
    public static final String MESSAGE_PUNCTUATED_SLASH_TOKEN =
            "Unrecognized word: '%1$s'. Write %2$s as a separate word without punctuation.";
    public static final String MESSAGE_INVALID_CARE_INSTRUCTION_INDEX =
            "The care instruction index provided is invalid.";
    public static final String MESSAGE_INVALID_ALLERGEN_INDEX = "The allergen index provided is invalid.";
    public static final String MESSAGE_INVALID_CONTACT_INDEX = "The contact index provided is invalid.";
    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX = "The patient index provided is invalid.";
    public static final String MESSAGE_NOTHING_DUE_TODAY = "No medications are due today.";
    public static final String MESSAGE_NOTHING_DUE_TODAY_FOR_PATIENT = "No medications are due today for %1$s.";
    public static final String MESSAGE_DUE_ENTRY = "%1$s %2$s: %3$s";
    public static final String MESSAGE_DUE_EARLIER_TODAY_SUFFIX = " (earlier today)";
    public static final String MESSAGE_PATIENTS_LISTED_OVERVIEW = "%1$d patient(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for a single-valued field: ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code patient} for display to the user.
     */
    public static String format(Patient patient) {
        final StringBuilder builder = new StringBuilder();
        builder.append(patient.getName())
                .append("; NRIC: ").append(patient.getNric())
                .append("; DOB: ").append(patient.getDob())
                .append("; Phone: ").append(patient.getPhone())
                .append("; Address: ").append(patient.getAddress());
        return builder.toString();
    }

}
