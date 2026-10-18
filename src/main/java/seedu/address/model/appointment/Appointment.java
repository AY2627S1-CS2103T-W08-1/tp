package seedu.address.model.appointment;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Represents an appointment of a Patient in MediConnect.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Appointment implements Comparable<Appointment> {

    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Appointment date should be a real date in YYYY-MM-DD format, e.g. 2026-10-25.";
    public static final String MESSAGE_TIME_CONSTRAINTS =
            "Appointment time should be in HH:MM 24-hour format, e.g. 14:30.";
    public static final String MESSAGE_PROVIDER_CONSTRAINTS =
            "Who the appointment is with should not be blank.";

    /*
     * Four digits for the year, so that "+10000-01-01" and "-0001-01-01" are rejected.
     */
    public static final String DATE_VALIDATION_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    /*
     * Two digits for the hour and two for the minute, so that "9:30" and "14:30:15" are rejected.
     */
    public static final String TIME_VALIDATION_REGEX = "\\d{2}:\\d{2}";

    private final LocalDate date;
    private final LocalTime time;
    private final String provider;
    private final String note;

    /**
     * Every field must be present and not null. An empty {@code note} means the appointment has no note.
     */
    public Appointment(LocalDate date, LocalTime time, String provider, String note) {
        requireAllNonNull(date, time, provider, note);
        checkArgument(isValidProvider(provider), MESSAGE_PROVIDER_CONSTRAINTS);
        this.date = date;
        this.time = time;
        this.provider = provider;
        this.note = note;
    }

    /**
     * Returns true if a given string is a real date in YYYY-MM-DD format.
     */
    public static boolean isValidDate(String test) {
        if (!test.matches(DATE_VALIDATION_REGEX)) {
            return false;
        }

        try {
            LocalDate.parse(test);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if a given string is a real time in HH:MM 24-hour format.
     */
    public static boolean isValidTime(String test) {
        if (!test.matches(TIME_VALIDATION_REGEX)) {
            return false;
        }

        try {
            LocalTime.parse(test);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if a given string is a valid provider, i.e. who the appointment is with.
     */
    public static boolean isValidProvider(String test) {
        return !test.isBlank();
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getProvider() {
        return provider;
    }

    public String getNote() {
        return note;
    }

    /**
     * Returns the date and time of the appointment together.
     */
    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time);
    }

    /**
     * Returns true if both appointments are at the same date and time, whoever they are with.
     */
    public boolean isAtSameTimeAs(Appointment otherAppointment) {
        return otherAppointment != null
                && date.equals(otherAppointment.date)
                && time.equals(otherAppointment.time);
    }

    /**
     * Returns true if both appointments are at the same date and time with the same provider,
     * ignoring letter case and repeated spaces in the provider. Notes are not compared.
     * This defines a weaker notion of equality between two appointments.
     */
    public boolean isSameAppointment(Appointment otherAppointment) {
        return isAtSameTimeAs(otherAppointment)
                && provider.replaceAll("\\s+", " ")
                        .equalsIgnoreCase(otherAppointment.provider.replaceAll("\\s+", " "));
    }

    /**
     * Orders appointments by date, then time.
     * Two different appointments at the same date and time compare as equal in this order.
     */
    @Override
    public int compareTo(Appointment other) {
        int dateComparison = date.compareTo(other.date);
        if (dateComparison != 0) {
            return dateComparison;
        }

        return time.compareTo(other.time);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Appointment otherAppointment)) {
            return false;
        }

        return date.equals(otherAppointment.date)
                && time.equals(otherAppointment.time)
                && provider.equals(otherAppointment.provider)
                && note.equals(otherAppointment.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, provider, note);
    }

    /**
     * Returns the appointment as shown to the user, e.g. "2026-10-25 14:30 with Dr Tan (Fasting)".
     */
    @Override
    public String toString() {
        String appointment = date + " " + time + " with " + provider;
        return note.isEmpty() ? appointment : appointment + " (" + note + ")";
    }
}
