package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class AppointmentTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 5);
    private static final LocalTime TIME = LocalTime.of(9, 30);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Appointment(null, TIME, "Dr Tan", ""));
        assertThrows(NullPointerException.class, () -> new Appointment(DATE, TIME, "Dr Tan", null));
    }

    @Test
    public void constructor_blankProvider_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Appointment(DATE, TIME, " ", ""));
    }

    @Test
    public void isValidDate() {
        // invalid dates
        assertFalse(Appointment.isValidDate("")); // empty string
        assertFalse(Appointment.isValidDate("2026-02-30")); // day does not exist
        assertFalse(Appointment.isValidDate("05-11-2026")); // wrong order
        assertFalse(Appointment.isValidDate("2026/11/05")); // wrong separator
        assertFalse(Appointment.isValidDate("+10000-01-01")); // year with more than 4 digits
        assertFalse(Appointment.isValidDate("-0001-01-01")); // negative year

        // valid dates
        assertTrue(Appointment.isValidDate("2026-11-05"));
        assertTrue(Appointment.isValidDate("2020-01-01")); // past date
    }

    @Test
    public void isValidTime() {
        // invalid times
        assertFalse(Appointment.isValidTime("")); // empty string
        assertFalse(Appointment.isValidTime("9:30")); // one-digit hour
        assertFalse(Appointment.isValidTime("14:30:15")); // seconds given
        assertFalse(Appointment.isValidTime("24:00")); // hour out of range
        assertFalse(Appointment.isValidTime("14:60")); // minute out of range

        // valid times
        assertTrue(Appointment.isValidTime("00:00"));
        assertTrue(Appointment.isValidTime("23:59"));
    }

    @Test
    public void isValidProvider() {
        assertFalse(Appointment.isValidProvider("")); // empty string
        assertFalse(Appointment.isValidProvider("   ")); // spaces only
        assertTrue(Appointment.isValidProvider("Dr Tan"));
    }

    @Test
    public void equals() {
        Appointment appointment = new Appointment(DATE, TIME, "Dr Tan", "");

        // same values -> returns true
        assertTrue(appointment.equals(new Appointment(DATE, TIME, "Dr Tan", "")));

        // same object -> returns true
        assertTrue(appointment.equals(appointment));

        // null -> returns false
        assertFalse(appointment.equals(null));

        // different types -> returns false
        assertFalse(appointment.equals(5.0f));

        // different values -> returns false
        assertFalse(appointment.equals(new Appointment(DATE.plusDays(1), TIME, "Dr Tan", "")));
        assertFalse(appointment.equals(new Appointment(DATE, TIME.plusHours(1), "Dr Tan", "")));
        assertFalse(appointment.equals(new Appointment(DATE, TIME, "Dr Lim", "")));
        assertFalse(appointment.equals(new Appointment(DATE, TIME, "Dr Tan", "Fasting")));
    }

    @Test
    public void toStringMethod() {
        assertEquals("2026-11-05 09:30 with Dr Tan", new Appointment(DATE, TIME, "Dr Tan", "").toString());
        assertEquals("2026-11-05 09:30 with Dr Tan (Fasting)",
                new Appointment(DATE, TIME, "Dr Tan", "Fasting").toString());
    }
}
