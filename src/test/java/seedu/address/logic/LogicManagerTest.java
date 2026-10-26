package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DOB_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.FIXED_CLOCK;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NRIC_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCareCommand;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.AdjustableClock;
import seedu.address.testutil.PatientBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private StorageManager storage;
    private Logic logic;
    private boolean wasSaveAttempted;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = createLogicWithTemporaryBackups(storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_NO_PATIENTS, model);
    }

    @Test
    public void execute_validCommand_logsCommandTypeButNotCommandText() throws Exception {
        String secretArguments = "S1234567A 91234567";
        List<String> messages = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                messages.add(logRecord.getMessage());
            }

            @Override
            public void flush() { }

            @Override
            public void close() { }
        };
        Logger logicLogger = LogsCenter.getLogger(LogicManager.class);
        logicLogger.addHandler(handler);
        try {
            logic.execute(ListCommand.COMMAND_WORD + " " + secretArguments);
        } finally {
            logicLogger.removeHandler(handler);
        }

        assertTrue(messages.stream().anyMatch(message -> message.contains(ListCommand.class.getSimpleName())));
        assertFalse(messages.stream().anyMatch(message -> message.contains(secretArguments)));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_clearWithoutConfirmation_preservesPatientsAndViewedPatient() throws Exception {
        model.addPatient(ALICE);
        model.setViewedPatient(ALICE);

        for (String input : List.of("clear", "clear 1", "clear confirm extra")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(ALICE), model.getAddressBook().getPatientList());
            assertEquals(ALICE, model.getViewedPatient().getValue());
            assertTrue(storage.readAddressBook().isEmpty());
        }
    }

    @Test
    public void execute_clearConfirmed_persistsEmptyData() throws Exception {
        model.addPatient(ALICE);
        model.addPatient(new PatientBuilder(BENSON).withArchived(true).build());
        model.setViewedPatient(ALICE);
        storage.saveAddressBook(model.getAddressBook());

        assertTrue(logic.execute(ClearCommand.COMMAND_FORMAT).getFeedbackToUser()
                .startsWith(ClearCommand.MESSAGE_SUCCESS + "\nBackup saved to: "));

        assertTrue(model.getAddressBook().getPatientList().isEmpty());
        assertNull(model.getViewedPatient().getValue());
        assertTrue(storage.readAddressBook().orElseThrow().getPatientList().isEmpty());
    }

    @Test
    public void execute_clearSaveFails_restoresPatientsFilterAndViewedPatient() throws Exception {
        Patient archivedPatient = new PatientBuilder(BENSON).withArchived(true).build();
        model.addPatient(ALICE);
        model.addPatient(archivedPatient);
        model.updateFilteredPatientList(patient -> patient.equals(ALICE));
        model.setViewedPatient(ALICE);
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);

        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), () ->
                logic.execute(ClearCommand.COMMAND_FORMAT));

        assertTrue(wasSaveAttempted);
        assertEquals(List.of(ALICE, archivedPatient), model.getAddressBook().getPatientList());
        assertEquals(List.of(ALICE), model.getFilteredPatientList());
        assertEquals(ALICE, model.getViewedPatient().getValue());
    }

    @Test
    public void execute_commandsThatChangeNoPatientDataWithFailingStorage_succeed() throws Exception {
        model.addPatient(ALICE);
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);

        logic.execute(ListCommand.COMMAND_WORD);
        logic.execute(ViewCommand.COMMAND_WORD + " 1");
        logic.execute(HelpCommand.COMMAND_WORD);
        assertTrue(logic.execute(ExitCommand.COMMAND_WORD).isExit());
    }

    @Test
    public void execute_storageThrowsIoException_restoresFilterAndViewedPatient() throws Exception {
        model.addPatient(ALICE);
        model.addPatient(BENSON);
        logic = createLogicWithFailingStorage(DUMMY_IO_EXCEPTION);
        logic.execute(FindCommand.COMMAND_WORD + " Alice");
        logic.execute(ViewCommand.COMMAND_WORD + " 1");

        // addcare would show the edited patient, and add would show all active patients
        assertThrows(CommandException.class, () -> logic.execute(AddCareCommand.COMMAND_WORD + " 1 med/Metformin"));
        assertThrows(CommandException.class, () -> logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY
                + PHONE_DESC_AMY + NRIC_DESC_AMY + DOB_DESC_AMY + ADDRESS_DESC_AMY));

        assertEquals(new AddressBookBuilder().withPatient(ALICE).withPatient(BENSON).build(),
                model.getAddressBook());
        assertEquals(List.of(ALICE), model.getFilteredPatientList());
        assertEquals(ALICE, model.getViewedPatient().getValue());
    }

    @Test
    public void getFilteredPatientList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPatientList().remove(0));
    }

    @Test
    public void getViewedPatient_viewCommandExecuted_returnsViewedPatient() throws Exception {
        assertNull(logic.getViewedPatient().getValue());

        model.addPatient(ALICE);
        logic.execute(ViewCommand.COMMAND_WORD + " 1");
        assertEquals(ALICE, logic.getViewedPatient().getValue());
    }

    @Test
    public void getAppointmentLinesAsShown_appointmentPassesWhileViewed_ordersAndMarksAtTimeShown() {
        AdjustableClock clock = new AdjustableClock(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
        Appointment soon = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(12, 1), "Dr Tan", "");
        Appointment later = new Appointment(LocalDate.of(2026, 10, 18), LocalTime.of(14, 0), "Dr Lim", "");
        Patient patient = new PatientBuilder(ALICE).withAppointments(soon, later).build();
        Model clockedModel = new ModelManager(new AddressBookBuilder().withPatient(patient).build(),
                new UserPrefs(), clock);
        Logic clockedLogic = new LogicManager(clockedModel, storage);
        clockedModel.setViewedPatient(patient);
        clock.advance(Duration.ofMinutes(2));

        // the appointment at 12:01 has passed, but is not marked until the patient is shown again
        assertEquals(List.of("2026-10-18 12:01 with Dr Tan", "2026-10-18 14:00 with Dr Lim"),
                clockedLogic.getAppointmentLinesAsShown(patient));

        clockedModel.setViewedPatient(patient);
        assertEquals(List.of("2026-10-18 14:00 with Dr Lim", "2026-10-18 12:01 with Dr Tan (past)"),
                clockedLogic.getAppointmentLinesAsShown(patient));
    }

    @Test
    public void execute_addCareOnViewedPatient_updatesViewedPatient() throws Exception {
        model.addPatient(ALICE);
        logic.execute(ViewCommand.COMMAND_WORD + " 1");

        logic.execute(AddCareCommand.COMMAND_WORD + " 1 med/Metformin");

        Patient expectedPatient = new PatientBuilder(ALICE).withMedications("Metformin").build();
        assertEquals(expectedPatient, logic.getViewedPatient().getValue());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component:
     * the command fails and the model is left unchanged.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        logic = createLogicWithFailingStorage(e);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + NRIC_DESC_AMY + DOB_DESC_AMY + ADDRESS_DESC_AMY;
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, new ModelManager());
    }

    /**
     * Returns a {@code LogicManager} for {@code model} whose storage throws {@code e} when saving patient data.
     */
    private Logic createLogicWithFailingStorage(IOException e) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                wasSaveAttempted = true;
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        return createLogicWithTemporaryBackups(storage);
    }

    /**
     * Returns a manager that validates command input normally but writes clear backups to a temporary folder.
     */
    private Logic createLogicWithTemporaryBackups(Storage storage) {
        AddressBookParser parser = new AddressBookParser() {
            @Override
            public Command parseCommand(String commandText) throws ParseException {
                Command command = super.parseCommand(commandText);
                return command instanceof ClearCommand
                        ? new ClearCommand(FIXED_CLOCK, temporaryFolder.resolve("backups")) : command;
            }
        };
        return new LogicManager(model, storage, parser);
    }
}
