package seedu.address.testutil;

import java.time.LocalDate;
import java.time.LocalTime;

import seedu.address.model.appointment.Appointment;

/**
 * A utility class containing {@code Appointment} objects to be used in tests.
 */
public class TypicalAppointments {

    public static final Appointment CHECKUP = new Appointment(LocalDate.of(2026, 11, 5), LocalTime.of(9, 30),
            "Dr Tan", "Fasting");

    private TypicalAppointments() {} // prevents instantiation
}
