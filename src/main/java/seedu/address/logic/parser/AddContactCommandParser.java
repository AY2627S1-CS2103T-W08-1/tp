package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Contact;

/**
 * Parses the patient index and fields of one new contact.
 */
public class AddContactCommandParser implements Parser<AddContactCommand> {

    private static final Set<String> ALLOWED_SLASH_TOKENS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p");

    @Override
    public AddContactCommand parse(String args) throws ParseException {
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, ALLOWED_SLASH_TOKENS,
                PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args,
                PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        fields.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        if (!Stream.of(PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE).allMatch(p -> fields.getValue(p).isPresent())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddContactCommand.MESSAGE_USAGE));
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(fields.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddContactCommand.MESSAGE_USAGE), e);
        }
        try {
            Contact contact = new Contact(fields.getValue(PREFIX_NAME).get(), fields.getValue(PREFIX_ROLE).get(),
                    fields.getValue(PREFIX_PHONE).get(), fields.getValue(PREFIX_NOTE).orElse("").trim());
            return new AddContactCommand(index, contact);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
