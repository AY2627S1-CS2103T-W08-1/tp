package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT =
            "Could not save data due to the following error: %s\nNothing was changed.";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.\n"
            + "Nothing was changed.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, new AddressBookParser());
    }

    /**
     * Constructs a manager with the given parser, allowing tests to isolate command dependencies.
     */
    LogicManager(Model model, Storage storage, AddressBookParser addressBookParser) {
        this.model = model;
        this.storage = storage;
        this.addressBookParser = addressBookParser;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        // the command text is not logged, as it can hold patient details
        logger.info("----------------[USER COMMAND][" + command.getClass().getSimpleName() + "]");
        Model.Snapshot snapshot = model.takeSnapshot();
        commandResult = command.execute(model);

        // Saves only changed patient data, so that commands that only read it still work if the file is read-only.
        if (model.getAddressBook().equals(snapshot.addressBook())) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            // Undoes the command, since it is reported as failed and its change would be lost on restart.
            model.restoreSnapshot(snapshot);
            logger.warning("Could not save the patient data, so the command was undone: " + ioe.getMessage());
            String format = ioe instanceof AccessDeniedException ? FILE_OPS_PERMISSION_ERROR_FORMAT
                    : FILE_OPS_ERROR_FORMAT;
            throw new CommandException(String.format(format, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Patient> getFilteredPatientList() {
        return model.getFilteredPatientList();
    }

    @Override
    public ObservableValue<Patient> getViewedPatient() {
        return model.getViewedPatient();
    }

    @Override
    public List<String> getAppointmentLinesAsShown(Patient patient) {
        requireNonNull(patient);
        LocalDateTime orderedAt = model.getAppointmentsOrderedAt(patient);
        return PatientFormatter.formatAppointments(patient.getAppointmentsUpcomingFirst(orderedAt), orderedAt);
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
