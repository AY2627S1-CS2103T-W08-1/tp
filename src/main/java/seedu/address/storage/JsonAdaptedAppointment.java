package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;

/**
 * Jackson-friendly version of {@link Appointment}.
 */
class JsonAdaptedAppointment {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Appointment's %s field is missing!";

    private final String date;
    private final String time;
    private final String provider;
    private final String note;

    /**
     * Constructs a {@code JsonAdaptedAppointment} with the given appointment details.
     */
    @JsonCreator
    public JsonAdaptedAppointment(@JsonProperty("date") String date, @JsonProperty("time") String time,
            @JsonProperty("provider") String provider, @JsonProperty("note") String note) {
        this.date = date;
        this.time = time;
        this.provider = provider;
        this.note = note;
    }

    /**
     * Converts a given {@code Appointment} into this class for Jackson use.
     */
    public JsonAdaptedAppointment(Appointment source) {
        requireNonNull(source);
        date = source.getDate().toString();
        time = source.getTime().toString();
        provider = source.getProvider();
        note = source.getNote();
    }

    /**
     * Converts this Jackson-friendly adapted appointment object into the model's {@code Appointment} object.
     * A missing note is treated as no note.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted appointment.
     */
    public Appointment toModelType() throws IllegalValueException {
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"));
        }
        if (!Appointment.isValidDate(date)) {
            throw new IllegalValueException(Appointment.MESSAGE_DATE_CONSTRAINTS);
        }

        if (time == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "time"));
        }
        if (!Appointment.isValidTime(time)) {
            throw new IllegalValueException(Appointment.MESSAGE_TIME_CONSTRAINTS);
        }

        if (provider == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "provider"));
        }
        if (!Appointment.isValidProvider(provider)) {
            throw new IllegalValueException(Appointment.MESSAGE_PROVIDER_CONSTRAINTS);
        }

        String modelNote = note == null ? "" : note;
        return new Appointment(LocalDate.parse(date), LocalTime.parse(time), provider, modelNote);
    }
}
