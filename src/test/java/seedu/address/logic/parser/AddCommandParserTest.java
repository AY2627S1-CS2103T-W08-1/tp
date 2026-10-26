package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Phone;
import seedu.address.testutil.PatientBuilder;

public class AddCommandParserTest {

    private static final String VALID_ARGS =
            " n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/21 Lorong 3, #05-10";
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        AddCommand expected = new AddCommand(new PatientBuilder().withName("John Tan").withNric("S1234567A")
                .withDob("1950-04-12").withPhone("91234567").withAddress("21 Lorong 3, #05-10").build());
        assertParseSuccess(parser, VALID_ARGS, expected);
        assertParseSuccess(parser, " \t" + VALID_ARGS, expected);
        assertParseSuccess(parser,
                " a/21 Lorong 3, #05-10 p/91234567 dob/1950-04-12 ic/S1234567A n/John Tan", expected);
    }

    @Test
    public void parse_secondSpecExample_success() {
        assertParseSuccess(parser, " n/Mary Lim ic/S7654321B dob/1945-11-02 p/98765432 a/Blk 12 Ang Mo Kio Ave 4",
                new AddCommand(new PatientBuilder().withName("Mary Lim").withNric("S7654321B")
                        .withDob("1945-11-02").withPhone("98765432").withAddress("Blk 12 Ang Mo Kio Ave 4").build()));
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        for (String field : List.of("n/John Tan", "ic/S1234567A", "dob/1950-04-12", "p/91234567", "a/21 Lorong 3")) {
            String prefix = field.substring(0, field.indexOf('/') + 1);
            String expected = "Multiple values specified for a single-valued field: " + prefix;
            assertParseFailure(parser, VALID_ARGS + " " + field, expected);
            assertParseFailure(parser, " " + prefix + VALID_ARGS, expected);
            assertParseFailure(parser, VALID_ARGS + " " + prefix, expected);
        }
    }

    @Test
    public void parse_requiredFieldMissing_failure() {
        String expected = "Invalid command format!\nadd: Adds a patient to the list. "
                + "Parameters: n/NAME ic/NRIC dob/DOB p/PHONE a/ADDRESS\n"
                + "Example: add n/John Tan ic/S1234567A dob/1950-04-12 p/91234567 a/21 Lorong 3";
        for (String field : List.of("n/John Tan", "ic/S1234567A", "dob/1950-04-12", "p/91234567",
                "a/21 Lorong 3, #05-10")) {
            assertParseFailure(parser, VALID_ARGS.replace(field, ""), expected);
        }
        assertParseFailure(parser, "", expected);
        assertParseFailure(parser, "unexpected" + VALID_ARGS, expected);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/");
        assertParseFailure(parser, " x/123" + VALID_ARGS, expected);
        assertParseFailure(parser, VALID_ARGS.replace(" dob/", " x/123 dob/"), expected);
        assertParseFailure(parser, VALID_ARGS + " x/123", expected);
        assertParseFailure(parser, " x/123", expected);
        assertParseFailure(parser, VALID_ARGS + " med/Aspirin",
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "med/"));
        assertParseFailure(parser, VALID_ARGS + " A/B",
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "A/"));
    }

    @Test
    public void parse_punctuatedSlashToken_failure() {
        for (String token : List.of("s/o,", "d/o,", "c/o,", "a/l,", "a/p,", "S/O.")) {
            String unpunctuatedToken = token.substring(0, token.length() - 1);
            String expected = String.format(
                    Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, token, unpunctuatedToken);
            assertParseFailure(parser, VALID_ARGS.replace("John Tan", "Ben " + token + " Ng"), expected);
            assertParseFailure(parser, VALID_ARGS + " " + token, expected);
        }
    }

    @Test
    public void parse_allowedSlashTokenInName_success() {
        for (String token : List.of("s/o", "d/o", "a/l", "a/p", "S/O", "D/O", "A/L", "A/P")) {
            String name = "John Tan " + token + " Lim";
            AddCommand expected = new AddCommand(new PatientBuilder().withName(name).withNric("S1234567A")
                    .withDob("1950-04-12").withPhone("91234567").withAddress("21 Lorong 3, #05-10").build());
            assertParseSuccess(parser, VALID_ARGS.replace("John Tan", name), expected);
        }
    }

    @Test
    public void parse_slashTextInAddress_success() {
        for (String address : List.of("Blk 21/3 Ang Mo Kio", "c/o 12 Bedok North", "C/O 12 Bedok North",
                "https://example.com", "Block x1/123", "/hello")) {
            AddCommand expected = new AddCommand(new PatientBuilder().withName("John Tan").withNric("S1234567A")
                    .withDob("1950-04-12").withPhone("91234567").withAddress(address).build());
            assertParseSuccess(parser, VALID_ARGS.replace("21 Lorong 3, #05-10", address), expected);
        }
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, VALID_ARGS.replace("John Tan", "John&"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("S1234567A", "A1234567B"), Nric.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("1950-04-12", "2021-02-30"), Dob.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("91234567", "9123abc"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("21 Lorong 3, #05-10", ""), Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("John Tan", "John&").replace("S1234567A", "invalid"),
                Name.MESSAGE_CONSTRAINTS);
    }
}
