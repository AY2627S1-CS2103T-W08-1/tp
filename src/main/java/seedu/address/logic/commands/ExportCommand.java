package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.FileUtil;
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
        Path exportFile = exportDir.resolve(patient.getNric() + FILE_EXTENSION);
        writeRecord(patient, exportFile);

        // the file is named after the NRIC, so only the folder is logged
        logger.info("Exported a patient record to " + exportDir);
        return new CommandResult(String.format(MESSAGE_SUCCESS, patient.getName().fullName,
                exportFile.toAbsolutePath().normalize()));
    }

    /**
     * Writes the record of {@code patient} to {@code exportFile}, after creating the export folder if it does not
     * exist. The record is written through {@code FileUtil#writeThroughTemporaryFile}, so a failed write never leaves
     * a partial record in place of an earlier one, and does not replace an earlier export that is read-only.
     * Since the temporary file is always new, no existing file or link is written to.
     *
     * @throws CommandException if the folder or the file cannot be written.
     */
    private void writeRecord(Patient patient, Path exportFile) throws CommandException {
        try {
            Files.createDirectories(exportDir);
            List<String> recordLines = createRecord(patient);
            FileUtil.writeThroughTemporaryFile(exportFile, temporaryFile -> writeLines(temporaryFile, recordLines));
        } catch (IOException ioe) {
            logger.warning("Could not export a patient record to " + exportDir + ": " + ioe.getClass().getSimpleName());
            String reason = Messages.describeWriteFailure(ioe);
            String message = String.format(MESSAGE_FAILURE, patient.getName().fullName,
                    exportFile.toAbsolutePath().normalize(), reason);
            throw new CommandException(message, ioe);
        }
    }

    /**
     * Writes {@code lines} to {@code temporaryFile} in UTF-8, without following {@code temporaryFile} if it is a link.
     *
     * @throws IOException if the lines cannot be written.
     */
    private static void writeLines(Path temporaryFile, List<String> lines) throws IOException {
        Files.write(temporaryFile, lines, StandardCharsets.UTF_8, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Returns the lines of the exported record of {@code patient}: a title, the date and time of the export,
     * then the patient's full record, with appointments ordered as upcoming or past at that date and time.
     */
    private List<String> createRecord(Patient patient) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<String> lines = new ArrayList<>();
        lines.add(RECORD_TITLE);
        lines.add(EXPORTED_ON_PREFIX + now.format(EXPORT_TIME_FORMAT));
        lines.add("");
        lines.addAll(PatientFormatter.formatRecord(patient, now));
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
