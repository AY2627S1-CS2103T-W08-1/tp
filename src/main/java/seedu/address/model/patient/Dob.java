package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Represents a Patient's date of birth in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidDob(String)}
 */
public class Dob {

    public static final String MESSAGE_CONSTRAINTS =
            "Date of birth should be a valid date in YYYY-MM-DD format, and cannot be in the future.";
    public final LocalDate value;

    /**
     * Constructs a {@code Dob}.
     *
     * @param dob A valid date of birth.
     */
    public Dob(String dob) {
        requireNonNull(dob);
        checkArgument(isValidDob(dob), MESSAGE_CONSTRAINTS);
        value = LocalDate.parse(dob);
    }

    /**
     * Returns true if a given string is a valid date of birth.
     */
    public static boolean isValidDob(String test) {
        try {
            LocalDate date = LocalDate.parse(test);
            return !date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Dob otherDob)) {
            return false;
        }

        return value.equals(otherDob.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
