package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;

import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.Patient;

/**
 * Parses input arguments and creates a new {@code AddCareCommand} object.
 */
public class AddCareCommandParser implements Parser<AddCareCommand> {

    private static final Set<String> ALLOWED_SLASH_TOKENS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p", "w/o");

    @Override
    public AddCareCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, ALLOWED_SLASH_TOKENS, PREFIX_MEDICATION, PREFIX_ALLERGEN,
                PREFIX_CARE_INSTRUCTION);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, ALLOWED_SLASH_TOKENS,
                PREFIX_MEDICATION, PREFIX_ALLERGEN, PREFIX_CARE_INSTRUCTION);
        List<String> medications = argMultimap.getAllValues(PREFIX_MEDICATION);
        List<String> allergens = argMultimap.getAllValues(PREFIX_ALLERGEN);
        List<String> careInstructions = argMultimap.getAllValues(PREFIX_CARE_INSTRUCTION);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCareCommand.MESSAGE_USAGE), pe);
        }

        if (medications.isEmpty() && allergens.isEmpty() && careInstructions.isEmpty()) {
            throw new ParseException(AddCareCommand.MESSAGE_NO_CARE_INFO);
        }

        rejectBlankEntries(medications, AddCareCommand.MESSAGE_BLANK_MEDICATION);
        rejectBlankCareEntries(allergens, AddCareCommand.MESSAGE_BLANK_ALLERGEN);
        rejectBlankCareEntries(careInstructions, AddCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
        if (careInstructions.stream().map(Patient::normalizeCareEntry)
                .anyMatch(entry -> !Patient.isCareInstructionWithinMaxLength(entry))) {
            throw new ParseException(AddCareCommand.MESSAGE_CARE_INSTRUCTION_TOO_LONG);
        }
        return new AddCareCommand(index, medications, allergens, careInstructions);
    }

    private static void rejectBlankEntries(List<String> entries, String message) throws ParseException {
        if (entries.stream().anyMatch(String::isBlank)) {
            throw new ParseException(message);
        }
    }

    private static void rejectBlankCareEntries(List<String> entries, String message) throws ParseException {
        if (entries.stream().map(Patient::normalizeCareEntry).anyMatch(String::isEmpty)) {
            throw new ParseException(message);
        }
    }
}
