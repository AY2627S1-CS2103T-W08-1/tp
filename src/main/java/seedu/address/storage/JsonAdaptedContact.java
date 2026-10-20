package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.patient.Contact;

/**
 * Jackson-friendly version of a contact.
 */
class JsonAdaptedContact {

    private final String name;
    private final String role;
    private final String phone;
    private final String note;

    /**
     * Creates the stored contact. A missing note means no note.
     */
    @JsonCreator
    public JsonAdaptedContact(@JsonProperty("name") String name, @JsonProperty("role") String role,
            @JsonProperty("phone") String phone, @JsonProperty("note") String note) {
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.note = note;
    }

    /**
     * Copies a model contact for saving.
     */
    public JsonAdaptedContact(Contact contact) {
        this(contact.getName(), contact.getRole(), contact.getPhone(), contact.getNote());
    }

    /**
     * Converts stored values using the same constraints as contact commands.
     */
    public Contact toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(Contact.MESSAGE_NAME_CONSTRAINTS);
        }
        if (role == null) {
            throw new IllegalValueException(Contact.MESSAGE_ROLE_CONSTRAINTS);
        }
        if (phone == null) {
            throw new IllegalValueException(Contact.MESSAGE_PHONE_CONSTRAINTS);
        }
        try {
            return new Contact(name, role, phone, note == null ? "" : note);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
