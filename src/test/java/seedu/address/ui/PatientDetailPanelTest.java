package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.ui.PatientDetailPanel.MESSAGE_NONE_RECORDED;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PatientDetailPanelTest {

    @Test
    public void formatEntries_emptyList_returnsNoneRecorded() {
        assertEquals(MESSAGE_NONE_RECORDED, PatientDetailPanel.formatEntries(List.of()));
    }

    @Test
    public void formatEntries_singleEntry_returnsBulletedEntry() {
        assertEquals("- Metformin 500mg", PatientDetailPanel.formatEntries(List.of("Metformin 500mg")));
    }

    @Test
    public void formatEntries_multipleEntries_returnsOneEntryPerLineInOrder() {
        assertEquals("- Penicillin\n- Peanuts",
                PatientDetailPanel.formatEntries(List.of("Penicillin", "Peanuts")));
    }
}
