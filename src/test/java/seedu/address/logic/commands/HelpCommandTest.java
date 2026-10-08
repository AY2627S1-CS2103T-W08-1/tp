package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.COMMAND_SUMMARY;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void commandSummary_listsEveryAvailableCommandOnce() {
        List<String> commandWords = List.of(AddCommand.COMMAND_WORD, ListCommand.COMMAND_WORD,
                ViewCommand.COMMAND_WORD, DeleteCommand.COMMAND_WORD, FindCommand.COMMAND_WORD,
                ClearCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD);
        List<String> lines = COMMAND_SUMMARY.lines().toList();

        assertEquals(commandWords.size(), lines.size());
        for (int i = 0; i < commandWords.size(); i++) {
            assertTrue(lines.get(i).startsWith(commandWords.get(i) + " "));
        }
    }

    @Test
    public void commandSummary_eachLineHasFormatAndDescription() {
        COMMAND_SUMMARY.lines().forEach(line -> {
            String[] parts = line.split(" : ", 2);
            assertEquals(2, parts.length);
            assertTrue(parts[1].endsWith("."));
        });
    }

    @Test
    public void commandSummary_addLineHasNoTags() {
        assertTrue(COMMAND_SUMMARY.contains("add n/NAME ic/NRIC dob/DOB p/PHONE a/ADDRESS :"));
        assertFalse(COMMAND_SUMMARY.contains("t/TAG"));
    }
}
