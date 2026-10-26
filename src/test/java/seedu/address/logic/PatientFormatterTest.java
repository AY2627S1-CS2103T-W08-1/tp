package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.ARCHIVED_ALICE;
import static seedu.address.testutil.TypicalPatients.DANIEL;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class PatientFormatterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 14, 30);

    @Test
    public void formatDetails_nullPatient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatDetails(null));
    }

    @Test
    public void formatDetails_patient_returnsLabeledLinesInPanelOrder() {
        assertEquals(List.of("NRIC: S0000001A", "DOB: " + ALICE.getDob(), "Phone: 94351253",
                "Address: 123, Jurong West Ave 6, #08-111"), PatientFormatter.formatDetails(ALICE));
    }

    @Test
    public void formatDetails_archivedPatient_endsWithArchivedStatus() {
        assertEquals(List.of("NRIC: S0000001A", "DOB: " + ALICE.getDob(), "Phone: 94351253",
                "Address: 123, Jurong West Ave 6, #08-111", PatientFormatter.ARCHIVED_STATUS),
                PatientFormatter.formatDetails(ARCHIVED_ALICE));
    }

    @Test
    public void formatEntries_nullEntries_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatEntries(null));
    }

    @Test
    public void formatEntries_noEntries_returnsNoneRecorded() {
        assertEquals(List.of("None recorded"), PatientFormatter.formatEntries(List.of()));
    }

    @Test
    public void formatEntries_entries_numbersEachEntryFromOne() {
        assertEquals(List.of("1. Metformin 500mg", "2. Aspirin 100mg"),
                PatientFormatter.formatEntries(List.of("Metformin 500mg", "Aspirin 100mg")));
    }

    @Test
    public void formatEntries_entriesWithLineBreaks_eachEntryKeptOnOneLine() {
        // only a hand-edited data file can hold entries like these
        List<String> entries = List.of("Check sugar\nbefore meals", "Turn every \r\n 2 hours", "Line one\n\nLine two");

        assertEquals(List.of("1. Check sugar before meals", "2. Turn every 2 hours", "3. Line one Line two"),
                PatientFormatter.formatEntries(entries));
    }

    @Test
    public void formatEntries_longRunOfSpacesWithoutLineBreak_spacesKept() {
        String entry = "Take" + " ".repeat(60_000) + "daily";

        List<String> lines = assertTimeout(Duration.ofSeconds(1), () -> PatientFormatter.formatEntries(List.of(entry)));

        assertEquals(List.of("1. " + entry), lines);
    }

    @Test
    public void formatEntries_tenEntries_lastNumberHasTwoDigits() {
        List<String> entries = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            entries.add("Entry " + i);
        }

        List<String> lines = PatientFormatter.formatEntries(entries);

        assertEquals("9. Entry 9", lines.get(8));
        assertEquals("10. Entry 10", lines.get(9));
    }

    @Test
    public void formatAppointments_nullAppointmentsOrTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatAppointments(null, NOW));
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatAppointments(List.of(), null));
    }

    @Test
    public void formatAppointments_noAppointments_returnsNoEntries() {
        assertEquals(List.of(), PatientFormatter.formatAppointments(List.of(), NOW));
    }

    @Test
    public void formatAppointments_pastAndUpcomingAppointments_orderKeptAndOnlyPastMarked() {
        Appointment upcoming = new Appointment(LocalDate.of(2026, 12, 1), LocalTime.of(14, 0), "Dr Lim", "");
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Polyclinic", "");

        assertEquals(List.of("2026-12-01 14:00 with Dr Lim", "2026-10-01 09:00 with Polyclinic (past)"),
                PatientFormatter.formatAppointments(List.of(upcoming, past), NOW));
    }

    @Test
    public void formatAppointments_appointmentInCurrentMinute_notMarked() {
        Appointment atNow = new Appointment(NOW.toLocalDate(), NOW.toLocalTime(), "Dr Lim", "");

        assertEquals(List.of("2026-10-20 14:30 with Dr Lim"),
                PatientFormatter.formatAppointments(List.of(atNow), NOW.plusSeconds(59)));
    }

    @Test
    public void formatRecord_nullPatientOrTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatRecord(null, NOW));
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatRecord(ALICE, null));
    }

    @Test
    public void formatRecord_patientWithCareInfo_listsEachListUnderItsHeading() {
        // Equivalence partitioning: a patient with an entry in every list, the partition with the fullest record
        List<String> expectedLines = new ArrayList<>(List.of("Daniel Meier", "NRIC: S0000004A",
                "DOB: " + DANIEL.getDob(), "Phone: 87652533", "Address: 10th street"));
        expectedLines.addAll(createSection("Medications", "1. Metformin (08:00, 20:00)"));
        expectedLines.addAll(createSection("Allergens", "1. Penicillin"));
        expectedLines.addAll(createSection("Care Instructions", "1. Check blood sugar"));
        expectedLines.addAll(createSection("Contacts",
                "1. Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning"));
        expectedLines.addAll(createSection("Appointments", "1. 2026-11-05 09:30 with Dr Tan (Fasting)"));

        assertEquals(expectedLines, PatientFormatter.formatRecord(DANIEL, NOW));
    }

    @Test
    public void formatRecord_patientWithoutCareInfo_showsNoneRecordedInEachSection() {
        // Equivalence partitioning: a patient with every list empty, the partition shown as "None recorded"
        List<String> expectedLines = new ArrayList<>(List.of("Alice Pauline", "NRIC: S0000001A",
                "DOB: " + ALICE.getDob(), "Phone: 94351253", "Address: 123, Jurong West Ave 6, #08-111"));
        for (String heading : List.of("Medications", "Allergens", "Care Instructions", "Contacts", "Appointments")) {
            expectedLines.addAll(createSection(heading, "None recorded"));
        }

        assertEquals(expectedLines, PatientFormatter.formatRecord(ALICE, NOW));
    }

    @Test
    public void formatRecord_pastAndUpcomingAppointments_upcomingFirstAndPastMarked() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Polyclinic", "");
        Appointment upcoming = new Appointment(LocalDate.of(2026, 12, 1), LocalTime.of(14, 0), "Dr Lim", "");
        Patient patient = new PatientBuilder(ALICE).withAppointments(past, upcoming).build();

        List<String> lines = PatientFormatter.formatRecord(patient, NOW);

        assertEquals(List.of("Appointments", "1. 2026-12-01 14:00 with Dr Lim",
                "2. 2026-10-01 09:00 with Polyclinic (past)"),
                lines.subList(lines.size() - 3, lines.size()));
    }

    @Test
    public void formatRecord_appointmentAtNow_listedAsUpcomingAndNotMarked() {
        Appointment minuteEarlier = new Appointment(NOW.toLocalDate(), LocalTime.of(14, 29), "Polyclinic", "");
        Appointment atNow = new Appointment(NOW.toLocalDate(), NOW.toLocalTime(), "Dr Lim", "");
        Patient patient = new PatientBuilder(ALICE).withAppointments(minuteEarlier, atNow).build();

        List<String> lines = PatientFormatter.formatRecord(patient, NOW);

        assertEquals(List.of("Appointments", "1. 2026-10-20 14:30 with Dr Lim",
                "2. 2026-10-20 14:29 with Polyclinic (past)"),
                lines.subList(lines.size() - 3, lines.size()));
    }

    /**
     * Returns the lines that the record shows for one list: a blank line, {@code heading}, then {@code entries}.
     */
    private static List<String> createSection(String heading, String... entries) {
        List<String> lines = new ArrayList<>(List.of("", heading));
        lines.addAll(List.of(entries));
        return lines;
    }
}
