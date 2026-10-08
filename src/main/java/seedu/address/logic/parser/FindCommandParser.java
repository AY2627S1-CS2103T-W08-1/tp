package seedu.address.logic.parser;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.NameContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_NO_KEYWORDS = "Please provide at least one keyword to search for.";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(MESSAGE_NO_KEYWORDS);
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
