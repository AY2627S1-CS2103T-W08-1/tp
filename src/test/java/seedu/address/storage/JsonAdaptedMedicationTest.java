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
import seedu.address.commons.util.JsonUtil;
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
    public void toModelType_descriptionWithLineBreak_loadsAsOneLine() throws Exception {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication("Aspirin\n20:00 Amy Tan: Warfarin",
                List.of("08:00"));
        assertEquals("Aspirin 20:00 Amy Tan: Warfarin", adapted.toModelType().getDescription());
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
    public void toModelType_descriptionAtAndOverMaxLength_loadedAndRejected() throws Exception {
        String atLimit = "m".repeat(Medication.MAX_DESCRIPTION_LENGTH);
        assertEquals(new Medication(atLimit), new JsonAdaptedMedication(atLimit, List.of()).toModelType());

        JsonAdaptedMedication overlong = new JsonAdaptedMedication(atLimit + "m", List.of());
        assertThrows(IllegalValueException.class, Medication.MESSAGE_DESCRIPTION_TOO_LONG, overlong::toModelType);
    }

    @Test
    public void toModelType_descriptionLongOnlyBecauseOfNoBreakSpaces_loaded() throws Exception {
        String word = "m".repeat(249);
        String description = word + "\u00a0".repeat(5) + word; // 503 characters, 499 after normalization
        assertEquals(word + " " + word, new JsonAdaptedMedication(description, List.of()).toModelType()
                .getDescription());
    }

    @Test
    public void toModelType_descriptionLongAfterNormalization_rejected() {
        String word = "m".repeat(250);
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(word + "\u00a0\u00a0 " + word + "m", List.of());
        assertThrows(IllegalValueException.class, Medication.MESSAGE_DESCRIPTION_TOO_LONG, adapted::toModelType);
    }

    @Test
    public void toModelType_invalidTime_throwsIllegalValueException() {
        for (String invalidTime : new String[] {"", "24:00", "08:60", "8:30", "noon"}) {
            JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION, List.of(invalidTime));
            assertThrows(IllegalValueException.class, Medication.MESSAGE_DATA_FILE_TIME_CONSTRAINTS,
                    adapted::toModelType);
        }
    }

    @Test
    public void toModelType_oneDigitHour_requiresTwoDigits() {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION, List.of("8:30"));
        assertThrows(IllegalValueException.class, "Time must be in HH:MM (24-hour), e.g. 08:30.",
                adapted::toModelType);
    }

    @Test
    public void toModelType_nullTime_throwsIllegalValueException() {
        JsonAdaptedMedication adapted = new JsonAdaptedMedication(VALID_DESCRIPTION, Arrays.asList("08:00", null));
        assertThrows(IllegalValueException.class, NULL_TIME_MESSAGE, adapted::toModelType);
    }

    @Test
    public void toModelType_v13PlainStringMedication_loadsWithNoTimes() throws Exception {
        JsonAdaptedMedication adapted = JsonUtil.fromJsonString("\"" + VALID_DESCRIPTION + "\"",
                JsonAdaptedMedication.class);
        assertEquals(new Medication(VALID_DESCRIPTION), adapted.toModelType());
    }

    @Test
    public void toModelType_v13PatientWithStringMedications_loadsMedicationsWithNoTimes() throws Exception {
        String v13Patient = "{\"name\": \"Alex Yeoh\", \"nric\": \"S1234567A\", \"dob\": \"1950-04-12\", "
                + "\"phone\": \"87438807\", \"address\": \"Blk 30 Geylang Street 29, #06-40\", "
                + "\"medications\": [\"Aspirin 100mg once daily\", \"Amlodipine 5mg once daily\"]}";
        JsonAdaptedPatient adapted = JsonUtil.fromJsonString(v13Patient, JsonAdaptedPatient.class);
        assertEquals(List.of(new Medication("Aspirin 100mg once daily"),
                new Medication("Amlodipine 5mg once daily")), adapted.toModelType().getMedications());
    }
}
