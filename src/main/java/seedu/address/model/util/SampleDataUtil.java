package seedu.address.model.util;

import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
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
                List.of("Metformin 500mg twice daily", "Amlodipine 5mg once daily"),
                List.of("Penicillin"),
                List.of("Check blood sugar before breakfast")),
            new Patient(new Name("Bernice Yu"), new Nric("S0000002A"), new Dob("1952-07-22"),
                new Phone("99272758"), new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                List.of(),
                List.of("Shellfish"),
                List.of("Walk for 15 minutes after dinner")),
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
