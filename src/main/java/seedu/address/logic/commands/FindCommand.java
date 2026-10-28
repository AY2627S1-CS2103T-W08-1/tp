package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ACTIVE_PATIENTS;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.patient.NameContainsKeywordsPredicate;

/**
 * Finds and lists active patients in the address book whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds active patients whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie";

    public static final String MESSAGE_NO_PATIENTS_FOUND = "No patients found matching the given keyword(s).";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final NameContainsKeywordsPredicate predicate;

    /**
     * Creates a {@code FindCommand} that finds the active patients that match {@code predicate}.
     */
    public FindCommand(NameContainsKeywordsPredicate predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPatientList(predicate.and(PREDICATE_SHOW_ACTIVE_PATIENTS));
        // Only the number of patients is logged, as a keyword can be part of the name of a patient
        logger.info("Find matched " + model.getFilteredPatientList().size() + " patient(s)");

        if (model.getFilteredPatientList().isEmpty()) {
            return new CommandResult(MESSAGE_NO_PATIENTS_FOUND);
        }

        return new CommandResult(
                String.format(Messages.MESSAGE_PATIENTS_LISTED_OVERVIEW, model.getFilteredPatientList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
