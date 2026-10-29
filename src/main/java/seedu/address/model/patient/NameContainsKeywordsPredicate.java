package seedu.address.model.patient;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Patient}'s {@code Name} matches any of the keywords given.
 * A name and a keyword are normalized with {@link StringUtil#normalizeEntry(String)}, so that Unicode spaces,
 * such as a no-break space, count as spaces and accented letters written with combining marks match the same
 * letters written as one character. They are then split into parts at spaces, hyphens, apostrophes (straight,
 * curly or a modifier letter apostrophe) and dots, ignoring letter case. A keyword matches if its parts are next
 * to each other in the name, e.g., {@code Mary-Jane} matches {@code Mary-Jane Tan} and {@code Jane} matches it
 * too.
 */
public class NameContainsKeywordsPredicate implements Predicate<Patient> {
    private static final Pattern NAME_PART_SEPARATORS = Pattern.compile("[\\s\\-'\u2018\u2019\u02bc.]+");

    private final List<String> keywords;

    /**
     * Creates a predicate that tests the name of a patient against {@code keywords}, none of which is null.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);
        CollectionUtil.requireAllNonNull(keywords);
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
     * The text is normalized first, so that Unicode spaces are spaces and accents are in one form.
     */
    private static List<String> toParts(String text) {
        String normalizedText = StringUtil.normalizeEntry(text).toLowerCase(Locale.ROOT);
        return Arrays.stream(NAME_PART_SEPARATORS.split(normalizedText))
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
