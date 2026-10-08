package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code AddCareCommand} object.
 */
public class AddCareCommandParser implements Parser<AddCareCommand> {

    @Override
    public AddCareCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_MEDICATION, PREFIX_ALLERGEN,
                PREFIX_CARE_INSTRUCTION);
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
        rejectBlankEntries(allergens, AddCareCommand.MESSAGE_BLANK_ALLERGEN);
        rejectBlankEntries(careInstructions, AddCareCommand.MESSAGE_BLANK_CARE_INSTRUCTION);
        return new AddCareCommand(index, medications, allergens, careInstructions);
    }

    private static void rejectBlankEntries(List<String> entries, String message) throws ParseException {
        if (entries.stream().anyMatch(String::isBlank)) {
            throw new ParseException(message);
        }
    }
}
