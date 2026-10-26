package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Patient's address in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address {

    public static final int MAX_LENGTH = 200;
    public static final String MESSAGE_CONSTRAINTS = "Addresses cannot be blank.";
    public static final String MESSAGE_TOO_LONG = "Addresses cannot exceed " + MAX_LENGTH + " characters.";

    /*
     * The first character of the address must not be a whitespace, including Unicode whitespace such as
     * U+2003, otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "(?U)[^\\s].*";

    public final String value;

    /**
     * Constructs an {@code Address}.
     *
     * @param address A valid address.
     */
    public Address(String address) {
        requireNonNull(address);
        checkArgument(address.matches(VALIDATION_REGEX), MESSAGE_CONSTRAINTS);
        checkArgument(isWithinMaxLength(address), MESSAGE_TOO_LONG);
        value = address;
    }

    /**
     * Returns true if a given string is a valid address.
     */
    public static boolean isValidAddress(String test) {
        return test.matches(VALIDATION_REGEX) && isWithinMaxLength(test);
    }

    /**
     * Returns true if {@code test} does not exceed the maximum address length.
     */
    public static boolean isWithinMaxLength(String test) {
        return test.codePointCount(0, test.length()) <= MAX_LENGTH;
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
        if (!(other instanceof Address otherAddress)) {
            return false;
        }

        return value.equals(otherAddress.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
