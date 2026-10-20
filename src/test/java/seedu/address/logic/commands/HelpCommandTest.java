package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.COMMAND_SUMMARY;
import static seedu.address.logic.commands.HelpCommand.FORMAT_DESCRIPTION_SEPARATOR;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALLERGEN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARE_INSTRUCTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NRIC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class HelpCommandTest {

    /** Command words in the order the help window lists them, mapped to the command each one creates. */
    private static final Map<String, Class<? extends Command>> EXPECTED_COMMANDS = createExpectedCommands();

    /** Valid sample values that replace the placeholders of a command format to give a valid input. */
    private static final Map<String, String> PLACEHOLDER_VALUES = Map.ofEntries(
            Map.entry("ALLERGY_INDEX", "1"),
            Map.entry("CONTACT_INDEX", "1"),
            Map.entry("CI_INDEX", "1"),
            Map.entry("ROLE", "GP"),
            Map.entry("NOTE", "Call first"),
            Map.entry("NAME", "John Tan"),
            Map.entry("NRIC", "S1234567A"),
            Map.entry("DOB", "1950-04-12"),
            Map.entry("PHONE", "91234567"),
            Map.entry("ADDRESS", "21 Lorong 3"),
            Map.entry("INDEX", "1"),
            Map.entry("MEDICATION", "Metformin 500mg"),
            Map.entry("ALLERGEN", "Penicillin"),
            Map.entry("CARE_INSTRUCTION", "Check blood sugar"),
            Map.entry("KEYWORD", "alice"),
            Map.entry("MORE_KEYWORDS", "bob"),
            Map.entry("FILENAME", "mediconnect-20261020-083015.json"));

    private static Map<String, Class<? extends Command>> createExpectedCommands() {
        Map<String, Class<? extends Command>> commands = new LinkedHashMap<>();
        commands.put(AddCommand.COMMAND_WORD, AddCommand.class);
        commands.put(ListCommand.COMMAND_WORD, ListCommand.class);
        commands.put(DeleteCommand.COMMAND_WORD, DeleteCommand.class);
        commands.put(AddCareCommand.COMMAND_WORD, AddCareCommand.class);
        commands.put(EditCareCommand.COMMAND_WORD, EditCareCommand.class);
        commands.put(DeleteCareCommand.COMMAND_WORD, DeleteCareCommand.class);
        commands.put(DeleteAllergyCommand.COMMAND_WORD, DeleteAllergyCommand.class);
        commands.put(AddContactCommand.COMMAND_WORD, AddContactCommand.class);
        commands.put(EditContactCommand.COMMAND_WORD, EditContactCommand.class);
        commands.put(DeleteContactCommand.COMMAND_WORD, DeleteContactCommand.class);
        commands.put(ViewCommand.COMMAND_WORD, ViewCommand.class);
        commands.put(DueCommand.COMMAND_WORD, DueCommand.class);
        commands.put(FindCommand.COMMAND_WORD, FindCommand.class);
        commands.put(HelpCommand.COMMAND_WORD, HelpCommand.class);
        commands.put(ClearCommand.COMMAND_WORD, ClearCommand.class);
        commands.put(BackupCommand.COMMAND_WORD, BackupCommand.class);
        commands.put(RestoreCommand.COMMAND_WORD, RestoreCommand.class);
        commands.put(ExitCommand.COMMAND_WORD, ExitCommand.class);
        return Collections.unmodifiableMap(commands);
    }

    @Test
    public void execute_emptyModel_showsHelpAndModelUnchanged() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);

        assertCommandSuccess(new HelpCommand(), new ModelManager(), expectedCommandResult,
                new ModelManager());
    }

    @Test
    public void execute_nonEmptyModel_showsHelpAndModelUnchanged() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);

        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_help_doesNotExit() {
        CommandResult result = new HelpCommand().execute(new ModelManager());

        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void commandSummary_everyCommand_listedOnceInOrder() {
        List<String> commandWords = getLines().stream()
                .map(line -> line.split(" ", 2)[0])
                .collect(Collectors.toList());

        assertEquals(List.copyOf(EXPECTED_COMMANDS.keySet()), commandWords);
    }

    @Test
    public void commandSummary_eachLine_hasFormatAndDescription() {
        for (String line : getLines()) {
            String[] parts = line.split(Pattern.quote(FORMAT_DESCRIPTION_SEPARATOR), -1);

            assertEquals(2, parts.length, "Expected one separator in: " + line);
            assertFalse(parts[0].isBlank(), "Missing format in: " + line);
            assertTrue(Character.isUpperCase(parts[1].charAt(0)),
                    "Description should be a sentence in: " + line);
            assertTrue(parts[1].endsWith("."), "Description should end with a full stop in: " + line);
        }
    }

    @Test
    public void commandSummary_eachLine_notBlankOrPadded() {
        for (String line : getLines()) {
            assertFalse(line.isBlank());
            assertEquals(line.strip(), line);
        }
    }

    @Test
    public void commandSummary_addFormat_listsEveryRequiredFieldInOrder() {
        String expectedFormat = AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_NRIC + "NRIC "
                + PREFIX_DOB + "DOB " + PREFIX_PHONE + "PHONE " + PREFIX_ADDRESS + "ADDRESS";

        assertEquals(expectedFormat, getFormat(AddCommand.COMMAND_WORD));
    }

    @Test
    public void commandSummary_addCareFormat_showsEveryCareFieldAsOptionalAndRepeatable() {
        String format = getFormat(AddCareCommand.COMMAND_WORD);

        assertTrue(format.contains("[" + PREFIX_MEDICATION + "MEDICATION]..."));
        assertTrue(format.contains("[" + PREFIX_ALLERGEN + "ALLERGEN]..."));
        assertTrue(format.contains("[" + PREFIX_CARE_INSTRUCTION + "CARE_INSTRUCTION]..."));
        assertTrue(getDescription(AddCareCommand.COMMAND_WORD).contains("at least one"));
    }

    @Test
    public void commandSummary_indexCommands_takeIndex() {
        for (String commandWord : List.of(DeleteCommand.COMMAND_WORD, ViewCommand.COMMAND_WORD,
                AddCareCommand.COMMAND_WORD, EditCareCommand.COMMAND_WORD, DeleteCareCommand.COMMAND_WORD,
                DeleteAllergyCommand.COMMAND_WORD)) {
            assertTrue(getFormat(commandWord).startsWith(commandWord + " INDEX"));
            assertTrue(getDescription(commandWord).contains("INDEX"));
        }
    }

    @Test
    public void commandSummary_commandsWithoutParameters_showOnlyCommandWord() {
        for (String commandWord : List.of(ListCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
                ClearCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD)) {
            assertEquals(commandWord, getFormat(commandWord));
        }
    }

    @Test
    public void commandSummary_ab3Terms_notMentioned() {
        String summary = COMMAND_SUMMARY.toLowerCase();

        for (String leftover : List.of("person", "address book", "t/tag", "e/email")) {
            assertFalse(summary.contains(leftover), "Summary should not mention: " + leftover);
        }
        assertFalse(getLines().stream().anyMatch(line -> line.startsWith(EditCommand.COMMAND_WORD + " ")));
    }

    @Test
    public void commandSummary_everyFormatWithSampleValues_isAcceptedByParser() throws ParseException {
        AddressBookParser parser = new AddressBookParser();

        for (Map.Entry<String, Class<? extends Command>> entry : EXPECTED_COMMANDS.entrySet()) {
            String input = toSampleInput(getFormat(entry.getKey()));

            assertEquals(entry.getValue(), parser.parseCommand(input).getClass(),
                    "Input from help: " + input);
        }
    }

    @Test
    public void toSampleInput_optionalAndRepeatedParts_filledWithSampleValues() {
        assertEquals("find alice bob", toSampleInput("find KEYWORD [MORE_KEYWORDS]..."));
        assertEquals("addcare 1 med/Metformin 500mg al/Penicillin ci/Check blood sugar",
                toSampleInput("addcare INDEX [med/MEDICATION]... [al/ALLERGEN]... [ci/CARE_INSTRUCTION]..."));
    }

    private static List<String> getLines() {
        return COMMAND_SUMMARY.lines().collect(Collectors.toList());
    }

    /**
     * Returns the line of {@code COMMAND_SUMMARY} for {@code commandWord}.
     */
    private static String getLine(String commandWord) {
        return getLines().stream()
                .filter(line -> line.startsWith(commandWord + " "))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No help line for: " + commandWord));
    }

    private static String getFormat(String commandWord) {
        return getLine(commandWord).split(Pattern.quote(FORMAT_DESCRIPTION_SEPARATOR))[0];
    }

    private static String getDescription(String commandWord) {
        return getLine(commandWord).split(Pattern.quote(FORMAT_DESCRIPTION_SEPARATOR))[1];
    }

    /**
     * Returns {@code format} as a valid user input, with optional and repeatable parts included once
     * and each placeholder replaced by its sample value.
     */
    private static String toSampleInput(String format) {
        String input = format.replace("[", "").replace("]", "").replace("...", "");
        List<String> placeholdersByLength = PLACEHOLDER_VALUES.keySet().stream()
                .sorted((first, second) -> Integer.compare(second.length(), first.length()))
                .toList();
        for (String placeholder : placeholdersByLength) {
            input = input.replaceAll("\\b" + placeholder + "\\b", PLACEHOLDER_VALUES.get(placeholder));
        }
        return input;
    }
}
