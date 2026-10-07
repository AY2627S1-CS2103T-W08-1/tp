package seedu.address.model.util;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.Phone;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Patient[] getSamplePatients() {
        return new Patient[] {
            new Patient(new Name("Alex Yeoh"), "S0000001A", "1950-01-01",
                new Phone("87438807"), new Address("Blk 30 Geylang Street 29, #06-40")),
            new Patient(new Name("Bernice Yu"), "S0000002A", "1950-01-01",
                new Phone("99272758"), new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18")),
            new Patient(new Name("Charlotte Oliveiro"), "S0000003A", "1950-01-01",
                new Phone("93210283"), new Address("Blk 11 Ang Mo Kio Street 74, #11-04")),
            new Patient(new Name("David Li"), "S0000004A", "1950-01-01",
                new Phone("91031282"), new Address("Blk 436 Serangoon Gardens Street 26, #16-43")),
            new Patient(new Name("Irfan Ibrahim"), "S0000005A", "1950-01-01",
                new Phone("92492021"), new Address("Blk 47 Tampines Street 20, #17-35")),
            new Patient(new Name("Roy Balakrishnan"), "S0000006A", "1950-01-01",
                new Phone("92624417"), new Address("Blk 45 Aljunied Street 85, #11-31"))
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
