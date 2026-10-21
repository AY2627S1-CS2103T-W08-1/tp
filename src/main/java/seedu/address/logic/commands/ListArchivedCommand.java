package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ARCHIVED_PATIENTS;

import seedu.address.model.Model;

/**
 * Lists all archived patients.
 */
public class ListArchivedCommand extends Command {

    public static final String COMMAND_WORD = "listarchived";
    public static final String MESSAGE_SUCCESS = "Listed all archived patients.";
    public static final String MESSAGE_NO_PATIENTS = "No archived patients found.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPatientList(PREDICATE_SHOW_ARCHIVED_PATIENTS);
        return new CommandResult(model.getFilteredPatientList().isEmpty() ? MESSAGE_NO_PATIENTS : MESSAGE_SUCCESS);
    }
}
