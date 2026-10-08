package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;

import seedu.address.model.Model;

/**
 * Lists all patients in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all patients.";

    public static final String MESSAGE_NO_PATIENTS = "No patients found. Use the 'add' command to add a patient.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);

        if (model.getFilteredPatientList().isEmpty()) {
            return new CommandResult(MESSAGE_NO_PATIENTS);
        }

        return new CommandResult(MESSAGE_SUCCESS);
    }
}
