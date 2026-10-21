package seedu.address.model.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalContacts.GUARDIAN;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ContactTest {

    @Test
    public void constructor_validFields_normalized() {
        Contact contact = new Contact(
                "  Tan   & Partners Clinic  ", " GP ", " +65 6123-4567 ", "Call in the morning");
        assertEquals(GP, contact);
        assertEquals(GP.hashCode(), contact.hashCode());
        assertEquals("Tan & Partners Clinic", contact.getName());
        assertEquals("GP", contact.getRole());
        assertEquals("+65 6123-4567", contact.getPhone());
        assertEquals("Call in the morning", contact.getNote());
    }

    @Test
    public void constructor_note_preservesText() {
        Contact contact = new Contact("Clinic", "GP", "123", "  Call first  ");
        assertEquals("  Call first  ", contact.getNote());
    }

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Contact(null, "GP", "123", ""));
        assertThrows(NullPointerException.class, () -> new Contact("Clinic", null, "123", ""));
        assertThrows(NullPointerException.class, () -> new Contact("Clinic", "GP", null, ""));
        assertThrows(NullPointerException.class, () -> new Contact("Clinic", "GP", "123", null));
    }

    @Test
    public void constructor_blankNameOrRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Contact.MESSAGE_NAME_CONSTRAINTS, () ->
                new Contact("  ", "GP", "123", ""));
        assertThrows(IllegalArgumentException.class, Contact.MESSAGE_ROLE_CONSTRAINTS, () ->
                new Contact("Clinic", "  ", "123", ""));
    }

    @Test
    public void isValidPhone_internationalAndClinicFormats_checked() {
        for (String phone : List.of("123", "+65 6123-4567", "+44 20 7946 0958", "1800-123-4567")) {
            assertTrue(Contact.isValidPhone(phone));
        }
        for (String phone : List.of("", "  ", "+--", "12", "123 ext 4", "(123)", "１２３")) {
            assertFalse(Contact.isValidPhone(phone));
            assertThrows(IllegalArgumentException.class, Contact.MESSAGE_PHONE_CONSTRAINTS, () ->
                    new Contact("Clinic", "GP", phone, ""));
        }
    }

    @Test
    public void isSameContact_nameAndPhone_normalizedIdentity() {
        assertTrue(GP.isSameContact(new Contact("TAN   & PARTNERS CLINIC", "Emergency", "+6561234567", "Different")));
        assertFalse(GP.isSameContact(new Contact(GP.getName(), "GP", "6561234567", "")));
        assertFalse(GP.isSameContact(GUARDIAN));
        assertFalse(GP.isSameContact(null));
    }

    @Test
    public void equals_allFields_compared() {
        assertTrue(GP.equals(GP));
        assertFalse(GP.equals(null));
        assertFalse(GP.equals("contact"));
        assertFalse(GP.equals(new Contact("Other", GP.getRole(), GP.getPhone(), GP.getNote())));
        assertFalse(GP.equals(new Contact(GP.getName(), "Other", GP.getPhone(), GP.getNote())));
        assertFalse(GP.equals(new Contact(GP.getName(), GP.getRole(), "123", GP.getNote())));
        assertFalse(GP.equals(new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "Other")));
    }

    @Test
    public void toString_withAndWithoutNote_showsContactFields() {
        assertEquals("Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning", GP.toString());
        assertEquals("Mary Tan (Guardian); Phone: +44 20 7946 0958", GUARDIAN.toString());
    }
}
