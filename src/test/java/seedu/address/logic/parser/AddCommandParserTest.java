package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Name;
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
            String expected = Messages.MESSAGE_DUPLICATE_FIELDS + prefix;
            assertParseFailure(parser, VALID_ARGS + " " + field, expected);
            assertParseFailure(parser, " " + prefix + VALID_ARGS, expected);
            assertParseFailure(parser, VALID_ARGS + " " + prefix, expected);
        }
    }

    @Test
    public void parse_requiredFieldMissing_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        for (String field : List.of("n/John Tan", "ic/S1234567A", "dob/1950-04-12", "p/91234567",
                "a/21 Lorong 3, #05-10")) {
            assertParseFailure(parser, VALID_ARGS.replace(field, ""), expected);
        }
        assertParseFailure(parser, "", expected);
        assertParseFailure(parser, "unexpected" + VALID_ARGS, expected);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, VALID_ARGS.replace("John Tan", "John&"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("91234567", "9123abc"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("21 Lorong 3, #05-10", ""), Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGS.replace("John Tan", "John&").replace("S1234567A", "invalid"),
                Name.MESSAGE_CONSTRAINTS);
    }
}
