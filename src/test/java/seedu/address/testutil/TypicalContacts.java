package seedu.address.testutil;

import seedu.address.model.patient.Contact;

/**
 * Contact examples shared by command, storage and display tests.
 */
public class TypicalContacts {

    public static final Contact GP = new Contact("Tan & Partners Clinic", "GP", "+65 6123-4567", "Call in the morning");
    public static final Contact GUARDIAN = new Contact("Mary Tan", "Guardian", "+44 20 7946 0958", "");

    private TypicalContacts() {}
}
