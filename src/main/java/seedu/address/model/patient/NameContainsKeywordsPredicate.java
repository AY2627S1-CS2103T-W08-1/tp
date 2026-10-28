package seedu.address.model.patient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Patient}'s {@code Name} matches any of the keywords given.
 * A name and a keyword are split into parts at spaces, hyphens, apostrophes and dots, ignoring letter case.
 * A keyword matches if its parts are next to each other in the name, e.g., {@code Mary-Jane} matches
 * {@code Mary-Jane Tan} and {@code Jane} matches it too.
 */
public class NameContainsKeywordsPredicate implements Predicate<Patient> {
    private static final Pattern NAME_PART_SEPARATORS = Pattern.compile("[\\s\\-'.]+");

    private final List<String> keywords;

    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Patient patient) {
        List<String> nameParts = toParts(patient.getName().fullName);
        return keywords.stream()
                .map(NameContainsKeywordsPredicate::toParts)
                .anyMatch(keywordParts -> !keywordParts.isEmpty()
                        && Collections.indexOfSubList(nameParts, keywordParts) >= 0);
    }

    /**
     * Returns the lower-case parts of {@code text}, which are separated by spaces, hyphens, apostrophes or dots.
     */
    private static List<String> toParts(String text) {
        return Arrays.stream(NAME_PART_SEPARATORS.split(text.toLowerCase(Locale.ROOT)))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
