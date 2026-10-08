package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Patient's NRIC in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidNric(String)}
 */
public class Nric {

    public static final String MESSAGE_CONSTRAINTS =
            "NRIC should be in the format: 1 letter, 7 digits, 1 letter (e.g., S1234567A).";
    public static final String VALIDATION_REGEX = "[STFGMstfgm]\\d{7}[A-Za-z]";
    public final String value;

    /**
     * Constructs a {@code Nric}.
     *
     * @param nric A valid NRIC.
     */
    public Nric(String nric) {
        requireNonNull(nric);
        checkArgument(isValidNric(nric), MESSAGE_CONSTRAINTS);
        value = nric.toUpperCase();
    }

    /**
     * Returns true if a given string is a valid NRIC.
     */
    public static boolean isValidNric(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Nric otherNric)) {
            return false;
        }

        return value.equals(otherNric.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
