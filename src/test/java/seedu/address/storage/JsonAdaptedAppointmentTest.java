package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedAppointment.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.appointment.Appointment;

public class JsonAdaptedAppointmentTest {
    private static final String INVALID_DATE = "2026-02-30";
    private static final String INVALID_TIME = "9:30";
    private static final String INVALID_PROVIDER = " ";

    private static final String VALID_DATE = "2026-11-05";
    private static final String VALID_TIME = "09:30";
    private static final String VALID_PROVIDER = "Dr Tan";

    @Test
    public void toModelType_validAppointmentDetails_returnsAppointment() throws Exception {
        assertEquals(CHECKUP, new JsonAdaptedAppointment(CHECKUP).toModelType());
    }

    @Test
    public void toModelType_nullNote_returnsAppointmentWithoutNote() throws Exception {
        JsonAdaptedAppointment appointment =
                new JsonAdaptedAppointment(VALID_DATE, VALID_TIME, VALID_PROVIDER, null);
        assertEquals("", appointment.toModelType().getNote());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment =
                new JsonAdaptedAppointment(INVALID_DATE, VALID_TIME, VALID_PROVIDER, "");
        String expectedMessage = Appointment.MESSAGE_DATE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }

    @Test
    public void toModelType_nullDate_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment = new JsonAdaptedAppointment(null, VALID_TIME, VALID_PROVIDER, "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "date");
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }

    @Test
    public void toModelType_invalidTime_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment =
                new JsonAdaptedAppointment(VALID_DATE, INVALID_TIME, VALID_PROVIDER, "");
        String expectedMessage = Appointment.MESSAGE_TIME_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }

    @Test
    public void toModelType_nullTime_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment = new JsonAdaptedAppointment(VALID_DATE, null, VALID_PROVIDER, "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "time");
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }

    @Test
    public void toModelType_invalidProvider_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment =
                new JsonAdaptedAppointment(VALID_DATE, VALID_TIME, INVALID_PROVIDER, "");
        String expectedMessage = Appointment.MESSAGE_PROVIDER_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }

    @Test
    public void toModelType_lineBreakInProviderOrNote_throwsIllegalValueException() {
        // commands cannot type a line break, which would show as a fake appointment in a list
        JsonAdaptedAppointment providerWithLineBreak =
                new JsonAdaptedAppointment(VALID_DATE, VALID_TIME, "Dr Tan\n2. 2099-01-01 09:00 with Dr Fake", "");
        JsonAdaptedAppointment noteWithLineBreak = new JsonAdaptedAppointment(VALID_DATE, VALID_TIME, VALID_PROVIDER,
                "Bring results)\n2. 2099-01-01 09:00 with Dr Fake");

        assertThrows(IllegalValueException.class, Appointment.MESSAGE_PROVIDER_CONSTRAINTS,
                providerWithLineBreak::toModelType);
        assertThrows(IllegalValueException.class, Appointment.MESSAGE_NOTE_CONSTRAINTS, noteWithLineBreak::toModelType);
    }

    @Test
    public void toModelType_nullProvider_throwsIllegalValueException() {
        JsonAdaptedAppointment appointment = new JsonAdaptedAppointment(VALID_DATE, VALID_TIME, null, "");
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "provider");
        assertThrows(IllegalValueException.class, expectedMessage, appointment::toModelType);
    }
}
