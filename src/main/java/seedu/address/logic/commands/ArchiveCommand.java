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
 * Archives a patient identified using its displayed index.
 */
public class ArchiveCommand extends Command {

    public static final String COMMAND_WORD = "archive";
    public static final String COMMAND_FORMAT = COMMAND_WORD + " INDEX";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Archives the patient at the specified INDEX.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_ARCHIVE_PATIENT_SUCCESS = "Archived patient: %1$s; NRIC: %2$s";
    public static final String MESSAGE_PATIENT_ALREADY_ARCHIVED = "This patient is already archived.";

    private final Index targetIndex;

    /**
     * Creates an {@code ArchiveCommand} for the patient at {@code targetIndex}.
     */
    public ArchiveCommand(Index targetIndex) {
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

        Patient patientToArchive = lastShownList.get(targetIndex.getZeroBased());
        if (patientToArchive.isArchived()) {
            throw new CommandException(MESSAGE_PATIENT_ALREADY_ARCHIVED);
        }

        model.setPatient(patientToArchive, patientToArchive.withArchived(true));
        return new CommandResult(String.format(MESSAGE_ARCHIVE_PATIENT_SUCCESS,
                patientToArchive.getName(), patientToArchive.getNric()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ArchiveCommand otherArchiveCommand
                && targetIndex.equals(otherArchiveCommand.targetIndex));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
