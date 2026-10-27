package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListBackupsCommand;
import seedu.address.logic.commands.RestoreCommand;

public class RestoreCommandParserTest {

    private static final String FILE_NAME = "mediconnect-20261020-083015.json";

    private final RestoreCommandParser parser = new RestoreCommandParser();

    @Test
    public void parse_fileName_returnsRestoreCommand() {
        assertParseSuccess(parser, FILE_NAME, new RestoreCommand(FILE_NAME));
    }

    @Test
    public void parse_fileNameWithSurroundingSpaces_spacesRemoved() {
        assertParseSuccess(parser, "  " + FILE_NAME + " \n", new RestoreCommand(FILE_NAME));
    }

    @Test
    public void parse_fileNameWithNoBreakSpacesAround_spacesRemoved() {
        // Equivalence partitioning: a name pasted with no-break spaces, e.g., from a chat app
        assertParseSuccess(parser, " " + FILE_NAME + " ", new RestoreCommand(FILE_NAME));
    }

    @Test
    public void parse_onlyNoBreakSpaces_returnsListBackupsCommand() {
        // Boundary value analysis: nothing is left of the name once the no-break spaces are removed
        assertParseSuccess(parser, "   ", new ListBackupsCommand());
    }

    @Test
    public void parse_fileNameWithInnerSpace_spaceKept() {
        assertParseSuccess(parser, " my backup.json", new RestoreCommand("my backup.json"));
    }

    @Test
    public void parse_noFileName_returnsListBackupsCommand() {
        assertParseSuccess(parser, "", new ListBackupsCommand());
        assertParseSuccess(parser, "   ", new ListBackupsCommand());
    }

    @Test
    public void parse_pathInsteadOfFileName_throwsParseException() {
        // folders, both slash styles, an absolute path and a Windows drive
        for (String path : List.of("data/backups/" + FILE_NAME, "../" + FILE_NAME, "..\\" + FILE_NAME,
                "/" + FILE_NAME, "C:" + FILE_NAME)) {
            assertParseFailure(parser, path, RestoreCommand.MESSAGE_INVALID_FILE_NAME);
        }
    }

    @Test
    public void parse_currentOrParentFolder_throwsParseException() {
        assertParseFailure(parser, ".", RestoreCommand.MESSAGE_INVALID_FILE_NAME);
        assertParseFailure(parser, "..", RestoreCommand.MESSAGE_INVALID_FILE_NAME);
    }

    @Test
    public void parse_nameRejectedByOperatingSystem_throwsParseException() {
        // every operating system rejects the null character in file names
        assertParseFailure(parser, "backup\0.json", RestoreCommand.MESSAGE_INVALID_FILE_NAME);
    }
}
