package seedu.address.testutil;

import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.patient.Patient;

/**
 * A utility class containing a list of {@code Patient} objects to be used in tests.
 */
public class TypicalPatients {

    public static final Patient ALICE = new PatientBuilder().withNric("S0000001A").withName("Alice Pauline")
            .withAddress("123, Jurong West Ave 6, #08-111")
            .withPhone("94351253")
            .build();
    public static final Patient BENSON = new PatientBuilder().withNric("S0000002A").withName("Benson Meier")
            .withAddress("311, Clementi Ave 2, #02-25")
            .withPhone("98765432")
            .build();
    public static final Patient CARL = new PatientBuilder().withNric("S0000003A").withName("Carl Kurz")
            .withPhone("95352563")
            .withAddress("wall street").build();
    public static final Patient DANIEL = new PatientBuilder().withNric("S0000004A").withName("Daniel Meier")
            .withPhone("87652533")
            .withAddress("10th street")
            .withMedications("Metformin").withAllergens("Penicillin")
            .withCareInstructions("Check blood sugar").build();
    public static final Patient ELLE = new PatientBuilder().withNric("S0000005A").withName("Elle Meyer")
            .withPhone("94822240")
            .withAddress("michegan ave").build();
    public static final Patient FIONA = new PatientBuilder().withNric("S0000006A").withName("Fiona Kunz")
            .withPhone("94824270")
            .withAddress("little tokyo").build();
    public static final Patient GEORGE = new PatientBuilder().withNric("S0000007A").withName("George Best")
            .withPhone("94824420")
            .withAddress("4th street").build();

    // Manually added
    public static final Patient HOON = new PatientBuilder().withNric("S0000008A").withName("Hoon Meier")
            .withPhone("84824240")
            .withAddress("little india").build();
    public static final Patient IDA = new PatientBuilder().withNric("S0000009A").withName("Ida Mueller")
            .withPhone("84821310")
            .withAddress("chicago ave").build();

    // Manually added - Patient's details found in {@code CommandTestUtil}
    public static final Patient AMY = new PatientBuilder().withName(VALID_NAME_AMY).withPhone(VALID_PHONE_AMY)
            .withNric("S1234567A").withDob("1950-04-12").withAddress(VALID_ADDRESS_AMY).build();
    public static final Patient BOB = new PatientBuilder().withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
            .withNric("T7654321B").withDob("1945-11-02").withAddress(VALID_ADDRESS_BOB)
            .build();

    public static final String KEYWORD_MATCHING_MEIER = "Meier"; // A keyword that matches MEIER

    private TypicalPatients() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical patients.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (Patient patient : getTypicalPatients()) {
            ab.addPatient(patient);
        }
        return ab;
    }

    public static List<Patient> getTypicalPatients() {
        return new ArrayList<>(Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }
}
