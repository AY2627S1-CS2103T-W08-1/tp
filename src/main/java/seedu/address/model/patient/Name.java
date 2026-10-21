package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Patient's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should be words of letters separated by single spaces. An apostrophe ('), hyphen (-) or "
            + "slash (/) can join two letters, e.g., O'Brien, Mary-Jane or s/o, and a period (.) can follow "
            + "a letter, e.g., Jr.";

    /*
     * A name is one or more words separated by single spaces. Each word is made of letters,
     * where an apostrophe, hyphen, slash or period may join two groups of letters,
     * and may end with a period, e.g. "O'Brien", "Mary-Jane", "s/o" and "Jr.".
     */
    private static final String WORD_REGEX = "\\p{Alpha}+(['/.-]\\p{Alpha}+)*\\.?";
    public static final String VALIDATION_REGEX = WORD_REGEX + "( " + WORD_REGEX + ")*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
