package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Medication;

/**
 * Jackson-friendly version of {@link Medication}.
 * The times are stored as "HH:MM" strings; a missing list of times is loaded as no times.
 * A plain string, as saved by v1.3, is loaded as a description with no times.
 */
class JsonAdaptedMedication {

    public static final String MISSING_DESCRIPTION_MESSAGE = "Medication's description field is missing!";
    public static final String NULL_TIME_MESSAGE = "Medication's times must not contain null.";

    private final String description;
    private final List<String> times = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedMedication} with the given description and times.
     */
    @JsonCreator
    public JsonAdaptedMedication(@JsonProperty("description") String description,
            @JsonProperty("times") List<String> times) {
        this.description = description;
        if (times != null) {
            this.times.addAll(times);
        }
    }

    /**
     * Converts a given {@code Medication} into this class for Jackson use.
     */
    public JsonAdaptedMedication(Medication source) {
        requireNonNull(source);
        description = source.getDescription();
        source.getTimes().forEach(time -> times.add(Medication.formatTime(time)));
    }

    /**
     * Constructs a {@code JsonAdaptedMedication} from a v1.3 plain-string medication, which has no times.
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static JsonAdaptedMedication fromDescription(String description) {
        return new JsonAdaptedMedication(description, null);
    }

    /**
     * Converts this Jackson-friendly adapted medication object into the model's {@code Medication} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted medication.
     */
    public Medication toModelType() throws IllegalValueException {
        if (description == null) {
            throw new IllegalValueException(MISSING_DESCRIPTION_MESSAGE);
        }
        if (!Medication.isValidDescription(description)) {
            throw new IllegalValueException(Medication.MESSAGE_DESCRIPTION_CONSTRAINTS);
        }

        List<LocalTime> modelTimes = new ArrayList<>();
        for (String time : times) {
            if (time == null) {
                throw new IllegalValueException(NULL_TIME_MESSAGE);
            }
            if (!Medication.isValidTime(time)) {
                throw new IllegalValueException(Medication.MESSAGE_TIME_CONSTRAINTS);
            }
            modelTimes.add(Medication.toTime(time));
        }
        return new Medication(description, modelTimes);
    }
}
