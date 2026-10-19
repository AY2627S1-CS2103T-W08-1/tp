package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAppointments.CHECKUP;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddApptCommand;
import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddContactCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BackupCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteAllergyCommand;
import seedu.address.logic.commands.DeleteCareCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteContactCommand;
import seedu.address.logic.commands.DeleteMedCommand;
import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.commands.EditCareCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditContactCommand;
import seedu.address.logic.commands.EditMedCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListArchivedCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RestoreCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.patient.NameContainsKeywordsPredicate;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.EditPatientDescriptorBuilder;
import seedu.address.testutil.PatientBuilder;
import seedu.address.testutil.PatientUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Patient patient = new PatientBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PatientUtil.getAddCommand(patient));
        assertEquals(new AddCommand(patient), command);
    }

    @Test
    public void parseCommand_addCare() throws Exception {
        AddCareCommand command = (AddCareCommand) parser.parseCommand("addcare 1 med/Metformin al/Penicillin");
        assertEquals(new AddCareCommand(INDEX_FIRST_PATIENT, List.of("Metformin"), List.of("Penicillin"), List.of()),
                command);
    }

    @Test
    public void parseCommand_addAppt() throws Exception {
        AddApptCommand command = (AddApptCommand) parser.parseCommand(
                "addappt 1 d/2026-11-05 t/09:30 w/Dr Tan nt/Fasting");
        assertEquals(new AddApptCommand(INDEX_FIRST_PATIENT, CHECKUP), command);
    }

    @Test
    public void parseCommand_careInstructionCommands_dispatched() throws Exception {
        assertTrue(parser.parseCommand("editcare 1 1 ci/Updated") instanceof EditCareCommand);
        assertTrue(parser.parseCommand("deletecare 1 1") instanceof DeleteCareCommand);
    }

    @Test
    public void parseCommand_due() throws Exception {
        assertEquals(new DueCommand(Clock.systemDefaultZone()), parser.parseCommand(DueCommand.COMMAND_WORD));
        assertEquals(new DueCommand(INDEX_FIRST_PATIENT, Clock.systemDefaultZone()),
                parser.parseCommand(DueCommand.COMMAND_WORD + " 1"));
    }

    @Test
    public void parseCommand_editMed() throws Exception {
        EditMedCommand command = (EditMedCommand) parser.parseCommand("editmed 1 2 med/Aspirin t/08:00");
        assertEquals(new EditMedCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT, Optional.of("Aspirin"),
                Optional.of(List.of(LocalTime.of(8, 0)))), command);
    }

    @Test
    public void parseCommand_deleteMed() throws Exception {
        DeleteMedCommand command = (DeleteMedCommand) parser.parseCommand("deletemed 1 2");
        assertEquals(new DeleteMedCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_backup() throws Exception {
        assertTrue(parser.parseCommand(BackupCommand.COMMAND_WORD) instanceof BackupCommand);
        assertTrue(parser.parseCommand(BackupCommand.COMMAND_WORD + " 3") instanceof BackupCommand);
    }

    @Test
    public void parseCommand_export() throws Exception {
        ExportCommand command = (ExportCommand) parser.parseCommand(
                ExportCommand.COMMAND_WORD + " " + INDEX_FIRST_PATIENT.getOneBased());
        assertEquals(new ExportCommand(INDEX_FIRST_PATIENT), command);
    }

    @Test
    public void parseCommand_restore() throws Exception {
        RestoreCommand command = (RestoreCommand) parser.parseCommand(
                RestoreCommand.COMMAND_WORD + " mediconnect-20261020-083015.json");
        assertEquals(new RestoreCommand("mediconnect-20261020-083015.json"), command);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PATIENT.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PATIENT), command);
    }

    @Test
    public void parseCommand_archiveAndUnarchive() throws Exception {
        assertEquals(new ArchiveCommand(INDEX_FIRST_PATIENT), parser.parseCommand("archive 1"));
        assertEquals(new UnarchiveCommand(INDEX_FIRST_PATIENT), parser.parseCommand("unarchive 1"));
    }

    @Test
    public void parseCommand_deleteAllergy() throws Exception {
        DeleteAllergyCommand command = (DeleteAllergyCommand) parser.parseCommand(
                DeleteAllergyCommand.COMMAND_WORD + " 1 2");
        assertEquals(new DeleteAllergyCommand(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        EditCommand command = (EditCommand) parser.parseCommand("edit 1 n/Amy Bee");
        assertEquals(new EditCommand(INDEX_FIRST_PATIENT,
                new EditPatientDescriptorBuilder().withName("Amy Bee").build()), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 123") instanceof ListCommand);
    }

    @Test
    public void parseCommand_listArchived_ignoresArguments() throws Exception {
        assertTrue(parser.parseCommand(ListArchivedCommand.COMMAND_WORD) instanceof ListArchivedCommand);
        assertTrue(parser.parseCommand(ListArchivedCommand.COMMAND_WORD + " ignored") instanceof ListArchivedCommand);
    }

    @Test
    public void parseCommand_view() throws Exception {
        ViewCommand command = (ViewCommand) parser.parseCommand(
                ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PATIENT.getOneBased());
        assertEquals(new ViewCommand(INDEX_FIRST_PATIENT), command);

        // missing index
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, ()
                -> parser.parseCommand(ViewCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_contactCommands_dispatched() throws Exception {
        assertTrue(parser.parseCommand("addcontact 1 n/Clinic r/GP p/123") instanceof AddContactCommand);
        assertTrue(parser.parseCommand("editcontact 1 1 nt/") instanceof EditContactCommand);
        assertTrue(parser.parseCommand("deletecontact 1 1") instanceof DeleteContactCommand);
    }
}
