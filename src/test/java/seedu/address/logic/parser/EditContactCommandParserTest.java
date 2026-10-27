package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditContactCommand;
import seedu.address.model.patient.Contact;

public class EditContactCommandParserTest {

    private final EditContactCommandParser parser = new EditContactCommandParser();

    @Test
    public void parse_allFieldsOrEmptyNote_preserved() {
        assertParseSuccess(parser, " 1 1 n/Clinic r/Emergency p/+44 123 nt/Call first",
                new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of("Clinic"),
                        Optional.of("Emergency"), Optional.of("+44 123"), Optional.of("Call first")));
        assertParseSuccess(parser, " 1   1 nt/",
                new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("")));
    }

    @Test
    public void parse_note_trimmedLikeAppointmentNotes() {
        assertParseSuccess(parser, " 1 1 nt/  Call first  ",
                new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT,
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("Call first")));
        assertParseSuccess(parser, " 1 1 nt/\u2003Call first\u2003",
                new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.of("\u2003Call first\u2003")));
    }

    @Test
    public void parse_indianNameSlashWords_success() {
        for (String token : List.of("a/l", "a/p", "A/L", "A/P")) {
            String name = "Muthu " + token + " Ramasamy";

            assertParseSuccess(parser, " 1 1 n/" + name,
                    new EditContactCommand(INDEX_FIRST_PATIENT, INDEX_FIRST_PATIENT, Optional.of(name),
                            Optional.empty(), Optional.empty(), Optional.empty()));
        }
    }

    @Test
    public void parse_missingFieldsOrInvalidIndices_rejected() {
        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditContactCommand.MESSAGE_USAGE);
        for (String args : List.of("", " 1 nt/note", " 1 1 1 nt/note", " 0 1 nt/note", " 1 0 nt/note",
                " -1 1 nt/note", " 1 x nt/note", " 1 2147483648 nt/note")) {
            assertParseFailure(parser, args, usage);
        }
        assertParseFailure(parser, " 1 1", EditContactCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_invalidFields_rejected() {
        assertParseFailure(parser, " 1 1 n/", Contact.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, " 1 1 r/", Contact.MESSAGE_ROLE_CONSTRAINTS);
        assertParseFailure(parser, " 1 1 p/12", Contact.MESSAGE_PHONE_CONSTRAINTS);
        assertParseFailure(parser, " 1 1 p/", Contact.MESSAGE_PHONE_CONSTRAINTS);
    }

    @Test
    public void parse_malformedOrOverlongPhone_rejected() {
        for (String phone : List.of("+++123+++", "123+456", "1234567890123456")) {
            assertParseFailure(parser, " 1 1 p/" + phone, Contact.MESSAGE_PHONE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicateAndUnknownPrefixes_rejected() {
        for (Prefix prefix : List.of(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ROLE,
                CliSyntax.PREFIX_PHONE, CliSyntax.PREFIX_NOTE)) {
            assertParseFailure(parser, " 1 1 " + prefix + "first " + prefix + "second",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
        }
        assertParseFailure(parser, " 1 1 x/value", String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/"));
        assertParseFailure(parser, " 1 1 nt/Ask d/o Clinic",
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "d/"));
    }
}
