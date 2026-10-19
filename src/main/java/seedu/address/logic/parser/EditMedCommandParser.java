package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_NO_MEDICATION_FIELD_TO_EDIT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditMedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code EditMedCommand} object.
 */
public class EditMedCommandParser implements Parser<EditMedCommand> {

    private static final int EXPECTED_INDEX_COUNT = 2;
    private static final int PATIENT_INDEX_POSITION = 0;
    private static final int MEDICATION_INDEX_POSITION = 1;
    private static final String WHITESPACE_REGEX = "\\s+";

    /**
     * Parses the given {@code String} of arguments in the context of the {@code EditMedCommand}
     * and returns an {@code EditMedCommand} object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public EditMedCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentTokenizer.verifyNoInvalidPrefixes(args, Set.of(), PREFIX_MEDICATION, PREFIX_TIME);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_MEDICATION, PREFIX_TIME);

        List<Index> indices = parseIndices(argMultimap.getPreamble());
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_MEDICATION);

        List<String> timeValues = argMultimap.getAllValues(PREFIX_TIME);
        Optional<String> description = argMultimap.getValue(PREFIX_MEDICATION);
        if (description.isEmpty() && timeValues.isEmpty()) {
            throw new ParseException(MESSAGE_NO_MEDICATION_FIELD_TO_EDIT);
        }

        return new EditMedCommand(indices.get(PATIENT_INDEX_POSITION), indices.get(MEDICATION_INDEX_POSITION),
                parseOptionalDescription(description), parseOptionalTimes(timeValues));
    }

    /**
     * Parses the preamble, which must be exactly the patient index followed by the medication index.
     *
     * @throws ParseException if the preamble does not consist of exactly two positive integers.
     */
    private static List<Index> parseIndices(String preamble) throws ParseException {
        String[] tokens = preamble.split(WHITESPACE_REGEX);
        if (preamble.isEmpty() || tokens.length != EXPECTED_INDEX_COUNT) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditMedCommand.MESSAGE_USAGE));
        }

        List<Index> indices = new ArrayList<>();
        for (String token : tokens) {
            try {
                indices.add(ParserUtil.parseIndex(token));
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditMedCommand.MESSAGE_USAGE),
                        pe);
            }
        }
        return indices;
    }

    private static Optional<String> parseOptionalDescription(Optional<String> description) throws ParseException {
        if (description.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ParserUtil.parseMedicationDescription(description.get()));
    }

    /**
     * Parses the values of every {@code t/}. No value means the times are not edited; a single blank value
     * means the times are cleared; otherwise every value must be a valid time.
     *
     * @throws ParseException if any time is invalid, including a blank {@code t/} next to other values.
     */
    private static Optional<List<LocalTime>> parseOptionalTimes(List<String> timeValues) throws ParseException {
        if (timeValues.isEmpty()) {
            return Optional.empty();
        }
        if (timeValues.size() == 1 && timeValues.get(0).isBlank()) {
            return Optional.of(List.of());
        }

        List<LocalTime> times = new ArrayList<>();
        for (String timeValue : timeValues) {
            times.add(ParserUtil.parseTime(timeValue));
        }
        return Optional.of(times);
    }
}
