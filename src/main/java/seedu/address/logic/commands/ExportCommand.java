package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.PatientFormatter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.patient.Patient;

/**
 * Saves the full record of a patient, identified by its displayed index, to a text file named after their NRIC.
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Saves the full record of the patient identified by the index number used in the displayed "
            + "patient list to data/exports, in a file named after their NRIC.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Exported %1$s's record to: %2$s";
    public static final String MESSAGE_FAILURE = "Could not export %1$s's record to %2$s: %3$s.";

    /** Folder that records are exported to, relative to the folder MediConnect is launched from. */
    public static final Path DEFAULT_EXPORT_DIR = Paths.get("data", "exports");

    /** First line of every exported record. */
    public static final String RECORD_TITLE = "MediConnect patient record";

    /** Start of the second line of every exported record, which ends with the date and time of the export. */
    public static final String EXPORTED_ON_PREFIX = "Exported on ";

    private static final String FILE_EXTENSION = ".txt";
    // Locale.ROOT keeps the export date the same whatever the computer's language settings are
    private static final DateTimeFormatter EXPORT_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private static final Logger logger = LogsCenter.getLogger(ExportCommand.class);

    private final Index targetIndex;
    private final Clock clock;
    private final Path exportDir;

    /**
     * Creates an ExportCommand that exports the patient at {@code targetIndex} to {@code DEFAULT_EXPORT_DIR},
     * dated with the system clock.
     *
     * @param targetIndex Index of the patient in the displayed patient list.
     */
    public ExportCommand(Index targetIndex) {
        this(targetIndex, Clock.systemDefaultZone(), DEFAULT_EXPORT_DIR);
    }

    /**
     * Creates an ExportCommand that exports the patient at {@code targetIndex} to {@code exportDir},
     * dated with {@code clock}.
     *
     * @param targetIndex Index of the patient in the displayed patient list.
     * @param clock {@code Clock} that gives the date and time shown in the exported record.
     * @param exportDir Folder to save the record to. It is created if it does not exist.
     */
    public ExportCommand(Index targetIndex, Clock clock, Path exportDir) {
        requireNonNull(targetIndex);
        requireNonNull(clock);
        requireNonNull(exportDir);
        this.targetIndex = targetIndex;
        this.clock = clock;
        this.exportDir = exportDir;
    }

    /**
     * Writes the full record of the patient at the target index to a file named after their NRIC,
     * replacing any earlier export of that patient. The model is not changed.
     *
     * @param model {@code Model} whose displayed patient list the target index refers to.
     * @return a result showing the full path of the exported file.
     * @throws CommandException if the index is invalid, or the file cannot be written.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Patient> lastShownList = model.getFilteredPatientList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        }

        Patient patient = lastShownList.get(targetIndex.getZeroBased());
        String name = patient.getName().fullName;
        Path exportFile = exportDir.resolve(patient.getNric() + FILE_EXTENSION);

        try {
            Files.createDirectories(exportDir);
            Files.write(exportFile, createRecord(patient), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            logger.warning("Could not export a patient record to " + exportFile + ": " + ioe);
            String reason = Messages.describeWriteFailure(ioe);
            throw new CommandException(
                    String.format(MESSAGE_FAILURE, name, exportFile.toAbsolutePath().normalize(), reason), ioe);
        }

        logger.info("Exported a patient record to " + exportFile);
        return new CommandResult(String.format(MESSAGE_SUCCESS, name, exportFile.toAbsolutePath().normalize()));
    }

    /**
     * Returns the lines of the exported record of {@code patient}: a title, the date and time of the export,
     * then the patient's full record.
     */
    private List<String> createRecord(Patient patient) {
        List<String> lines = new ArrayList<>();
        lines.add(RECORD_TITLE);
        lines.add(EXPORTED_ON_PREFIX + LocalDateTime.now(clock).format(EXPORT_TIME_FORMAT));
        lines.add("");
        lines.addAll(PatientFormatter.formatRecord(patient));
        return lines;
    }

    /**
     * Returns true if {@code other} is an {@code ExportCommand} with the same index, clock and export folder.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ExportCommand otherExportCommand)) {
            return false;
        }

        return targetIndex.equals(otherExportCommand.targetIndex)
                && clock.equals(otherExportCommand.clock)
                && exportDir.equals(otherExportCommand.exportDir);
    }

    /**
     * Returns a string showing the index, clock and export folder of this command.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("clock", clock)
                .add("exportDir", exportDir)
                .toString();
    }
}
