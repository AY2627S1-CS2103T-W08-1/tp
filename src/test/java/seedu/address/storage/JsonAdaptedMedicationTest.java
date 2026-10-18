package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedMedication.MISSING_DESCRIPTION_MESSAGE;
import static seedu.address.storage.JsonAdaptedMedication.NULL_TIME_MESSAGE;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Medication;

public class JsonAdaptedMedicationTest {

    private static final String VALID_DESCRIPTION = "Metformin 500mg";

    @Test
    public void toModelType_validMedication_roundTrips() throws Exception {
        Medication medication = new Medication(VALID_DESCRIPTION, List.of(LocalTime.of(20, 0), LocalTime.of(8, 0)));
        assertEquals(medication, new JsonAdaptedMedication(medication).toModelType());
    }

    @Test
    public void toModelType_noTimes_roundTrips() throws Exception {
        Medication medication = new Medication(VALID_DESCRIPTION);
        assertEquals(medication, new JsonAdaptedMedication(medication).toModelType());
    }

    @Test
    public void toModelType_missingTimes_loadsAsEmpty() throws Exception {
        assertEquals(new Medication(VALID_DESCRIPTION),
                new JsonAdaptedMedication(VALID_DESCRIPTION, null).toModelType());
    }

    @Test
    public void toModelType_unsortedDuplicateTimes_sortedAndDeduplicated() throws Exception {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION,
                List.of("20:00", "08:00", "20:00"));
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)), adapted.toModelType().getTimes());
    }

    @Test
    public void toModelType_nullDescription_throwsIllegalValueException() {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(null, List.of());
        assertThrows(IllegalValueException.class, MISSING_DESCRIPTION_MESSAGE, adapted::toModelType);
    }

    @Test
    public void toModelType_blankDescription_throwsIllegalValueException() {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication("  ", List.of());
        assertThrows(IllegalValueException.class, Medication.MESSAGE_DESCRIPTION_CONSTRAINTS,
                adapted::toModelType);
    }

    @Test
    public void toModelType_invalidTime_throwsIllegalValueException() {
        for (String invalidTime : new String[] {"", "24:00", "08:60", "8:30", "noon"}) {
            JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION, List.of(invalidTime));
            assertThrows(IllegalValueException.class, Medication.MESSAGE_TIME_CONSTRAINTS, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_nullTime_throwsIllegalValueException() {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION, Arrays.asList("08:00", null));
        assertThrows(IllegalValueException.class, NULL_TIME_MESSAGE, adapted::toModelType);
    }
}
