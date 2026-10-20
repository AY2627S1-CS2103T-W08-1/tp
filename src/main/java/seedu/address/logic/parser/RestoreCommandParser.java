package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new RestoreCommand object.
 */
public class RestoreCommandParser implements Parser<RestoreCommand> {

    /** Characters that let a file name point outside the backup folder on at least one operating system. */
    private static final List<String> PATH_CHARACTERS = List.of("/", "\\", ":");

    /**
     * Parses the given {@code String} of arguments in the context of the RestoreCommand
     * and returns a RestoreCommand object for execution.
     *
     * @throws ParseException If the user input does not conform to the expected format.
     */
    @Override
    public RestoreCommand parse(String args) throws ParseException {
        String fileName = args.trim();
        if (fileName.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RestoreCommand.MESSAGE_USAGE));
        }
        if (!isPlainFileName(fileName)) {
            throw new ParseException(RestoreCommand.MESSAGE_INVALID_FILE_NAME);
        }
        return new RestoreCommand(fileName);
    }

    /**
     * Returns true if {@code fileName} is only the name of a file, with no folder in it, on every operating system.
     */
    private static boolean isPlainFileName(String fileName) {
        if (fileName.equals(".") || fileName.equals("..")
                || PATH_CHARACTERS.stream().anyMatch(fileName::contains)) {
            return false;
        }

        try {
            // the operating system rejects some characters, e.g., * and ? on Windows
            Paths.get(fileName);
            return true;
        } catch (InvalidPathException ipe) {
            return false;
        }
    }
}
