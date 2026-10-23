package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.assertReadableReason;
import static seedu.address.testutil.TestUtil.runWhileReadOnly;
import static seedu.address.testutil.TestUtil.toPathRelativeToWorkingDir;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalPatients.ALICE;
import static seedu.address.testutil.TypicalPatients.ARCHIVED_ALICE;
import static seedu.address.testutil.TypicalPatients.BENSON;
import static seedu.address.testutil.TypicalPatients.DANIEL;
import static seedu.address.testutil.TypicalPatients.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.PatientFormatter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class ExportCommandTest {

    // an afternoon time, so that a 12-hour clock would show it differently
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-20T14:30:15Z"), ZoneOffset.UTC);
    private static final Index INDEX_DANIEL = Index.fromOneBased(4);

    @TempDir
    private Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(null, FIXED_CLOCK, temporaryFolder));
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(INDEX_FIRST_PATIENT, null, temporaryFolder));
    }

    @Test
    public void constructor_nullExportDir_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, null));
    }

    @Test
    public void constructor_indexOnly_usesSystemClockAndDataExportsFolder() {
        // the UG and help text tell users that records go to data/exports
        assertEquals(new ExportCommand(INDEX_FIRST_PATIENT, Clock.systemDefaultZone(), Paths.get("data", "exports")),
                new ExportCommand(INDEX_FIRST_PATIENT));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> createExportCommand(INDEX_FIRST_PATIENT).execute(null));
    }

    @Test
    public void execute_patientWithCareInfo_writesFullRecordToNricFile() throws Exception {
        Path expectedFile = temporaryFolder.resolve("S0000004A.txt");
        List<String> expectedLines = List.of(
                "MediConnect patient record",
                "Exported on 2026-10-20 14:30",
                "",
                "Daniel Meier",
                "NRIC: S0000004A",
                "DOB: " + DANIEL.getDob(),
                "Phone: 87652533",
                "Address: 10th street",
                "",
                "Medications",
                "1. Metformin (08:00, 20:00)",
                "",
                "Allergens",
                "1. Penicillin",
                "",
                "Care Instructions",
                "1. Check blood sugar",
                "",
                "Contacts",
                "1. Tan & Partners Clinic (GP); Phone: +65 6123-4567; Note: Call in the morning",
                "",
                "Appointments",
                "1. 2026-11-05 09:30 with Dr Tan (Fasting)");

        assertCommandSuccess(createExportCommand(INDEX_DANIEL), model, getExpectedSuccessMessage(DANIEL, expectedFile),
                createTypicalModel());
        assertEquals(expectedLines, Files.readAllLines(expectedFile, StandardCharsets.UTF_8));
    }

    @Test
    public void execute_patientWithoutCareInfo_showsNoneRecorded() throws Exception {
        createExportCommand(INDEX_FIRST_PATIENT).execute(model);

        List<String> lines = Files.readAllLines(temporaryFolder.resolve("S0000001A.txt"), StandardCharsets.UTF_8);
        assertEquals(getExpectedRecord(ALICE), lines);
        assertEquals(5, Collections.frequency(lines, PatientFormatter.NONE_RECORDED));
    }

    @Test
    public void execute_earlierExportOfPatient_replacesFile() throws Exception {
        Path exportFile = temporaryFolder.resolve("S0000001A.txt");
        List<String> oldLines = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            oldLines.add("old line " + i);
        }
        Files.write(exportFile, oldLines);

        createExportCommand(INDEX_FIRST_PATIENT).execute(model);

        // the new record is shorter than the old file, so no old lines may be left at the end
        assertEquals(getExpectedRecord(ALICE), Files.readAllLines(exportFile, StandardCharsets.UTF_8));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flag on folders")
    public void execute_temporaryFileCannotBeCreated_keepsEarlierExport() throws Throwable {
        Path exportDir = Files.createDirectory(temporaryFolder.resolve("exports"));
        Path exportFile = exportDir.resolve("S0000001A.txt");
        Files.write(exportFile, List.of("earlier export"));

        // a read-only folder makes creating the temporary file fail before the earlier export is touched
        runWhileReadOnly(exportDir, () -> assertExportFailsWithPermissionReason(exportDir, exportFile));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Windows ignores the read-only flag on files")
    public void execute_readOnlyEarlierExport_showsPermissionReasonAndKeepsEarlierExport() throws Throwable {
        Path exportDir = Files.createDirectory(temporaryFolder.resolve("exports"));
        Path exportFile = exportDir.resolve("S0000001A.txt");
        Files.write(exportFile, List.of("earlier export"));

        runWhileReadOnly(exportFile, () -> assertExportFailsWithPermissionReason(exportDir, exportFile));
    }

    @Test
    public void execute_nonEmptyFolderAtExportPath_failsAndRemovesTemporaryFile() throws Exception {
        Path exportDir = Files.createDirectory(temporaryFolder.resolve("exports"));
        Path exportFile = Files.createDirectory(exportDir.resolve("S0000001A.txt"));
        Path keptFile = Files.write(exportFile.resolve("kept.txt"), List.of("kept"));
        // the reason given by the file system differs between operating systems, so only the start is checked
        String expectedStart = String.format(ExportCommand.MESSAGE_FAILURE, ALICE.getName().fullName,
                exportFile, "");

        CommandException exception = Assertions.assertThrows(CommandException.class, () ->
                new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir).execute(model));
        assertTrue(exception.getMessage().startsWith(expectedStart.substring(0, expectedStart.length() - 1)));
        assertEquals(List.of("kept"), Files.readAllLines(keptFile, StandardCharsets.UTF_8));
        try (Stream<Path> files = Files.list(exportDir)) {
            assertEquals(List.of(exportFile), files.toList());
        }
    }

    @Test
    public void deleteTemporaryFile_fileCannotBeDeleted_doesNotThrow() throws Exception {
        Path nonEmptyFolder = Files.createDirectory(temporaryFolder.resolve("stuck"));
        Path insideFile = Files.write(nonEmptyFolder.resolve("inside.txt"), List.of("inside"));

        // deleting a folder that is not empty fails, which must only be logged
        ExportCommand.deleteTemporaryFile(nonEmptyFolder);
        assertTrue(Files.exists(insideFile));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Creating a link needs admin rights on Windows")
    public void execute_linkAtOldTemporaryPath_leavesLinkTargetUnchanged() throws Exception {
        Path exportDir = Files.createDirectory(temporaryFolder.resolve("exports"));
        Path unrelatedFile = Files.write(temporaryFolder.resolve("unrelated.txt"), List.of("unrelated"));
        // exports once wrote to this fixed name, so a link there could make them overwrite another file
        Path link = Files.createSymbolicLink(exportDir.resolve("S0000001A.txt.tmp"), unrelatedFile);
        Path exportFile = exportDir.resolve("S0000001A.txt");

        new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir).execute(model);

        assertEquals(List.of("unrelated"), Files.readAllLines(unrelatedFile, StandardCharsets.UTF_8));
        assertTrue(Files.isRegularFile(exportFile, LinkOption.NOFOLLOW_LINKS));
        assertEquals(getExpectedRecord(ALICE), Files.readAllLines(exportFile, StandardCharsets.UTF_8));
        assertTrue(Files.isSymbolicLink(link));
    }

    @Test
    public void execute_success_leavesNoTemporaryFile() throws Exception {
        createExportCommand(INDEX_FIRST_PATIENT).execute(model);

        try (Stream<Path> files = Files.list(temporaryFolder)) {
            assertEquals(List.of(temporaryFolder.resolve("S0000001A.txt")), files.toList());
        }
    }

    @Test
    public void execute_twoPatients_writesSeparateFiles() throws Exception {
        createExportCommand(INDEX_FIRST_PATIENT).execute(model);
        createExportCommand(INDEX_SECOND_PATIENT).execute(model);

        assertEquals(getExpectedRecord(ALICE),
                Files.readAllLines(temporaryFolder.resolve("S0000001A.txt"), StandardCharsets.UTF_8));
        assertEquals(getExpectedRecord(BENSON),
                Files.readAllLines(temporaryFolder.resolve("S0000002A.txt"), StandardCharsets.UTF_8));
    }

    @Test
    public void execute_nonAsciiAndApostrophes_savedAsUtf8() throws Exception {
        // an apostrophe in the name, then Chinese characters, an en dash, an accented letter and curly quotes
        Patient patient = new PatientBuilder().withName("Mary O'Brien")
                .withAddress("新加坡 Blk 8 – café")
                .withCareInstructions("Say ‘good morning’ first").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPatient(patient);
        Model singlePatientModel = new ModelManager(addressBook, new UserPrefs());
        Path expectedFile = temporaryFolder.resolve(patient.getNric() + ".txt");

        assertCommandSuccess(createExportCommand(INDEX_FIRST_PATIENT), singlePatientModel,
                getExpectedSuccessMessage(patient, expectedFile), new ModelManager(addressBook, new UserPrefs()));
        assertEquals(getExpectedRecord(patient), Files.readAllLines(expectedFile, StandardCharsets.UTF_8));
    }

    @Test
    public void execute_exportClock_decidesAppointmentOrder() throws Exception {
        // 20:00 on 2026-10-20 is after the export clock's 14:30, but before the system clock's date and time
        Appointment laterToday = new Appointment(LocalDate.of(2026, 10, 20), LocalTime.of(20, 0), "Dr Tan", "");
        Appointment nextWeek = new Appointment(LocalDate.of(2026, 10, 27), LocalTime.of(9, 0), "Dr Lim", "");
        Patient patient = new PatientBuilder().withAppointments(nextWeek, laterToday).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPatient(patient);

        createExportCommand(INDEX_FIRST_PATIENT).execute(new ModelManager(addressBook, new UserPrefs()));

        List<String> lines = Files.readAllLines(temporaryFolder.resolve(patient.getNric() + ".txt"),
                StandardCharsets.UTF_8);
        assertEquals(List.of("Appointments", "1. 2026-10-20 20:00 with Dr Tan", "2. 2026-10-27 09:00 with Dr Lim"),
                lines.subList(lines.size() - 3, lines.size()));
    }

    @Test
    public void execute_clockInOtherTimeZone_showsLocalTime() throws Exception {
        // 14:30 UTC is 22:30 in Singapore (UTC+8)
        Clock singaporeClock = FIXED_CLOCK.withZone(ZoneId.of("Asia/Singapore"));

        new ExportCommand(INDEX_FIRST_PATIENT, singaporeClock, temporaryFolder).execute(model);

        List<String> lines = Files.readAllLines(temporaryFolder.resolve("S0000001A.txt"), StandardCharsets.UTF_8);
        assertEquals("Exported on 2026-10-20 22:30", lines.get(1));
    }

    @Test
    public void execute_indexOutOfRange_throwsCommandExceptionAndWritesNothing() throws Exception {
        Index outOfRangeIndex = Index.fromOneBased(model.getFilteredPatientList().size() + 1);

        assertCommandFailure(createExportCommand(outOfRangeIndex), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertFolderEmpty(temporaryFolder);
    }

    @Test
    public void execute_filteredList_exportsDisplayedPatient() throws Exception {
        showPatientAtIndex(model, INDEX_SECOND_PATIENT);
        Model expectedModel = createTypicalModel();
        showPatientAtIndex(expectedModel, INDEX_SECOND_PATIENT);
        Path expectedFile = temporaryFolder.resolve("S0000002A.txt");

        assertCommandSuccess(createExportCommand(INDEX_FIRST_PATIENT), model,
                getExpectedSuccessMessage(BENSON, expectedFile), expectedModel);
        assertEquals(getExpectedRecord(BENSON), Files.readAllLines(expectedFile, StandardCharsets.UTF_8));
    }

    @Test
    public void execute_archivedPatientInDisplayedList_exportsRecord() throws Exception {
        Patient archivedPatient = new PatientBuilder(ARCHIVED_ALICE).withNric("S1111111A").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPatient(ALICE);
        addressBook.addPatient(archivedPatient);
        // shows archived patients only, as listarchived will
        Model archivedListModel = new ModelManager(addressBook, new UserPrefs());
        archivedListModel.updateFilteredPatientList(Patient::isArchived);
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPatientList(Patient::isArchived);
        Path expectedFile = temporaryFolder.resolve("S1111111A.txt");

        assertCommandSuccess(createExportCommand(INDEX_FIRST_PATIENT), archivedListModel,
                getExpectedSuccessMessage(archivedPatient, expectedFile), expectedModel);
        List<String> lines = Files.readAllLines(expectedFile, StandardCharsets.UTF_8);
        assertEquals(getExpectedRecord(archivedPatient), lines);
        assertTrue(lines.contains(PatientFormatter.ARCHIVED_STATUS));
    }

    @Test
    public void execute_filteredListIndexOutOfRange_throwsCommandException() throws Exception {
        showPatientAtIndex(model, INDEX_FIRST_PATIENT);

        // the second patient is in MediConnect, but not in the displayed list
        assertCommandFailure(createExportCommand(INDEX_SECOND_PATIENT), model,
                Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertFolderEmpty(temporaryFolder);
    }

    @Test
    public void execute_missingExportFolder_createsFolder() throws Exception {
        Path exportDir = temporaryFolder.resolve("data").resolve("exports");
        assertFalse(Files.exists(exportDir));

        assertExportsAlice(exportDir, exportDir);
    }

    @Test
    public void execute_relativeExportFolder_showsFullPath() throws Exception {
        assertExportsAlice(toPathRelativeToWorkingDir(temporaryFolder), temporaryFolder);
    }

    @Test
    public void execute_exportFolderCannotBeCreated_throwsCommandException() throws Exception {
        // a regular file where the export folder's parent should be makes creating the folder fail on every OS
        Path exportDir = Files.createFile(temporaryFolder.resolve("blocked")).resolve("exports");
        Path exportFile = exportDir.resolve("S0000001A.txt");
        ExportCommand exportCommand = new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir);
        String message = Assertions.assertThrows(CommandException.class, () -> exportCommand.execute(model))
                .getMessage();

        assertReadableReason(message, "Could not export Alice Pauline's record to " + exportFile + ": ", ".",
                exportDir);
        assertCommandFailure(exportCommand, model, message);
    }

    @Test
    public void execute_fileInPlaceOfExportFolder_throwsCommandException() throws Exception {
        Path exportDir = Files.createFile(temporaryFolder.resolve("exports"));
        String expectedMessage = String.format(ExportCommand.MESSAGE_FAILURE, ALICE.getName().fullName,
                exportDir.resolve("S0000001A.txt"), Messages.MESSAGE_FILE_SYSTEM_ERROR);

        assertCommandFailure(new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir), model, expectedMessage);
        assertTrue(Files.isRegularFile(exportDir));
    }

    @Test
    public void execute_folderInPlaceOfExportFile_throwsCommandException() throws Exception {
        Path exportFile = Files.createDirectory(temporaryFolder.resolve("S0000001A.txt"));
        ExportCommand exportCommand = createExportCommand(INDEX_FIRST_PATIENT);

        String message = Assertions.assertThrows(CommandException.class, () -> exportCommand.execute(model))
                .getMessage();

        assertReadableReason(message, "Could not export Alice Pauline's record to " + exportFile + ": ", ".",
                exportFile);
        assertTrue(Files.isDirectory(exportFile));
        // the temporary file was written before the move failed, and is deleted again
        assertNoTemporaryFiles(temporaryFolder);
    }

    @Test
    public void execute_relativeExportFolderCannotBeCreated_showsFullPathInError() throws Exception {
        Path exportDir = Files.createFile(temporaryFolder.resolve("blocked")).resolve("exports");
        ExportCommand exportCommand = new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK,
                toPathRelativeToWorkingDir(exportDir));

        String message = Assertions.assertThrows(CommandException.class, () -> exportCommand.execute(model))
                .getMessage();

        assertTrue(message.startsWith("Could not export Alice Pauline's record to "
                + exportDir.resolve("S0000001A.txt") + ": "));
    }

    @Test
    public void equals() {
        Clock otherClock = Clock.fixed(Instant.parse("2026-10-21T09:00:00Z"), ZoneOffset.UTC);
        ExportCommand exportCommand = createExportCommand(INDEX_FIRST_PATIENT);

        // same object -> returns true
        assertTrue(exportCommand.equals(exportCommand));

        // same values -> returns true
        assertTrue(exportCommand.equals(new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, temporaryFolder)));

        // null -> returns false
        assertFalse(exportCommand.equals(null));

        // different types -> returns false
        assertFalse(exportCommand.equals(new ViewCommand(INDEX_FIRST_PATIENT)));

        // different index -> returns false
        assertFalse(exportCommand.equals(createExportCommand(INDEX_SECOND_PATIENT)));

        // different clock -> returns false
        assertFalse(exportCommand.equals(new ExportCommand(INDEX_FIRST_PATIENT, otherClock, temporaryFolder)));

        // different export folder -> returns false
        assertFalse(exportCommand.equals(new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK,
                temporaryFolder.resolve("other"))));
    }

    @Test
    public void toStringMethod() {
        String expected = ExportCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PATIENT
                + ", clock=" + FIXED_CLOCK + ", exportDir=" + temporaryFolder + "}";
        assertEquals(expected, createExportCommand(INDEX_FIRST_PATIENT).toString());
    }

    private ExportCommand createExportCommand(Index index) {
        return new ExportCommand(index, FIXED_CLOCK, temporaryFolder);
    }

    private static Model createTypicalModel() {
        return new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    private static String getExpectedSuccessMessage(Patient patient, Path exportFile) {
        return String.format(ExportCommand.MESSAGE_SUCCESS, patient.getName().fullName, exportFile);
    }

    /**
     * Returns the lines that an export of {@code patient} with {@code FIXED_CLOCK} should contain.
     */
    private static List<String> getExpectedRecord(Patient patient) {
        List<String> lines = new ArrayList<>(List.of(ExportCommand.RECORD_TITLE,
                ExportCommand.EXPORTED_ON_PREFIX + "2026-10-20 14:30", ""));
        lines.addAll(PatientFormatter.formatRecord(patient, LocalDateTime.now(FIXED_CLOCK)));
        return lines;
    }

    private static void assertNoTemporaryFiles(Path folder) throws Exception {
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of(), files.filter(file -> file.getFileName().toString().endsWith(".tmp")).toList());
        }
    }

    private static void assertFolderEmpty(Path folder) throws Exception {
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(0, files.count());
        }
    }

    /**
     * Confirms that exporting the first patient to {@code exportDir} fails because MediConnect may not write there,
     * and that the earlier export in {@code exportFile} is kept, with no temporary file left behind.
     */
    private void assertExportFailsWithPermissionReason(Path exportDir, Path exportFile) throws Exception {
        String expectedMessage = String.format(ExportCommand.MESSAGE_FAILURE, ALICE.getName().fullName,
                exportFile, Messages.MESSAGE_NO_WRITE_PERMISSION);

        assertCommandFailure(new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir), model, expectedMessage);
        assertEquals(List.of("earlier export"), Files.readAllLines(exportFile, StandardCharsets.UTF_8));
        assertNoTemporaryFiles(exportDir);
    }

    /**
     * Exports the first patient, Alice, to {@code exportDir}, and confirms that the message shows her file in
     * {@code expectedFolder}, the full form of {@code exportDir}, that the file holds her record, and that
     * the model is unchanged.
     */
    private void assertExportsAlice(Path exportDir, Path expectedFolder) throws Exception {
        Path expectedFile = expectedFolder.resolve("S0000001A.txt");

        assertCommandSuccess(new ExportCommand(INDEX_FIRST_PATIENT, FIXED_CLOCK, exportDir), model,
                getExpectedSuccessMessage(ALICE, expectedFile), createTypicalModel());
        assertEquals(getExpectedRecord(ALICE), Files.readAllLines(expectedFile, StandardCharsets.UTF_8));
    }
}
