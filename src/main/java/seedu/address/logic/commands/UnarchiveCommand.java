package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * Unarchives a patient identified using its displayed index.
 */
public class UnarchiveCommand extends Command {

    public static final String COMMAND_WORD = "unarchive";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the archived patient at the specified INDEX.\n"
            + "Format: " + COMMAND_FORMAT + "\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_UNARCHIVE_PATIENT_SUCCESS = "Unarchived patient: %1$s; NRIC: %2$s";
    public static final String MESSAGE_PATIENT_ALREADY_ACTIVE = "This patient is already active.";

    private final Index targetIndex;

    /**
     * Creates an {@code UnarchiveCommand} for the patient at {@code targetIndex}.
     */
    public UnarchiveCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> lastShownList = model.getFilteredPatientList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }

        Patient patientToUnarchive = lastShownList.get(targetIndex.getZeroBased());
        if (!patientToUnarchive.isArchived()) {
            throw new CommandException(MESSAGE_PATIENT_ALREADY_ACTIVE);
        }

        model.setPatient(patientToUnarchive, patientToUnarchive.withArchived(false));
        return new CommandResult(String.format(MESSAGE_UNARCHIVE_PATIENT_SUCCESS,
                patientToUnarchive.getName(), patientToUnarchive.getNric()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof UnarchiveCommand otherUnarchiveCommand
                && targetIndex.equals(otherUnarchiveCommand.targetIndex));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
