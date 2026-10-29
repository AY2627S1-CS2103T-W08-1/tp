package seedu.address.model.appointment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public void constructor_dateBeforeEarliestDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Appointment.MESSAGE_DATE_CONSTRAINTS, ()
            -> new Appointment(LocalDate.of(1899, 12, 31), TIME, "Dr Tan", ""));
    }

    @Test
    public void constructor_timeWithSeconds_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Appointment.MESSAGE_TIME_CONSTRAINTS, ()
            -> new Appointment(DATE, LocalTime.of(9, 30, 15), "Dr Tan", ""));
    }

    @Test
    public void isBefore() {
        Appointment appointment = new Appointment(DATE, TIME, "Dr Tan", "");
        assertTrue(appointment.isBefore(LocalDateTime.of(DATE, TIME.plusMinutes(1))));
        assertFalse(appointment.isBefore(LocalDateTime.of(DATE, TIME))); // same time
        assertFalse(appointment.isBefore(LocalDateTime.of(DATE, TIME).plusSeconds(30))); // same minute
        assertFalse(appointment.isBefore(LocalDateTime.of(DATE, TIME.minusMinutes(1))));
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
        assertFalse(Appointment.isValidDate("0001-01-01")); // typing mistake for a recent year
        assertFalse(Appointment.isValidDate("1899-12-31")); // day before the earliest date

        // valid dates
        assertTrue(Appointment.isValidDate("2026-11-05"));
        assertTrue(Appointment.isValidDate("2020-01-01")); // past date
        assertTrue(Appointment.isValidDate("1900-01-01")); // earliest date
        assertTrue(Appointment.isValidDate("9999-12-31")); // latest four-digit date
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
        assertFalse(Appointment.isValidProvider(" ")); // no-break space only
        assertFalse(Appointment.isValidProvider("Dr Tan\nDr Fake")); // line break
        assertFalse(Appointment.isValidProvider("a".repeat(Appointment.MAX_PROVIDER_LENGTH + 1))); // too long
        assertTrue(Appointment.isValidProvider("Dr Tan"));
        assertTrue(Appointment.isValidProvider("a".repeat(Appointment.MAX_PROVIDER_LENGTH))); // maximum length
    }

    @Test
    public void isValidNote() {
        assertFalse(Appointment.isValidNote("Bring results)\n2. 2099-01-01 09:00 with Dr Fake")); // line break
        assertFalse(Appointment.isValidNote("Bring results")); // Unicode line separator
        assertFalse(Appointment.isValidNote("a".repeat(Appointment.MAX_NOTE_LENGTH + 1))); // too long
        assertTrue(Appointment.isValidNote("")); // no note
        assertTrue(Appointment.isValidNote("a".repeat(Appointment.MAX_NOTE_LENGTH))); // maximum length
    }

    @Test
    public void constructor_noteWithLineBreak_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Appointment.MESSAGE_NOTE_CONSTRAINTS, ()
            -> new Appointment(DATE, TIME, "Dr Tan", "Fasting\nFake line"));
    }

    @Test
    public void isAtSameTimeAs() {
        Appointment appointment = new Appointment(DATE, TIME, "Dr Tan", "");

        // same date and time, different provider -> returns true
        assertTrue(appointment.isAtSameTimeAs(new Appointment(DATE, TIME, "Dr Lim", "")));

        // null -> returns false
        assertFalse(appointment.isAtSameTimeAs(null));

        // different date or time -> returns false
        assertFalse(appointment.isAtSameTimeAs(new Appointment(DATE.plusDays(1), TIME, "Dr Tan", "")));
        assertFalse(appointment.isAtSameTimeAs(new Appointment(DATE, TIME.plusMinutes(1), "Dr Tan", "")));
    }

    @Test
    public void isSameAppointment() {
        Appointment appointment = new Appointment(DATE, TIME, "Dr Tan", "");

        // same provider, ignoring case, spaces at either end, repeated spaces and note -> returns true
        assertTrue(appointment.isSameAppointment(new Appointment(DATE, TIME, "dr  TAN", "Fasting")));
        assertTrue(appointment.isSameAppointment(new Appointment(DATE, TIME, " Dr Tan ", "")));
        assertTrue(new Appointment(DATE, TIME, "  dr  tan ", "").isSameAppointment(appointment));
        assertTrue(appointment.isSameAppointment(new Appointment(DATE, TIME, "Dr Tan", ""))); // no-break space

        // null -> returns false
        assertFalse(appointment.isSameAppointment(null));

        // different provider or time -> returns false
        assertFalse(appointment.isSameAppointment(new Appointment(DATE, TIME, "Dr Lim", "")));
        assertFalse(appointment.isSameAppointment(new Appointment(DATE, TIME.plusMinutes(1), "Dr Tan", "")));
    }

    @Test
    public void isSameAppointment_canonicallyEquivalentProviders_returnsTrue() {
        // "é" as one character, and as "e" followed by a combining accent, look the same
        Appointment composed = new Appointment(DATE, TIME, "Caf\u00e9 clinic", "");
        Appointment decomposed = new Appointment(DATE, TIME, "CAFE\u0301 clinic", "");
        assertTrue(composed.isSameAppointment(decomposed));
        assertTrue(decomposed.isSameAppointment(composed));
        assertEquals("CAFE\u0301 clinic", decomposed.getProvider()); // the spelling typed is kept

        // accents still count, and fullwidth letters are not canonically equivalent, as for contact names
        assertFalse(composed.isSameAppointment(new Appointment(DATE, TIME, "Cafe clinic", "")));
        assertFalse(new Appointment(DATE, TIME, "Clinic", "")
                .isSameAppointment(new Appointment(DATE, TIME, "\uff23linic", "")));
    }

    @Test
    public void getDateTime() {
        assertEquals(LocalDateTime.of(DATE, TIME), new Appointment(DATE, TIME, "Dr Tan", "").getDateTime());
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

    @Test
    public void getDateTimeTextAndDetailsText() {
        Appointment appointment = new Appointment(DATE, TIME, "Dr Tan", "Fasting");
        assertEquals("2026-11-05 09:30", appointment.getDateTimeText());
        assertEquals("with Dr Tan (Fasting)", appointment.getDetailsText());
        assertEquals("with Dr Tan", new Appointment(DATE, TIME, "Dr Tan", "").getDetailsText());
    }
}
