package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.DANIEL;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.ui.PatientDetailPanel;

public class PatientFormatterTest {

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
    public void formatRecord_nullPatient_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PatientFormatter.formatRecord(null));
    }

    @Test
    public void formatRecord_patientWithCareInfo_listsEachCareListUnderItsHeading() {
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
                "1. Check blood sugar");

        assertEquals(expectedLines, PatientFormatter.formatRecord(DANIEL));
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
                "None recorded");

        assertEquals(expectedLines, PatientFormatter.formatRecord(ALICE));
    }
}
