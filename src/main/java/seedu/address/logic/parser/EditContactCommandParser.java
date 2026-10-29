package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.EditContactCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Contact;

/**
 * Parses optional replacement fields, keeping an empty note distinct from an omitted one.
 */
public class EditContactCommandParser implements Parser<EditContactCommand> {

    private static final Set<String> ALLOWED_SLASH_TOKENS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p");

    @Override
    public EditContactCommand parse(String args) throws ParseException {
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, ALLOWED_SLASH_TOKENS,
                Set.of(PREFIX_NAME, PREFIX_ROLE, PREFIX_NOTE),
                PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args,
                PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        fields.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE);
        List<Index> indices;
        try {
            indices = ParserUtil.parseTwoIndexes(fields.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditContactCommand.MESSAGE_USAGE), e);
        }
        if (Stream.of(PREFIX_NAME, PREFIX_ROLE, PREFIX_PHONE, PREFIX_NOTE)
                .noneMatch(p -> fields.getValue(p).isPresent())) {
            throw new ParseException(EditContactCommand.MESSAGE_NOT_EDITED);
        }
        validateField(fields.getValue(PREFIX_NAME), Contact::isValidName, Contact.MESSAGE_NAME_CONSTRAINTS);
        validateField(fields.getValue(PREFIX_ROLE), Contact::isValidRole, Contact.MESSAGE_ROLE_CONSTRAINTS);
        validateField(fields.getValue(PREFIX_PHONE), Contact::isValidPhone, Contact.MESSAGE_PHONE_CONSTRAINTS);
        Optional<String> note = fields.getValue(PREFIX_NOTE).map(StringUtil::stripUnicodeWhitespace);
        validateField(note, Contact::isValidNote, Contact.MESSAGE_NOTE_CONSTRAINTS);
        return new EditContactCommand(indices.get(0), indices.get(1), fields.getValue(PREFIX_NAME),
                fields.getValue(PREFIX_ROLE), fields.getValue(PREFIX_PHONE), note);
    }

    private static void validateField(Optional<String> value, Predicate<String> validator, String message)
            throws ParseException {
        if (value.isPresent() && !validator.test(value.get())) {
            throw new ParseException(message);
        }
    }
}
