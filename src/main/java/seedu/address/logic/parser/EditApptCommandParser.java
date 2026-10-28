package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WITH;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditApptCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code EditApptCommand} object.
 */
public class EditApptCommandParser implements Parser<EditApptCommand> {

    /*
     * Words kept as text, as in names, and "w/o" (without). "w/o" and "d/o" are not read as the w/ and d/
     * prefixes, e.g. in "nt/MRI w/o contrast".
     */
    private static final Set<String> ALLOWED_SLASH_TOKENS = Set.of("s/o", "d/o", "c/o", "a/l", "a/p", "w/o");

    /**
     * Parses the given {@code String} of arguments in the context of the EditApptCommand
     * and returns an EditApptCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public EditApptCommand parse(String args) throws ParseException {
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, ALLOWED_SLASH_TOKENS,
                Set.of(PREFIX_WITH, PREFIX_NOTE),
                PREFIX_DATE, PREFIX_TIME, PREFIX_WITH, PREFIX_NOTE);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, ALLOWED_SLASH_TOKENS,
                        PREFIX_DATE, PREFIX_TIME, PREFIX_WITH, PREFIX_NOTE);

        List<Index> indexes;
        try {
            indexes = ParserUtil.parseTwoIndexes(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditApptCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TIME, PREFIX_WITH, PREFIX_NOTE);
        boolean isAnyFieldGiven = Stream.of(PREFIX_DATE, PREFIX_TIME, PREFIX_WITH, PREFIX_NOTE)
                .anyMatch(prefix -> argMultimap.getValue(prefix).isPresent());
        if (!isAnyFieldGiven) {
            throw new ParseException(EditApptCommand.MESSAGE_NOT_EDITED);
        }

        Optional<LocalDate> date = Optional.empty();
        if (argMultimap.getValue(PREFIX_DATE).isPresent()) {
            date = Optional.of(ParserUtil.parseDate(argMultimap.getValue(PREFIX_DATE).get()));
        }
        Optional<LocalTime> time = Optional.empty();
        if (argMultimap.getValue(PREFIX_TIME).isPresent()) {
            time = Optional.of(ParserUtil.parseTime(argMultimap.getValue(PREFIX_TIME).get()));
        }
        Optional<String> provider = Optional.empty();
        if (argMultimap.getValue(PREFIX_WITH).isPresent()) {
            provider = Optional.of(ParserUtil.parseProvider(argMultimap.getValue(PREFIX_WITH).get()));
        }
        Optional<String> note = Optional.empty();
        if (argMultimap.getValue(PREFIX_NOTE).isPresent()) {
            note = Optional.of(ParserUtil.parseNote(argMultimap.getValue(PREFIX_NOTE).get()));
        }

        return new EditApptCommand(indexes.get(0), indexes.get(1), date, time, provider, note);
    }
}
