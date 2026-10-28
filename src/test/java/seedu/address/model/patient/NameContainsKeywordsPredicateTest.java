package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PatientBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different patient -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PatientBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PatientBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PatientBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PatientBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PatientBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PatientBuilder().withName("Alice Bob").build()));

        // Keywords match phone, NRIC and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("91234567", "S1234567A", "Main", "Street"));
        assertFalse(predicate.test(new PatientBuilder().withName("Alice").withPhone("91234567")
                .withNric("S1234567A").withAddress("Main Street").build()));
    }

    @Test
    public void test_hyphenApostropheOrDotInName_eachPartMatches() {
        Patient maryJane = new PatientBuilder().withName("Mary-Jane Tan").build();
        Patient maryObrien = new PatientBuilder().withName("Mary O'Brien").build();
        Patient maryLim = new PatientBuilder().withName("Mary Lim").build();
        Patient kowJr = new PatientBuilder().withName("Tan Ah Kow Jr.").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("Mary")).test(maryJane));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Jane")).test(maryJane));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Mary-Jane")).test(maryJane));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Brien")).test(maryObrien));
        assertTrue(new NameContainsKeywordsPredicate(List.of("O'Brien")).test(maryObrien));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Jr")).test(kowJr));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Jr.")).test(kowJr));

        // keyword with several parts needs all the parts, next to each other
        assertFalse(new NameContainsKeywordsPredicate(List.of("Mary-Jane")).test(maryLim));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Mary-Jane")).test(maryObrien));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Jane")).test(maryLim));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Bri")).test(maryObrien));
    }

    @Test
    public void test_keywordOfOnlySeparators_returnsFalse() {
        Patient patient = new PatientBuilder().withName("Mary-Jane Tan").build();
        assertFalse(new NameContainsKeywordsPredicate(List.of("-")).test(patient));
        assertFalse(new NameContainsKeywordsPredicate(List.of("'.")).test(patient));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
