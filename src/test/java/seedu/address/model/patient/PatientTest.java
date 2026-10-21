package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BOB;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.appointment.Appointment;
import seedu.address.testutil.PatientBuilder;

public class PatientTest {

    @Test
    public void withCareInstructions_replacement_preservesOtherFields() {
        Medication medication = new Medication("Medication", List.of(LocalTime.of(8, 0)));
        Patient original = new PatientBuilder(ALICE).withMedications(medication).withAllergens("Allergen")
                .withCareInstructions("Old instruction").withContacts(GP)
                .withAppointments(CHECKUP).withArchived(true).build();
        List<String> instructions = new ArrayList<>(List.of("New instruction"));

        Patient updated = original.withCareInstructions(instructions);
        instructions.clear();

        assertEquals(new PatientBuilder(original).withCareInstructions("New instruction").build(), updated);
        assertEquals(medication.getTimes(), updated.getMedications().get(0).getTimes());
        assertEquals(original.getContacts(), updated.getContacts());
        assertEquals(List.of(CHECKUP), updated.getAppointments());
        assertTrue(updated.isArchived());
        assertThrows(UnsupportedOperationException.class, () -> updated.getCareInstructions().clear());
        assertThrows(NullPointerException.class, () -> original.withCareInstructions(null));
    }

    @Test
    public void withAllergens_replacement_preservesOtherFields() {
        Medication medication = new Medication("Medication", List.of(LocalTime.of(8, 0)));
        Patient original = new PatientBuilder(ALICE).withMedications(medication).withAllergens("Allergen")
                .withCareInstructions("Instruction").withContacts(GP)
                .withAppointments(CHECKUP).withArchived(true).build();
        List<String> allergens = new ArrayList<>(List.of("Latex", "Shellfish"));

        Patient updated = original.withAllergens(allergens);

        assertEquals(new PatientBuilder(original).withAllergens("Latex", "Shellfish").build(), updated);
        assertFalse(original.equals(updated));
        assertEquals(List.of(medication), updated.getMedications());
        assertEquals(List.of(GP), updated.getContacts());
        assertEquals(List.of(CHECKUP), updated.getAppointments());
        assertTrue(updated.isArchived());
        assertThrows(UnsupportedOperationException.class, () -> updated.getAllergens().clear());
        assertThrows(NullPointerException.class, () -> original.withAllergens(null));
        allergens.clear();
        assertEquals(List.of("Latex", "Shellfish"), updated.getAllergens());
    }

    @Test
    public void withContacts_replacement_preservesOtherFields() {
        Patient original = new PatientBuilder(ALICE).withMedications("Medication").withAllergens("Allergen")
                .withCareInstructions("Instruction").withAppointments(CHECKUP).withArchived(true).build();
        Contact contact = GP;
        Patient updated = original.withContacts(List.of(contact));
        assertTrue(original.getContacts().isEmpty());
        assertEquals(new PatientBuilder(original).withContacts(contact).build(), updated);
        assertFalse(original.equals(updated));
        assertEquals(List.of(CHECKUP), updated.getAppointments());
        assertTrue(updated.isArchived());
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
        assertTrue(patient.getAppointments().isEmpty());
        assertFalse(patient.isArchived());
    }

    @Test
    public void withArchived_replacementPreservesOtherFields() {
        Medication medication = new Medication("Metformin");
        Patient original = new PatientBuilder(ALICE).withMedications(medication).withAllergens("Penicillin")
                .withCareInstructions("Check blood sugar").withContacts(GP).withAppointments(CHECKUP).build();

        Patient archived = original.withArchived(true);

        assertEquals(new PatientBuilder(original).withArchived(true).build(), archived);
        assertTrue(archived.isArchived());
        assertFalse(original.isArchived());
        assertEquals(List.of(CHECKUP), archived.getAppointments());
        assertEquals(original, archived.withArchived(false));
    }

    @Test
    public void constructor_careLists_defensivelyCopied() {
        List<Medication> medications = new ArrayList<>(List.of(new Medication("Metformin")));
        List<String> allergens = new ArrayList<>(List.of("Penicillin"));
        List<String> instructions = new ArrayList<>(List.of("Check blood sugar"));
        Patient patient = new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(), ALICE.getPhone(),
                ALICE.getAddress(), medications, allergens, instructions, List.of());
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
    public void constructor_unsortedAppointments_sortedByDateThenTime() {
        Appointment laterDate = new Appointment(LocalDate.of(2026, 12, 1), LocalTime.of(8, 0), "Dr Lee", "");
        Appointment laterTime = new Appointment(LocalDate.of(2026, 11, 5), LocalTime.of(14, 0), "Dr Lim", "");
        Appointment earliest = new Appointment(LocalDate.of(2026, 11, 5), LocalTime.of(9, 30), "Dr Tan", "");
        List<Appointment> appointments = new ArrayList<>(List.of(laterDate, laterTime, earliest));
        Patient patient = new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(), ALICE.getPhone(),
                ALICE.getAddress(), List.of(), List.of(), List.of(), List.of(), appointments);
        assertEquals(List.of(earliest, laterTime, laterDate), patient.getAppointments());
        assertEquals(List.of(laterDate, laterTime, earliest), appointments);
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
                ALICE.getPhone(), ALICE.getAddress(), null, List.of(), List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(),
                ALICE.getPhone(), ALICE.getAddress(), List.of(), List.of(), List.of(), List.of(), null));
        assertThrows(NullPointerException.class, () -> new Patient(ALICE.getName(), ALICE.getNric(), ALICE.getDob(),
                ALICE.getPhone(), ALICE.getAddress(), List.of(), List.of(), List.of(), List.of(),
                Collections.singletonList(null)));
    }

    @Test
    public void getAppointmentsUpcomingFirst() {
        Appointment past = new Appointment(LocalDate.of(2026, 10, 1), LocalTime.of(9, 0), "Dr Lee", "");
        Appointment now = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 0), "Dr Lim", "");
        Appointment later = new Appointment(LocalDate.of(2026, 12, 1), LocalTime.of(8, 0), "Dr Tan", "");
        Patient patient = new PatientBuilder(ALICE).withAppointments(later, past, now).build();

        // appointments in the current minute count as upcoming
        assertEquals(List.of(now, later, past),
                patient.getAppointmentsUpcomingFirst(LocalDateTime.of(2026, 10, 18, 12, 0)));
        assertEquals(List.of(now, later, past),
                patient.getAppointmentsUpcomingFirst(LocalDateTime.of(2026, 10, 18, 12, 0, 30)));
        assertEquals(List.of(later, past, now),
                patient.getAppointmentsUpcomingFirst(LocalDateTime.of(2026, 10, 18, 12, 1)));
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
    public void withAppointments_newList_replacesOnlyAppointments() {
        Patient patient = new PatientBuilder(ALICE).withMedications("Metformin").withArchived(true).build();
        Patient expected = new PatientBuilder(patient).withAppointments(CHECKUP).build();
        assertEquals(expected, patient.withAppointments(List.of(CHECKUP)));
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
        assertNotEquals(ALICE, new PatientBuilder(ALICE).withAppointments(CHECKUP).build());
    }

    @Test
    public void toStringMethod() {
        String expected = Patient.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", nric=" + ALICE.getNric() + ", dob=" + ALICE.getDob() + ", address=" + ALICE.getAddress()
                + ", medications=[], allergens=[], careInstructions=[], contacts=[], appointments=[], archived=false}";
        assertEquals(expected, ALICE.toString());
    }
}
