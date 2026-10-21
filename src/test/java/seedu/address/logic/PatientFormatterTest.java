package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
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
import seedu.address.ui.PatientDetailPanel;

public class PatientFormatterTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 14, 30);

    @Test
    public void noneRecorded_detailPanelConstant_sameText() {
        assertEquals(PatientDetailPanel.MESSAGE_NONE_RECORDED, PatientFormatter.NONE_RECORDED);
    }

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
    public void formatRecord_nullPatientOrTime_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatRecord(null, NOW));
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatRecord(ALICE, null));
    }

    @Test
    public void formatRecord_patientWithCareInfo_listsEachListUnderItsHeading() {
        List<String> expectedLines = List.of(
                "Daniel Meier",
                "NRIC: S0000004A",
                "DOB: " + DANIEL.getDob(),
                "Phone: 87652533",
                "Address: 10th street",
                "",
                "Medications",
                "1. Metformin (08:00, 20:00)",
                "",
                "Allergens",
                "1. Penicillin",
                "",
                "Care Instructions",
                "1. Check blood sugar",
                "",
                "Contacts",
                "1. Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning",
                "",
                "Appointments",
                "1. 2026-11-05 09:30 with Dr Tan (Fasting)");

        assertEquals(expectedLines, PatientFormatter.formatRecord(DANIEL, NOW));
    }

    @Test
    public void formatRecord_patientWithoutCareInfo_showsNoneRecordedInEachSection() {
        List<String> expectedLines = List.of(
                "Alice Pauline",
                "NRIC: S0000001A",
                "DOB: " + ALICE.getDob(),
                "Phone: 94351253",
                "Address: 123, Jurong West Ave 6, #08-111",
                "",
                "Medications",
                "None recorded",
                "",
                "Allergens",
                "None recorded",
                "",
                "Care Instructions",
                "None recorded",
                "",
                "Contacts",
                "None recorded",
                "",
                "Appointments",
                "None recorded");

        assertEquals(expectedLines, PatientFormatter.formatRecord(ALICE, NOW));
    }

    @Test
    public void formatRecord_pastAndUpcomingAppointments_upcomingFirst() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Polyclinic", "");
        Appointment upcoming = new Appointment(LocalDate.of(2026, 12, 1), LocalTime.of(14, 0), "Dr Lim", "");
        Patient patient = new PatientBuilder(ALICE).withAppointments(past, upcoming).build();

        List<String> lines = PatientFormatter.formatRecord(patient, NOW);

        assertEquals(List.of("Appointments", "1. 2026-12-01 14:00 with Dr Lim", "2. 2026-10-01 09:00 with Polyclinic"),
                lines.subList(lines.size() - 3, lines.size()));
    }
}
