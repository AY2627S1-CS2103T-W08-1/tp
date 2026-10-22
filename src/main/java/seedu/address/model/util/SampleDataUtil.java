package seedu.address.model.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Contact;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Patient[] getSamplePatients() {
        return new Patient[] {
            new Patient(new Name("Alex Yeoh"), new Nric("S0000001A"), new Dob("1948-03-15"),
                new Phone("87438807"), new Address("Blk 30 Geylang Street 29, #06-40"),
                List.of(new Medication("Aspirin 100mg", List.of(LocalTime.of(8, 0))),
                    new Medication("Amlodipine 5mg", List.of(LocalTime.of(8, 0), LocalTime.of(20, 0)))),
                List.of("Penicillin"),
                List.of("Monitor blood pressure every morning"),
                List.of(new Contact("Tan & Partners Clinic", "GP", "+65 6123-4567", "Call in the morning")),
                List.of(new Appointment(LocalDate.of(2027, 11, 5), LocalTime.of(9, 30), "Dr Tan", ""),
                    new Appointment(LocalDate.of(2027, 12, 10), LocalTime.of(14, 0), "Cardiology clinic",
                        "Bring BP log"))),
            new Patient(new Name("Bernice Yu"), new Nric("S0000002A"), new Dob("1952-07-22"),
                new Phone("99272758"), new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                List.of(),
                List.of("Latex"),
                List.of("Walk for 15 minutes after dinner"),
                List.of(),
                List.of(new Appointment(LocalDate.of(2026, 10, 2), LocalTime.of(10, 0), "Polyclinic nurse",
                    "Blood pressure normal"))),
            new Patient(new Name("Charlotte Oliveiro"), new Nric("S0000003A"), new Dob("1945-11-02"),
                new Phone("93210283"), new Address("Blk 11 Ang Mo Kio Street 74, #11-04")),
            new Patient(new Name("David Li"), new Nric("S0000004A"), new Dob("1956-09-30"),
                new Phone("91031282"), new Address("Blk 436 Serangoon Gardens Street 26, #16-43"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Patient samplePatient : getSamplePatients()) {
            sampleAb.addPatient(samplePatient);
        }
        return sampleAb;
    }

}
