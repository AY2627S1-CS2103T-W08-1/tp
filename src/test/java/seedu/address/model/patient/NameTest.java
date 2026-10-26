package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphabetic characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphabetic characters
        assertFalse(Name.isValidName("12345")); // numbers only
        assertFalse(Name.isValidName("peter the 2nd")); // contains numbers
        assertFalse(Name.isValidName("Alex2")); // ends with a number
        assertFalse(Name.isValidName("-Alex")); // does not start with a letter
        assertFalse(Name.isValidName("/Alex")); // starts with a forward slash
        assertFalse(Name.isValidName(".Alex")); // starts with a period
        assertFalse(Name.isValidName("Ben -- // ..")); // symbols not next to letters
        assertFalse(Name.isValidName("Mary -Jane")); // hyphen after a space
        assertFalse(Name.isValidName("Tan .")); // period not after a letter
        assertFalse(Name.isValidName("s/o")); // only a joining word
        assertFalse(Name.isValidName("Tan s/o")); // ends with a joining word
        assertFalse(Name.isValidName("C/O Tan")); // starts with a joining word, in capitals
        assertFalse(Name.isValidName("Muthu a/l.")); // ends with a joining word and a period

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("Tan Ah Kow s/o Lim")); // with forward slash
        assertTrue(Name.isValidName("J. Doe")); // with period
        assertTrue(Name.isValidName("Tan Ah Kow Jr.")); // ends with a period
        assertTrue(Name.isValidName("O'Brien")); // with apostrophe
        assertTrue(Name.isValidName("Mary-Jane")); // with hyphen
        assertTrue(Name.isValidName("A.B. Tan")); // initials
        assertTrue(Name.isValidName("D'Angelo-Smith")); // apostrophe and hyphen
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr")); // long names
        assertTrue(Name.isValidName("Zoë José")); // accented letters
        assertTrue(Name.isValidName("Zoë")); // accent typed as a separate mark
        assertTrue(Name.isValidName("陈大文")); // Chinese letters
        assertTrue(Name.isValidName("Muthu a/l Ramasamy")); // joining word between two names
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
