package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class MedicationTest {

    private static final String DESCRIPTION = "Metformin 500mg";
    private static final LocalTime MORNING = LocalTime.of(8, 0);
    private static final LocalTime EVENING = LocalTime.of(20, 0);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Medication(null));
        assertThrows(NullPointerException.class, () -> new Medication(DESCRIPTION, null));
        assertThrows(NullPointerException.class, () -> new Medication(DESCRIPTION, Arrays.asList(MORNING, null)));
    }

    @Test
    public void constructor_blankDescription_throwsIllegalArgumentException() {
        String message = Medication.MESSAGE_DESCRIPTION_CONSTRAINTS;
        assertThrows(IllegalArgumentException.class, message, () -> new Medication(""));
        assertThrows(IllegalArgumentException.class, message, () -> new Medication("   ", List.of(MORNING)));
    }

    @Test
    public void constructor_descriptionWhitespaceNormalized() {
        assertEquals(DESCRIPTION, new Medication("  Metformin   500mg \t").getDescription());
    }

    @Test
    public void constructor_noTimes_emptyTimes() {
        assertTrue(new Medication(DESCRIPTION).getTimes().isEmpty());
    }

    @Test
    public void constructor_unsortedTimes_sortedAscending() {
        Medication medication = new Medication(DESCRIPTION, List.of(EVENING, MORNING, LocalTime.NOON));
        assertEquals(List.of(MORNING, LocalTime.NOON, EVENING), medication.getTimes());
    }

    @Test
    public void constructor_duplicateTimes_deduplicated() {
        Medication medication = new Medication(DESCRIPTION, List.of(EVENING, MORNING, EVENING, MORNING));
        assertEquals(List.of(MORNING, EVENING), medication.getTimes());
    }

    @Test
    public void constructor_timesWithSeconds_truncatedToMinutes() {
        Medication medication = new Medication(DESCRIPTION, List.of(LocalTime.of(8, 0, 30), MORNING));
        assertEquals(List.of(MORNING), medication.getTimes());
    }

    @Test
    public void constructor_timesDefensivelyCopied() {
        List<LocalTime> times = new ArrayList<>(List.of(MORNING));
        Medication medication = new Medication(DESCRIPTION, times);
        times.clear();
        assertEquals(List.of(MORNING), medication.getTimes());
        assertThrows(UnsupportedOperationException.class, () -> medication.getTimes().add(EVENING));
    }

    @Test
    public void isValidDescription() {
        assertThrows(NullPointerException.class, () -> Medication.isValidDescription(null));
        assertFalse(Medication.isValidDescription(""));
        assertFalse(Medication.isValidDescription("  \t"));
        assertTrue(Medication.isValidDescription("a"));
        assertTrue(Medication.isValidDescription(DESCRIPTION));
    }

    @Test
    public void isValidTime() {
        assertThrows(NullPointerException.class, () -> Medication.isValidTime(null));
        assertTrue(Medication.isValidTime("00:00"));
        assertTrue(Medication.isValidTime("08:30"));
        assertTrue(Medication.isValidTime("23:59"));
        assertFalse(Medication.isValidTime(""));
        assertFalse(Medication.isValidTime("24:00"));
        assertFalse(Medication.isValidTime("08:60"));
        assertFalse(Medication.isValidTime("8:30"));
        assertFalse(Medication.isValidTime("08:5"));
        assertFalse(Medication.isValidTime("0830"));
        assertFalse(Medication.isValidTime("ab:cd"));
        assertFalse(Medication.isValidTime(" 08:30"));
    }

    @Test
    public void toTimeAndFormatTime_roundTrip() {
        assertEquals(LocalTime.of(8, 30), Medication.toTime("08:30"));
        assertEquals("08:30", Medication.formatTime(LocalTime.of(8, 30)));
        assertEquals("00:00", Medication.formatTime(LocalTime.MIDNIGHT));
        assertThrows(NullPointerException.class, () -> Medication.toTime(null));
        assertThrows(NullPointerException.class, () -> Medication.formatTime(null));
    }

    @Test
    public void toTime_invalidTime_failsAssertion() {
        assertThrows(AssertionError.class, () -> Medication.toTime("24:00"));
    }

    @Test
    public void isSameMedication() {
        Medication medication = new Medication(DESCRIPTION, List.of(MORNING));

        assertTrue(medication.isSameMedication(medication));
        assertFalse(medication.isSameMedication(null));
        assertTrue(medication.isSameMedication(new Medication(DESCRIPTION)));
        assertTrue(medication.isSameMedication(new Medication("  metformin   500MG ")));
        assertFalse(medication.isSameMedication(new Medication("Metformin 850mg", List.of(MORNING))));
    }

    @Test
    public void equals() {
        Medication medication = new Medication(DESCRIPTION, List.of(MORNING, EVENING));

        assertEquals(medication, medication);
        assertEquals(medication, new Medication(DESCRIPTION, List.of(EVENING, MORNING)));
        assertEquals(medication.hashCode(), new Medication(DESCRIPTION, List.of(EVENING, MORNING)).hashCode());
        assertNotEquals(medication, null);
        assertNotEquals(medication, DESCRIPTION);
        assertNotEquals(medication, new Medication("Aspirin", List.of(MORNING, EVENING)));
        assertNotEquals(medication, new Medication("metformin 500mg", List.of(MORNING, EVENING)));
        assertNotEquals(medication, new Medication(DESCRIPTION, List.of(MORNING)));
    }

    @Test
    public void toString_withAndWithoutTimes() {
        assertEquals(DESCRIPTION, new Medication(DESCRIPTION).toString());
        assertEquals("Metformin 500mg (08:00, 20:00)",
                new Medication(DESCRIPTION, List.of(EVENING, MORNING)).toString());
    }
}
