package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DobTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Dob(null));
    }

    @Test
    public void constructor_invalidDob_throwsIllegalArgumentException() {
        String invalidDob = "";
        assertThrows(IllegalArgumentException.class, () -> new Dob(invalidDob));
    }

    @Test
    public void isValidDob() {
        // null dob
        assertThrows(NullPointerException.class, () -> Dob.isValidDob(null));

        // invalid dobs
        assertFalse(Dob.isValidDob("")); // empty string
        assertFalse(Dob.isValidDob("2021-02-30")); // day does not exist
        assertFalse(Dob.isValidDob("2023-02-29")); // not a leap year
        assertFalse(Dob.isValidDob("12-04-1950")); // wrong order
        assertFalse(Dob.isValidDob("1950/04/12")); // wrong separator
        assertFalse(Dob.isValidDob(LocalDate.now().plusDays(1).toString())); // future date

        // valid dobs
        assertTrue(Dob.isValidDob("1950-04-12"));
        assertTrue(Dob.isValidDob("2024-02-29")); // leap year
        assertTrue(Dob.isValidDob(LocalDate.now().toString())); // today
    }

    @Test
    public void equals() {
        Dob dob = new Dob("1950-04-12");

        // same values -> returns true
        assertTrue(dob.equals(new Dob("1950-04-12")));

        // same object -> returns true
        assertTrue(dob.equals(dob));

        // null -> returns false
        assertFalse(dob.equals(null));

        // different types -> returns false
        assertFalse(dob.equals(5.0f));

        // different values -> returns false
        assertFalse(dob.equals(new Dob("1945-11-02")));
    }
}
