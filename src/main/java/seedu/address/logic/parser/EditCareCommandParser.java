package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;

import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCareCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Patient;

/**
 * Parses the patient index, care-instruction index and replacement text of an edit.
 */
public class EditCareCommandParser implements Parser<EditCareCommand> {

    private static final Set<String> ALLOWED_SLASH_TOKENS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p", "w/o");

    @Override
    public EditCareCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, ALLOWED_SLASH_TOKENS,
                Set.of(PREFIX_CARE_INSTRUCTION), PREFIX_CARE_INSTRUCTION);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, ALLOWED_SLASH_TOKENS, PREFIX_CARE_INSTRUCTION);
        fields.verifyNoDuplicatePrefixesFor(PREFIX_CARE_INSTRUCTION);

        List<Index> indices;
        try {
            indices = ParserUtil.parseTwoIndexes(fields.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCareCommand.MESSAGE_USAGE), e);
        }

        String careInstruction = fields.getValue(PREFIX_CARE_INSTRUCTION).orElseThrow(() ->
                new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCareCommand.MESSAGE_USAGE)));
        if (careInstruction.isBlank()) {
            throw new ParseException(EditCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
        }
        String normalizedInstruction = careInstruction.trim().replaceAll("\\s+", " ");
        if (!Patient.isCareInstructionWithinMaxLength(normalizedInstruction)) {
            throw new ParseException(EditCareCommand.MESSAGE_CARE_INSTRUCTION_TOO_LONG);
        }
        return new EditCareCommand(indices.get(0), indices.get(1), careInstruction);
    }
}
