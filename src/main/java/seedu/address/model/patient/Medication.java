package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a medication of a patient: a description and the times of day it is taken.
 * Guarantees: immutable; description is valid with normalized whitespace; times are sorted ascending,
 * without duplicates and truncated to whole minutes.
 */
public class Medication {

    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final String MESSAGE_DESCRIPTION_CONSTRAINTS = "Medication description cannot be blank.";
    public static final String MESSAGE_DESCRIPTION_TOO_LONG =
            "Medication descriptions cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.";
    public static final String MESSAGE_TIME_CONSTRAINTS = "Time must be in HH:MM (24-hour), e.g. 08:30.";

    private static final String TIME_PATTERN = "HH:mm";
    private static final String TIME_REGEX = "\\d{2}:\\d{2}";
    private static final String TIMES_PREFIX = " (";
    private static final String TIMES_SUFFIX = ")";
    private static final String TIMES_SEPARATOR = ", ";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT);

    private final String description;
    private final List<LocalTime> times;

    /**
     * Creates a medication with the given {@code description} and no times.
     *
     * @throws IllegalArgumentException if {@code description} is blank.
     */
    public Medication(String description) {
        this(description, List.of());
    }

    /**
     * Creates a medication with the given {@code description} and {@code times}.
     * Times are sorted, deduplicated and truncated to whole minutes.
     *
     * @throws IllegalArgumentException if {@code description} is blank.
     */
    public Medication(String description, Collection<LocalTime> times) {
        requireAllNonNull(description, times);
        requireAllNonNull(times);
        if (!isValidDescription(description)) {
            throw new IllegalArgumentException(MESSAGE_DESCRIPTION_CONSTRAINTS);
        }
        this.description = normalizeDescription(description);
        this.times = times.stream()
                .map(time -> time.truncatedTo(ChronoUnit.MINUTES))
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns true if {@code description} contains at least one non-whitespace character.
     * Unicode whitespace, such as a no-break space, counts as whitespace.
     */
    public static boolean isValidDescription(String description) {
        requireNonNull(description);
        return !normalizeDescription(description).isEmpty();
    }

    /**
     * Returns {@code description} with every run of whitespace, including Unicode whitespace and line breaks,
     * replaced by one space, and without leading and trailing whitespace.
     */
    public static String normalizeDescription(String description) {
        requireNonNull(description);
        return description.replaceAll("(?U)\\s+", " ").trim();
    }

    /**
     * Returns true if {@code description} does not exceed the maximum medication-description length.
     */
    public static boolean isDescriptionWithinMaxLength(String description) {
        requireNonNull(description);
        return description.codePointCount(0, description.length()) <= MAX_DESCRIPTION_LENGTH;
    }

    /**
     * Returns true if {@code time} is a 24-hour time of the form HH:MM with two digits each.
     */
    public static boolean isValidTime(String time) {
        requireNonNull(time);
        if (!time.matches(TIME_REGEX)) {
            return false;
        }
        try {
            LocalTime.parse(time, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Converts a {@code time} that satisfies {@link #isValidTime(String)} into a {@code LocalTime}.
     */
    public static LocalTime toTime(String time) {
        requireNonNull(time);
        assert isValidTime(time) : "time must be validated before conversion";
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    /**
     * Returns {@code time} formatted as HH:MM.
     */
    public static String formatTime(LocalTime time) {
        requireNonNull(time);
        return time.format(TIME_FORMATTER);
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns an unmodifiable list of the times, sorted ascending.
     */
    public List<LocalTime> getTimes() {
        return times;
    }

    /**
     * Returns true if both medications have the same description, ignoring case.
     * This defines a weaker notion of equality used to detect duplicates; times are not compared.
     */
    public boolean isSameMedication(Medication otherMedication) {
        return otherMedication == this
                || (otherMedication != null && otherMedication.description.equalsIgnoreCase(description));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Medication otherMedication)) {
            return false;
        }

        return description.equals(otherMedication.description) && times.equals(otherMedication.times);
    }

    @Override
    public int hashCode() {
        return 31 * description.hashCode() + times.hashCode();
    }

    /**
     * Returns the description followed by the times in brackets, e.g. {@code Metformin 500mg (08:00, 20:00)}.
     * The brackets are omitted if there are no times.
     */
    @Override
    public String toString() {
        if (times.isEmpty()) {
            return description;
        }
        return description + TIMES_PREFIX
                + times.stream().map(Medication::formatTime).collect(Collectors.joining(TIMES_SEPARATOR))
                + TIMES_SUFFIX;
    }
}
