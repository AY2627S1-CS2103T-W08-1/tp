package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BOB;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PatientBuilder;

public class PatientTest {

    @Test
    public void withContacts_replacement_preservesOtherFields() {
        Patient original = new PatientBuilder(ALICE).withMedications("Medication").withAllergens("Allergen")
                .withCareInstructions("Instruction").withArchived(true).build();
        Contact contact = GP;
        Patient updated = original.withContacts(List.of(contact));
        assertTrue(original.getContacts().isEmpty());
        assertEquals(new PatientBuilder(original).withContacts(contact).build(), updated);
        assertFalse(original.equals(updated));
        assertThrows(UnsupportedOperationException.class, () -> updated.getContacts().clear());
        assertThrows(NullPointerException.class, () -> original.withContacts(null));
        assertThrows(IllegalArgumentException.class, Contact.MESSAGE_DUPLICATE, () ->
                original.withContacts(List.of(contact, contact)));
        List<Contact> entries = new ArrayList<>(List.of(contact));
        Patient copied = original.withContacts(entries);
        entries.clear();
        assertEquals(List.of(contact), copied.getContacts());
    }


    @Test
    public void constructor_emptyCareLists() {
        Patient patient = new PatientBuilder().build();
        assertTrue(patient.getMedications().isEmpty());
        assertTrue(patient.getAllergens().isEmpty());
        assertTrue(patient.getCareInstructions().isEmpty());
        assertFalse(patient.isArchived());
    }

    @Test
    public void withArchived_replacementPreservesOtherFields() {
        Medication medication = new Medication("Metformin");
        Patient original = new PatientBuilder(ALICE).withMedications(medication).withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar").withContacts(GP).build();

        Patient archived = original.withArchived(true);

        assertEquals(new PatientBuilder(original).withArchived(true).build(), archived);
        assertTrue(archived.isArchived());
        assertFalse(original.isArchived());
        assertEquals(original, archived.withArchived(false));
    }

    @Test
    public void constructor_careLists_defensivelyCopied() {
        List<Medication> medications = new ArrayList<>(List.of(new Medication("Metformin")));
        List<String> allergens = new ArrayList<>(List.of("Penicillin"));
        List<String> instructions = new ArrayList<>(List.of("Check blood sugar"));
        Patient patient = new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(), ALICE.getPhone(),
                ALICE.getAddress(), medications, allergens, instructions);
        medications.clear();
        allergens.clear();
        instructions.clear();
        assertEquals(List.of(new Medication("Metformin")), patient.getMedications());
        assertEquals(List.of("Penicillin"), patient.getAllergens());
        assertEquals(List.of("Check blood sugar"), patient.getCareInstructions());
        Medication aspirin = new Medication("Aspirin");
        assertThrows(UnsupportedOperationException.class, () -> patient.getMedications().add(aspirin));
        assertThrows(UnsupportedOperationException.class, () -> patient.getAllergens().clear());
        assertThrows(UnsupportedOperationException.class, () -> patient.getCareInstructions().remove(0));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Patient(null, ALICE.getNric(), ALICE.getDob(),
                ALICE.getPhone(), ALICE.getAddress()));
        assertThrows(NullPointerException.class, () -> new Patient(ALICE.getName(), null, ALICE.getDob(),
                ALICE.getPhone(), ALICE.getAddress()));
        assertThrows(NullPointerException.class, () -> new Patient(ALICE.getName(), ALICE.getNric(), null,
                ALICE.getPhone(), ALICE.getAddress()));
        assertThrows(NullPointerException.class, () -> new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(),
                ALICE.getPhone(), ALICE.getAddress(), null, List.of(), List.of()));
    }

    @Test
    public void isSamePatient() {
        assertTrue(ALICE.isSamePatient(ALICE));
        assertFalse(ALICE.isSamePatient(null));
        assertTrue(ALICE.isSamePatient(new PatientBuilder(BOB).withNric(ALICE.getNric().value.toLowerCase()).build()));
        assertFalse(ALICE.isSamePatient(new PatientBuilder(ALICE).withNric(BOB.getNric().value).build()));
        assertTrue(ALICE.isSamePatient(new PatientBuilder(ALICE).withName("Other Name").build()));
        assertTrue(ALICE.isSamePatient(new PatientBuilder(ALICE).withMedications("Metformin")
                .withAllergens("Penicillin").withCareInstructions("Rest").build()));
        assertTrue(ALICE.isSamePatient(new PatientBuilder(ALICE).withArchived(true).build()));
    }

    @Test
    public void equals() {
        Patient copy = new PatientBuilder(ALICE).build();
        assertEquals(ALICE, copy);
        assertEquals(ALICE.hashCode(), copy.hashCode());
        assertFalse(ALICE.equals(null));
        assertFalse(ALICE.equals(5));
        assertNotEquals(ALICE, BOB);
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withName("Other Name").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withNric(BOB.getNric().value).build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withDob("1940-01-01").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withPhone("91234567").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withAddress("Other address").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withMedications("Metformin").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withAllergens("Penicillin").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withCareInstructions("Rest").build());
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withArchived(true).build());
    }

    @Test
    public void toStringMethod() {
        String expected = Patient.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", nric=" + ALICE.getNric() + ", dob=" + ALICE.getDob() + ", address=" + ALICE.getAddress()
                + ", medications=[], allergens=[], careInstructions=[], contacts=[], archived=false}";
        assertEquals(expected, ALICE.toString());
    }
}
