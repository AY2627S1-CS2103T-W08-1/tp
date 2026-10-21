package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.DOB_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NRIC_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DOB_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NRIC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPatientDescriptor;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Phone;
import seedu.address.testutil.EditPatientDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private final EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_allFields_success() {
        String input = "1" + NAME_DESC_BOB + NRIC_DESC_BOB + DOB_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB;
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder().withName(VALID_NAME_BOB)
                .withNric(VALID_NRIC_BOB).withDob(VALID_DOB_BOB).withPhone(VALID_PHONE_BOB)
                .withAddress(VALID_ADDRESS_BOB).build();

        assertParseSuccess(parser, input, new EditCommand(INDEX_FIRST_PATIENT, descriptor));
    }

    @Test
    public void parse_eachField_success() {
        assertParseSuccess(parser, "1" + NAME_DESC_BOB, new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withName(VALID_NAME_BOB).build()));
        assertParseSuccess(parser, "1" + NRIC_DESC_BOB, new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withNric(VALID_NRIC_BOB).build()));
        assertParseSuccess(parser, "1" + DOB_DESC_BOB, new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withDob(VALID_DOB_BOB).build()));
        assertParseSuccess(parser, "1" + PHONE_DESC_BOB, new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withPhone(VALID_PHONE_BOB).build()));
        assertParseSuccess(parser, "1" + ADDRESS_DESC_BOB, new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withAddress(VALID_ADDRESS_BOB).build()));
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        for (String input : new String[] {
            NAME_DESC_BOB, "", "0" + NAME_DESC_BOB, "-1" + NAME_DESC_BOB,
            "abc" + NAME_DESC_BOB, "1 2" + NAME_DESC_BOB
        }) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_noFields_failure() {
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, "1 n/James&", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 ic/not-an-nric", Nric.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 dob/2050-01-01", Dob.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 p/123", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 a/", Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateOrUnknownPrefix_failure() {
        assertParseFailure(parser, "1" + NAME_DESC_BOB + NAME_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, "1 x/value", String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/"));
    }

    @Test
    public void parse_allowedSlashText_success() {
        EditPatientDescriptor descriptor = new EditPatientDescriptorBuilder().withAddress("21 c/o Clinic").build();

        assertParseSuccess(parser, "1 a/21 c/o Clinic", new EditCommand(INDEX_FIRST_PATIENT, descriptor));
    }
}
