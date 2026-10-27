package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalContacts.GP;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Contact;

public class JsonAdaptedContactTest {

    @Test
    public void toModelType_validFields_preserved() throws Exception {
        assertEquals(GP, new JsonAdaptedContact(GP).toModelType());
        Contact withoutNote = new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "");
        assertEquals(withoutNote,
                new JsonAdaptedContact(GP.getName(), GP.getRole(), GP.getPhone(), null).toModelType());
    }

    @Test
    public void toModelType_note_preservesStoredText() throws Exception {
        Contact contact = new Contact("Clinic", "GP", "123", "  Call first  ");
        assertEquals(contact, new JsonAdaptedContact(contact).toModelType());
    }

    @Test
    public void toModelType_invalidFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Contact.MESSAGE_NAME_CONSTRAINTS, () ->
                new JsonAdaptedContact(null, "GP", "123", "").toModelType());
        assertThrows(IllegalValueException.class, Contact.MESSAGE_ROLE_CONSTRAINTS, () ->
                new JsonAdaptedContact("Clinic", null, "123", "").toModelType());
        assertThrows(IllegalValueException.class, Contact.MESSAGE_PHONE_CONSTRAINTS, () ->
                new JsonAdaptedContact("Clinic", "GP", null, "").toModelType());
        assertThrows(IllegalValueException.class, Contact.MESSAGE_NAME_CONSTRAINTS, () ->
                new JsonAdaptedContact(" ", "GP", "123", "").toModelType());
        assertThrows(IllegalValueException.class, Contact.MESSAGE_ROLE_CONSTRAINTS, () ->
                new JsonAdaptedContact("Clinic", " ", "123", "").toModelType());
        assertThrows(IllegalValueException.class, Contact.MESSAGE_PHONE_CONSTRAINTS, () ->
                new JsonAdaptedContact("Clinic", "GP", "12", "").toModelType());
    }
    @Test
    public void toModelType_malformedOrOverlongPhone_throwsIllegalValueException() {
        for (String phone : List.of("+++123+++", "123+456", "1234567890123456")) {
            assertThrows(IllegalValueException.class, Contact.MESSAGE_PHONE_CONSTRAINTS, () ->
                    new JsonAdaptedContact("Clinic", "GP", phone, "").toModelType());
        }
    }

}
