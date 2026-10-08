package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NricTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Nric(null));
    }

    @Test
    public void constructor_invalidNric_throwsIllegalArgumentException() {
        String invalidNric = "";
        assertThrows(IllegalArgumentException.class, () -> new Nric(invalidNric));
    }

    @Test
    public void constructor_lowercaseNric_storedInUppercase() {
        assertEquals("S1234567A", new Nric("s1234567a").value);
    }

    @Test
    public void isValidNric() {
        // null nric
        assertThrows(NullPointerException.class, () -> Nric.isValidNric(null));

        // invalid nrics
        assertFalse(Nric.isValidNric("")); // empty string
        assertFalse(Nric.isValidNric(" ")); // spaces only
        assertFalse(Nric.isValidNric("A1234567B")); // first letter not S, T, F, G or M
        assertFalse(Nric.isValidNric("S123456A")); // less than 7 digits
        assertFalse(Nric.isValidNric("S12345678A")); // more than 7 digits
        assertFalse(Nric.isValidNric("S1234567")); // missing last letter
        assertFalse(Nric.isValidNric("S1234 567A")); // spaces within nric

        // valid nrics
        assertTrue(Nric.isValidNric("S1234567A"));
        assertTrue(Nric.isValidNric("t7654321b")); // lowercase letters
    }

    @Test
    public void equals() {
        Nric nric = new Nric("S1234567A");

        // same values -> returns true
        assertTrue(nric.equals(new Nric("S1234567A")));

        // same values in different case -> returns true
        assertTrue(nric.equals(new Nric("s1234567a")));

        // same object -> returns true
        assertTrue(nric.equals(nric));

        // null -> returns false
        assertFalse(nric.equals(null));

        // different types -> returns false
        assertFalse(nric.equals(5.0f));

        // different values -> returns false
        assertFalse(nric.equals(new Nric("S7654321B")));
    }
}
