package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
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
    public static final String MESSAGE_INVALID_MEDICATION_DISPLAYED_INDEX =
            "The medication index provided is invalid.";
    public static final String MESSAGE_NO_MEDICATION_FIELD_TO_EDIT =
            "At least one of med/ or t/ must be provided.";
    public static final String MESSAGE_CLEAR_TIMES_CANNOT_BE_COMBINED =
            "t/ with no time clears all times and cannot be combined with other times.";
    public static final String MESSAGE_DUPLICATE_MEDICATION_DESCRIPTION =
            "Medication \"%1$s\" was not updated: it already exists for this patient.";
    public static final String MESSAGE_EDIT_MEDICATION_SUCCESS = "Updated medication %1$d of %2$s: %3$s";
    public static final String MESSAGE_PATIENTS_LISTED_OVERVIEW = "%1$d patient(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for a single-valued field: ";

    /** Reason given by {@code describeWriteFailure} when MediConnect is not allowed to write to a folder. */
    public static final String MESSAGE_NO_WRITE_PERMISSION = "MediConnect does not have permission to write there";

    /** Reason given by {@code describeWriteFailure} when the file system does not give a reason of its own. */
    public static final String MESSAGE_FILE_SYSTEM_ERROR = "the folder or file could not be created";

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

    /**
     * Returns a short, readable reason for {@code ioe}, a failure to write a file, suitable for showing to the user.
     * File system exceptions use the file path as their message, so their reason is used instead.
     * Trailing full stops are removed, as the messages that show the reason add their own.
     *
     * @param ioe Exception thrown while writing the file.
     * @return the reason, without a full stop at the end.
     */
    public static String describeWriteFailure(IOException ioe) {
        requireNonNull(ioe);
        if (ioe instanceof AccessDeniedException) {
            return MESSAGE_NO_WRITE_PERMISSION;
        }

        String reason;
        String fallback;
        if (ioe instanceof FileSystemException fileSystemException) {
            reason = fileSystemException.getReason();
            fallback = MESSAGE_FILE_SYSTEM_ERROR;
        } else {
            reason = ioe.getMessage();
            fallback = ioe.getClass().getSimpleName();
        }

        String trimmedReason = reason == null ? "" : stripTrailingSpacesAndFullStops(reason).strip();
        return trimmedReason.isEmpty() ? fallback : trimmedReason;
    }

    /**
     * Returns {@code text} without the spaces and full stops at its end.
     * Checks one character at a time from the end, so a long run of spaces inside {@code text} is not
     * checked again and again, as a regular expression would.
     */
    private static String stripTrailingSpacesAndFullStops(String text) {
        int end = text.length();
        while (end > 0 && (Character.isWhitespace(text.charAt(end - 1)) || text.charAt(end - 1) == '.')) {
            end--;
        }
        return text.substring(0, end);
    }

}
