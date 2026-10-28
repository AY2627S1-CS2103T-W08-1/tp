package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalContacts.GP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddContactCommand;
import seedu.address.model.patient.Contact;

public class AddContactCommandParserTest {

    private static final String FIELDS = " n/Tan & Partners Clinic r/GP p/+65 6123-4567";
    private final AddContactCommandParser parser = new AddContactCommandParser();

    @Test
    public void parse_textLength_acceptsLimitsAndRejectsOverlongFields() {
        String name = "\uD83D\uDE00".repeat(Contact.MAX_NAME_LENGTH);
        String role = "r".repeat(Contact.MAX_ROLE_LENGTH);
        String note = "n".repeat(Contact.MAX_NOTE_LENGTH);
        assertParseSuccess(parser, " 1 n/" + name + " r/" + role + " p/123 nt/\u00a0" + note + "\u00a0",
                new AddContactCommand(INDEX_FIRST_PATIENT, new Contact(name, role, "123", note)));
        assertParseFailure(parser, " 1 n/" + name + "x r/GP p/123", Contact.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, " 1 n/Clinic r/" + role + "x p/123", Contact.MESSAGE_ROLE_CONSTRAINTS);
        assertParseFailure(parser, " 1" + FIELDS + " nt/" + note + "x", Contact.MESSAGE_NOTE_CONSTRAINTS);
    }

    @Test
    public void parse_validFields_contactCreated() {
        assertParseSuccess(parser, " 1" + FIELDS + " nt/Call in the morning",
                new AddContactCommand(INDEX_FIRST_PATIENT, GP));
        Contact withoutNote = new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "");
        assertParseSuccess(parser, " 1" + FIELDS, new AddContactCommand(INDEX_FIRST_PATIENT, withoutNote));
        assertParseSuccess(parser, " 1 p/+65 6123-4567 r/GP n/Tan & Partners Clinic nt/",
                new AddContactCommand(INDEX_FIRST_PATIENT, withoutNote));
    }

    @Test
    public void parse_standaloneSlashWords_preservedInContactFields() {
        for (String token : List.of("s/o", "d/o", "c/o", "a/l", "a/p", "S/O", "D/O", "C/O", "A/L", "A/P")) {
            String name = "Tan " + token + " Lim";
            String role = "Guardian " + token + " family";
            String note = "Call " + token + " daughter";
            Contact contact = new Contact(name, role, "123", note);

            assertParseSuccess(parser, " 1 n/" + name + " r/" + role + " p/123 nt/" + note,
                    new AddContactCommand(INDEX_FIRST_PATIENT, contact));
        }
    }

    @Test
    public void parse_unicodeWhitespace_rejectsBlankNamesAndRolesAndTrimsNotes() {
        for (String space : List.of("\u00a0", "\u202f", "\u2007")) {
            assertParseFailure(parser, " 1 n/" + space + " r/GP p/123", Contact.MESSAGE_NAME_CONSTRAINTS);
            assertParseFailure(parser, " 1 n/Clinic r/" + space + " p/123", Contact.MESSAGE_ROLE_CONSTRAINTS);
            assertParseSuccess(parser, " 1 n/Clinic r/GP p/123 nt/" + space,
                    new AddContactCommand(INDEX_FIRST_PATIENT, new Contact("Clinic", "GP", "123", "")));
        }
    }

    @Test
    public void parse_missingFieldsOrInvalidIndex_rejected() {
        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AddContactCommand.MESSAGE_USAGE);
        for (String args : List.of("", " 1", " 0" + FIELDS, " -1" + FIELDS, " x" + FIELDS,
                " 2147483648" + FIELDS, " 1 2" + FIELDS,
                " 1 r/GP p/123", " 1 n/Clinic p/123", " 1 n/Clinic r/GP")) {
            assertParseFailure(parser, args, usage);
        }
    }

    @Test
    public void parse_invalidFields_rejected() {
        assertParseFailure(parser, " 1 n/ r/GP p/123", Contact.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, " 1 n/Clinic r/ p/123", Contact.MESSAGE_ROLE_CONSTRAINTS);
        assertParseFailure(parser, " 1 n/Clinic r/GP p/12", Contact.MESSAGE_PHONE_CONSTRAINTS);
        assertParseFailure(parser, " 1 n/Clinic r/GP p/", Contact.MESSAGE_PHONE_CONSTRAINTS);
    }

    @Test
    public void parse_malformedOrOverlongPhone_rejected() {
        for (String phone : List.of("+++123+++", "123+456", "1234567890123456")) {
            assertParseFailure(parser, " 1 n/Clinic r/GP p/" + phone, Contact.MESSAGE_PHONE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicateAndUnknownPrefixes_rejected() {
        for (Prefix prefix : List.of(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ROLE,
                CliSyntax.PREFIX_PHONE, CliSyntax.PREFIX_NOTE)) {
            String args = " 1" + FIELDS + " nt/note " + prefix + "again";
            assertParseFailure(parser, args, Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
        assertParseFailure(parser, " 1 x/value" + FIELDS,
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/"));
    }

    @Test
    public void parse_punctuatedSlashWords_rejected() {
        for (String token : List.of("s/o", "d/o", "c/o", "a/l", "a/p")) {
            String expected = String.format(Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, token + ",", token);
            assertParseFailure(parser, " 1 n/Tan " + token + ", Lim r/Guardian p/123", expected);
            assertParseFailure(parser, " 1" + FIELDS + " nt/Call " + token + ", daughter", expected);
        }
    }

    @Test
    public void parse_note_trimsUnicodeWhitespace() {
        assertParseSuccess(parser, " 1" + FIELDS + " nt/  Call in the morning  ",
                new AddContactCommand(INDEX_FIRST_PATIENT, GP));
        Contact contact = new Contact(GP.getName(), GP.getRole(), GP.getPhone(), "Call first");
        assertParseSuccess(parser, " 1" + FIELDS + " nt/\u2003Call first\u2003",
                new AddContactCommand(INDEX_FIRST_PATIENT, contact));
    }
}
