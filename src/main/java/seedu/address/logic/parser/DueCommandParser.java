package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.Clock;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DueCommand} object.
 * The patient index is optional; the command takes the current time from the system clock.
 */
public class DueCommandParser implements Parser<DueCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code DueCommand}
     * and returns a {@code DueCommand} object for execution.
     *
     * @throws ParseException if the arguments are neither empty nor a single positive integer.
     */
    @Override
    public DueCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Clock clock = Clock.systemDefaultZone();
        if (args.isBlank()) {
            return new DueCommand(clock);
        }

        try {
            Index index = ParserUtil.parseIndex(args);
            return new DueCommand(index, clock);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DueCommand.MESSAGE_USAGE), pe);
        }
    }
}
