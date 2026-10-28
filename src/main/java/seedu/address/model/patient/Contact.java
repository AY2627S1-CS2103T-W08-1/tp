package seedu.address.model.patient;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.StringUtil;

/**
 * An immutable person or organization to contact about a patient.
 */
public class Contact {

    public static final String MESSAGE_NAME_CONSTRAINTS = "Contact name cannot be blank.";
    public static final String MESSAGE_ROLE_CONSTRAINTS = "Contact role cannot be blank.";
    public static final String MESSAGE_PHONE_CONSTRAINTS =
            "Contact phone must contain 3 to 15 digits, with spaces and hyphens allowed, "
            + "and at most one + at the start.";
    public static final String MESSAGE_DUPLICATE = "This contact (identified by name and phone) already exists.";

    private static final int MIN_PHONE_DIGITS = 3;
    private static final int MAX_PHONE_DIGITS = 15;
    private static final String PHONE_REGEX = "\\+?[0-9 -]+";
    private static final String WHITESPACE_REGEX = "(?U)\\s+";

    private final String name;
    private final String role;
    private final String phone;
    private final String note;

    /**
     * Creates a contact. An empty note means no note.
     * Names allow organizations such as "Tan & Partners Clinic", so patient Name rules do not apply.
     */
    public Contact(String name, String role, String phone, String note) {
        requireAllNonNull(name, role, phone, note);
        checkArgument(isValidName(name), MESSAGE_NAME_CONSTRAINTS);
        checkArgument(isValidRole(role), MESSAGE_ROLE_CONSTRAINTS);
        checkArgument(isValidPhone(phone), MESSAGE_PHONE_CONSTRAINTS);
        this.name = name.replaceAll(WHITESPACE_REGEX, " ").strip();
        this.role = StringUtil.stripUnicodeWhitespace(role);
        this.phone = phone.strip();
        this.note = note;
    }

    /**
     * Returns the contact's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the contact's role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the contact's phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the contact's note.
     */
    public String getNote() {
        return note;
    }

    /**
     * Returns whether a contact name is nonblank.
     */
    public static boolean isValidName(String name) {
        return !StringUtil.stripUnicodeWhitespace(name).isEmpty();
    }

    /**
     * Returns whether a contact role is nonblank.
     */
    public static boolean isValidRole(String role) {
        return !StringUtil.stripUnicodeWhitespace(role).isEmpty();
    }

    /**
     * Returns whether a phone has 3 to 15 digits, optional separators, and at most one leading plus sign.
     */
    public static boolean isValidPhone(String phone) {
        long digitCount = phone.chars().filter(Character::isDigit).count();
        return phone.strip().matches(PHONE_REGEX)
                && digitCount >= MIN_PHONE_DIGITS && digitCount <= MAX_PHONE_DIGITS;
    }

    /**
     * Compares names ignoring case, and phones ignoring spaces and hyphens.
     * The plus sign is significant; no country code is inferred.
     */
    public boolean isSameContact(Contact other) {
        return other != null && name.equalsIgnoreCase(other.name)
                && getComparisonPhone().equals(other.getComparisonPhone());
    }

    private String getComparisonPhone() {
        return phone.replace(" ", "").replace("-", "");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Contact otherContact)) {
            return false;
        }

        return name.equals(otherContact.name)
                && role.equals(otherContact.role)
                && phone.equals(otherContact.phone)
                && note.equals(otherContact.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, role, phone, note);
    }

    @Override
    public String toString() {
        return name + " (" + role + "); Phone: " + phone + (note.isEmpty() ? "" : "; Note: " + note);
    }
}
