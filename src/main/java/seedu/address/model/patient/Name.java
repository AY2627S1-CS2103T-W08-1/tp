package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Set;

/**
 * Represents a Patient's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should be words of letters, in any language, separated by single spaces. An apostrophe ('), "
            + "hyphen (-) or slash (/) can join two letters, e.g., O'Brien, Mary-Jane or s/o, and a period (.) "
            + "can follow a letter, e.g., Jr. A name cannot start or end with s/o, d/o, c/o, a/l or a/p.";

    /*
     * A letter in any language, followed by any accents typed as separate marks, e.g. "e" followed by U+0308.
     */
    private static final String LETTER_REGEX = "\\p{L}\\p{M}*";

    /*
     * A name is one or more words separated by single spaces. Each word is made of letters,
     * where an apostrophe, hyphen, slash or period may join two groups of letters,
     * and may end with a period, e.g. "O'Brien", "Mary-Jane", "s/o" and "Jr.".
     */
    private static final String WORD_REGEX = "(" + LETTER_REGEX + ")+(['/.-](" + LETTER_REGEX + ")+)*\\.?";
    public static final String VALIDATION_REGEX = WORD_REGEX + "( " + WORD_REGEX + ")*";

    /*
     * Words that join two names, e.g. "Raj s/o Kumar", so they cannot start or end a name.
     */
    private static final Set<String> JOINING_WORDS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p");

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
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }

        String[] words = test.split(" ");
        return !isJoiningWord(words[0]) && !isJoiningWord(words[words.length - 1]);
    }

    /**
     * Returns true if {@code word} is a word such as "s/o" that joins two names, ignoring letter case and a
     * period after it.
     */
    private static boolean isJoiningWord(String word) {
        String wordWithoutPeriod = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
        return JOINING_WORDS.contains(wordWithoutPeriod.toLowerCase(Locale.ROOT));
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
