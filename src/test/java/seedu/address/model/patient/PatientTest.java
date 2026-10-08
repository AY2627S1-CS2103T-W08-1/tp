package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BOB;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PatientBuilder;

public class PatientTest {

    @Test
    public void constructor_emptyCareLists() {
        Patient patient = new PatientBuilder().build();
        assertTrue(patient.getMedications().isEmpty());
        assertTrue(patient.getAllergens().isEmpty());
        assertTrue(patient.getCareInstructions().isEmpty());
    }

    @Test
    public void constructor_careLists_defensivelyCopied() {
        List<String> medications = new ArrayList<>(List.of("Metformin"));
        List<String> allergens = new ArrayList<>(List.of("Penicillin"));
        List<String> instructions = new ArrayList<>(List.of("Check blood sugar"));
        Patient patient = new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(), ALICE.getPhone(),
                ALICE.getAddress(), medications, allergens, instructions);
        medications.clear();
        allergens.clear();
        instructions.clear();
        assertEquals(List.of("Metformin"), patient.getMedications());
        assertEquals(List.of("Penicillin"), patient.getAllergens());
        assertEquals(List.of("Check blood sugar"), patient.getCareInstructions());
        assertThrows(UnsupportedOperationException.class, () -> patient.getMedications().add("Aspirin"));
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
    }

    @Test
    public void toStringMethod() {
        String expected = Patient.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", nric=" + ALICE.getNric() + ", dob=" + ALICE.getDob() + ", address=" + ALICE.getAddress()
                + ", medications=[], allergens=[], careInstructions=[]}";
        assertEquals(expected, ALICE.toString());
    }
}
