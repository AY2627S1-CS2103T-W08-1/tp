package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_DUE_PATIENT_ARCHIVED;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_NOTHING_DUE_TODAY;
import static seedu.address.logic.Messages.MESSAGE_NOTHING_DUE_TODAY_FOR_PATIENT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPatientAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PATIENTS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PATIENT;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Patient;
import seedu.address.testutil.PatientBuilder;

public class DueCommandTest {

    private static final String EARLIER = " (earlier today)";

    private static final Medication ASPIRIN = medication("Aspirin 100mg", 8, 0);
    private static final Medication METFORMIN = medication("Metformin 500mg", 8, 0, 20, 0);
    private static final Medication NO_TIMES = new Medication("Vitamin D");

    private final Patient alex = patient("Alex Yeoh", "S0000001A", ASPIRIN, METFORMIN);
    private final Patient bernice = patient("Bernice Yu", "S0000002A", medication("Amlodipine 5mg", 13, 30));
    private final Patient charlotte = patient("Charlotte Oliveiro", "S0000003A", NO_TIMES);
    private final Patient david = patient("David Li", "S0000004A");

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        Index index = INDEX_FIRST_PATIENT;
        Clock clock = clockAt(8, 0, 0);
        assertThrows(NullPointerException.class, () -> new DueCommand(null));
        assertThrows(NullPointerException.class, () -> new DueCommand(null, clock));
        assertThrows(NullPointerException.class, () -> new DueCommand(index, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DueCommand command = new DueCommand(clockAt(8, 0, 0));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_morning_noDoseIsMarkedEarlierToday() throws Exception {
        Model model = createModel(alex, bernice);
        assertDue(new DueCommand(clockAt(7, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg",
                "08:00 Alex Yeoh: Metformin 500mg",
                "13:30 Bernice Yu: Amlodipine 5mg",
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_midday_onlyPastDosesAreMarkedEarlierToday() throws Exception {
        Model model = createModel(alex, bernice);
        assertDue(new DueCommand(clockAt(12, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg" + EARLIER,
                "08:00 Alex Yeoh: Metformin 500mg" + EARLIER,
                "13:30 Bernice Yu: Amlodipine 5mg",
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_evening_everyDoseIsMarkedEarlierToday() throws Exception {
        Model model = createModel(alex, bernice);
        assertDue(new DueCommand(clockAt(21, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg" + EARLIER,
                "08:00 Alex Yeoh: Metformin 500mg" + EARLIER,
                "13:30 Bernice Yu: Amlodipine 5mg" + EARLIER,
                "20:00 Alex Yeoh: Metformin 500mg" + EARLIER);
    }

    @Test
    public void execute_exactlyCurrentMinute_notMarkedEarlierToday() throws Exception {
        Model model = createModel(alex);
        assertDue(new DueCommand(clockAt(8, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg",
                "08:00 Alex Yeoh: Metformin 500mg",
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_secondsInsideCurrentMinute_notMarkedEarlierToday() throws Exception {
        Model model = createModel(alex);
        assertDue(new DueCommand(clockAt(8, 0, 59)), model,
                "08:00 Alex Yeoh: Aspirin 100mg",
                "08:00 Alex Yeoh: Metformin 500mg",
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_oneMinuteAfterDose_markedEarlierToday() throws Exception {
        Model model = createModel(alex);
        assertDue(new DueCommand(clockAt(8, 1, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg" + EARLIER,
                "08:00 Alex Yeoh: Metformin 500mg" + EARLIER,
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_oneMinuteBeforeDose_notMarkedEarlierToday() throws Exception {
        Model model = createModel(alex);
        assertDue(new DueCommand(clockAt(7, 59, 59)), model,
                "08:00 Alex Yeoh: Aspirin 100mg",
                "08:00 Alex Yeoh: Metformin 500mg",
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_midnightAndLastMinuteOfDay() throws Exception {
        Model model = createModel(patient("Zed Tan", "S0000009A", medication("Night Dose", 0, 0, 23, 59)));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model,
                "00:00 Zed Tan: Night Dose", "23:59 Zed Tan: Night Dose");
        assertDue(new DueCommand(clockAt(23, 59, 30)), model,
                "00:00 Zed Tan: Night Dose" + EARLIER, "23:59 Zed Tan: Night Dose");
    }

    @Test
    public void execute_clockZoneDecidesCurrentTime() throws Exception {
        Model model = createModel(patient("Zed Tan", "S0000009A", medication("Dose", 8, 0)));
        Clock utc = Clock.fixed(Instant.parse("2026-10-19T00:00:00Z"), ZoneOffset.UTC);
        Clock singapore = Clock.fixed(Instant.parse("2026-10-19T00:00:00Z"), ZoneOffset.ofHours(8));

        assertDue(new DueCommand(utc), model, "08:00 Zed Tan: Dose");
        assertDue(new DueCommand(singapore), model, "08:00 Zed Tan: Dose");
        assertDue(new DueCommand(Clock.fixed(Instant.parse("2026-10-19T00:01:00Z"), ZoneOffset.ofHours(8))), model,
                "08:00 Zed Tan: Dose" + EARLIER);
    }

    @Test
    public void execute_samePatientNamesAndTimes_sortedByTimeThenNameIgnoringCase() throws Exception {
        Model model = createModel(
                patient("zoe Lim", "S0000003A", medication("Dose A", 9, 0)),
                patient("Amy Bee", "S0000002A", medication("Dose B", 9, 0), medication("Dose C", 7, 0)),
                patient("amy bee", "S0000001A", medication("Dose D", 9, 0)));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model,
                "07:00 Amy Bee (S0000002A): Dose C",
                "09:00 amy bee (S0000001A): Dose D",
                "09:00 Amy Bee (S0000002A): Dose B",
                "09:00 zoe Lim: Dose A");
    }

    @Test
    public void execute_sameNameDifferentNrics_nricShownOnlyForSharedName() throws Exception {
        Model model = createModel(
                patient("Amy Tan", "S1111111A", medication("Aspirin", 8, 0)),
                patient("Amy Tan", "S2222222B", medication("Aspirin", 8, 0)),
                patient("Bob Lim", "S3333333C", medication("Aspirin", 8, 0)));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model,
                "08:00 Amy Tan (S1111111A): Aspirin",
                "08:00 Amy Tan (S2222222B): Aspirin",
                "08:00 Bob Lim: Aspirin");
    }

    @Test
    public void execute_sameNameButOnlyOneHasDoses_nricNotShown() throws Exception {
        Model model = createModel(
                patient("Amy Tan", "S1111111A", medication("Aspirin", 8, 0)),
                patient("Amy Tan", "S2222222B", new Medication("Vitamin D")));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model, "08:00 Amy Tan: Aspirin");
    }

    @Test
    public void execute_sameNameButOneArchived_nricNotShown() throws Exception {
        Patient archivedAmy = new PatientBuilder(patient("Amy Tan", "S2222222B", medication("Aspirin", 8, 0)))
                .withArchived(true).build();
        Model model = createModel(patient("Amy Tan", "S1111111A", medication("Aspirin", 8, 0)), archivedAmy);
        assertDue(new DueCommand(clockAt(0, 0, 0)), model, "08:00 Amy Tan: Aspirin");
    }

    @Test
    public void execute_sameNameWithTwoMedications_nricShownOnEveryLine() throws Exception {
        Model model = createModel(
                patient("Amy Tan", "S1111111A", medication("Aspirin", 8, 0), medication("Zinc", 9, 0)),
                patient("Amy Tan", "S2222222B", medication("Aspirin", 8, 0)));
        assertDue(new DueCommand(clockAt(8, 30, 0)), model,
                "08:00 Amy Tan (S1111111A): Aspirin (earlier today)",
                "08:00 Amy Tan (S2222222B): Aspirin (earlier today)",
                "09:00 Amy Tan (S1111111A): Zinc");
    }

    @Test
    public void execute_descriptionWithLineBreak_listsOneLinePerTime() throws Exception {
        Model model = createModel(patient("Amy Tan", "S0000001A",
                medication("Aspirin\n20:00 Amy Tan: Warfarin", 8, 0)));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model, "08:00 Amy Tan: Aspirin 20:00 Amy Tan: Warfarin");
    }

    @Test
    public void execute_samePatientSameTime_keepsMedicationOrder() throws Exception {
        Model model = createModel(patient("Amy Bee", "S0000001A",
                medication("Second Listed Dose", 9, 0), medication("Another Dose", 9, 0)));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model,
                "09:00 Amy Bee: Second Listed Dose", "09:00 Amy Bee: Another Dose");
    }

    @Test
    public void execute_duplicateTimesOnMedication_listedOnce() throws Exception {
        Medication duplicated = new Medication("Dose", List.of(LocalTime.of(9, 0), LocalTime.of(9, 0)));
        Model model = createModel(patient("Amy Bee", "S0000001A", duplicated));
        assertDue(new DueCommand(clockAt(0, 0, 0)), model, "09:00 Amy Bee: Dose");
    }

    @Test
    public void execute_medicationsWithoutTimes_notListed() throws Exception {
        Model model = createModel(charlotte, bernice);
        assertDue(new DueCommand(clockAt(0, 0, 0)), model, "13:30 Bernice Yu: Amlodipine 5mg");
    }

    @Test
    public void execute_noPatients_showsNothingDueMessage() throws Exception {
        assertDue(new DueCommand(clockAt(9, 0, 0)), new ModelManager(), MESSAGE_NOTHING_DUE_TODAY);
    }

    @Test
    public void execute_patientsWithoutMedications_showsNothingDueMessage() throws Exception {
        assertDue(new DueCommand(clockAt(9, 0, 0)), createModel(david), MESSAGE_NOTHING_DUE_TODAY);
    }

    @Test
    public void execute_onlyMedicationsWithoutTimes_showsNothingDueMessage() throws Exception {
        assertDue(new DueCommand(clockAt(9, 0, 0)), createModel(charlotte, david), MESSAGE_NOTHING_DUE_TODAY);
    }

    @Test
    public void execute_allDosesEarlierToday_stillListedNotTreatedAsNothingDue() throws Exception {
        assertDue(new DueCommand(clockAt(23, 0, 0)), createModel(bernice),
                "13:30 Bernice Yu: Amlodipine 5mg" + EARLIER);
    }

    @Test
    public void execute_archivedPatientWithDueMedication_notListed() throws Exception {
        Patient archivedAlex = new PatientBuilder(alex).withArchived(true).build();

        assertDue(new DueCommand(clockAt(7, 0, 0)), createModel(archivedAlex, bernice),
                "13:30 Bernice Yu: Amlodipine 5mg");
    }

    @Test
    public void execute_archivedPatientAtDisplayedIndex_throwsCommandException() {
        Patient archivedAlex = new PatientBuilder(alex).withArchived(true).build();
        Model model = createModel(archivedAlex);
        model.updateFilteredPatientList(PREDICATE_SHOW_ALL_PATIENTS);

        assertCommandFailure(new DueCommand(INDEX_FIRST_PATIENT, clockAt(7, 0, 0)), model,
                MESSAGE_DUE_PATIENT_ARCHIVED);
    }

    @Test
    public void execute_withIndex_showsOnlyThatPatient() throws Exception {
        Model model = createModel(alex, bernice);
        assertDue(new DueCommand(INDEX_SECOND_PATIENT, clockAt(7, 0, 0)), model,
                "13:30 Bernice Yu: Amlodipine 5mg");
        assertDue(new DueCommand(INDEX_FIRST_PATIENT, clockAt(12, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg" + EARLIER,
                "08:00 Alex Yeoh: Metformin 500mg" + EARLIER,
                "20:00 Alex Yeoh: Metformin 500mg");
    }

    @Test
    public void execute_withIndexPatientWithoutTimes_showsNothingDueForPatient() throws Exception {
        Model model = createModel(alex, charlotte, david);
        assertDue(new DueCommand(INDEX_SECOND_PATIENT, clockAt(9, 0, 0)), model,
                String.format(MESSAGE_NOTHING_DUE_TODAY_FOR_PATIENT, charlotte.getName()));
        assertDue(new DueCommand(INDEX_THIRD_PATIENT, clockAt(9, 0, 0)), model,
                String.format(MESSAGE_NOTHING_DUE_TODAY_FOR_PATIENT, david.getName()));
    }

    @Test
    public void execute_withInvalidIndex_failsAndLeavesModelUnchanged() {
        Model model = createModel(alex, bernice);
        Clock clock = clockAt(9, 0, 0);
        assertCommandFailure(new DueCommand(INDEX_THIRD_PATIENT, clock), model,
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertCommandFailure(new DueCommand(Index.fromOneBased(Integer.MAX_VALUE), clock), model,
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
        assertCommandFailure(new DueCommand(INDEX_FIRST_PATIENT, clock), new ModelManager(),
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_dueListsEveryoneButIndexUsesDisplayedList() throws Exception {
        Model model = createModel(alex, bernice);
        showPatientAtIndex(model, INDEX_SECOND_PATIENT);

        assertDue(new DueCommand(clockAt(7, 0, 0)), model,
                "08:00 Alex Yeoh: Aspirin 100mg",
                "08:00 Alex Yeoh: Metformin 500mg",
                "13:30 Bernice Yu: Amlodipine 5mg",
                "20:00 Alex Yeoh: Metformin 500mg");
        assertDue(new DueCommand(INDEX_FIRST_PATIENT, clockAt(7, 0, 0)), model, "13:30 Bernice Yu: Amlodipine 5mg");
        assertCommandFailure(new DueCommand(INDEX_SECOND_PATIENT, clockAt(7, 0, 0)), model,
                MESSAGE_INVALID_PATIENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_success_doesNotShowHelpOrExit() throws Exception {
        CommandResult result = new DueCommand(clockAt(9, 0, 0)).execute(createModel(alex));
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_runTwice_sameResult() throws Exception {
        Model model = createModel(alex);
        DueCommand command = new DueCommand(clockAt(9, 0, 0));
        assertEquals(command.execute(model), command.execute(model));
    }

    @Test
    public void equals() {
        Clock clock = clockAt(9, 0, 0);
        DueCommand command = new DueCommand(clock);
        DueCommand commandWithIndex = new DueCommand(INDEX_FIRST_PATIENT, clock);

        assertEquals(command, command);
        assertEquals(command, new DueCommand(clock));
        assertEquals(commandWithIndex, new DueCommand(INDEX_FIRST_PATIENT, clock));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, commandWithIndex);
        assertNotEquals(commandWithIndex, new DueCommand(INDEX_SECOND_PATIENT, clock));
        assertNotEquals(command, new DueCommand(clockAt(9, 1, 0)));
    }

    @Test
    public void toString_containsAllFields() {
        Clock clock = clockAt(9, 0, 0);
        assertEquals(DueCommand.class.getCanonicalName() + "{patientIndex=Optional.empty, clock=" + clock + "}",
                new DueCommand(clock).toString());
        assertEquals(DueCommand.class.getCanonicalName() + "{patientIndex=Optional[" + INDEX_FIRST_PATIENT
                + "], clock=" + clock + "}", new DueCommand(INDEX_FIRST_PATIENT, clock).toString());
    }

    /**
     * Asserts that executing {@code command} on {@code model} returns the {@code expectedLines} joined by
     * new lines, and that the patients and the displayed patient list of {@code model} are not changed.
     */
    private static void assertDue(DueCommand command, Model model, String... expectedLines) throws Exception {
        AddressBook addressBookBefore = new AddressBook(model.getAddressBook());
        List<Patient> displayedBefore = new ArrayList<>(model.getFilteredPatientList());

        CommandResult result = command.execute(model);

        assertEquals(String.join("\n", expectedLines), result.getFeedbackToUser());
        assertEquals(addressBookBefore, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPatientList());
    }

    private static Clock clockAt(int hour, int minute, int second) {
        return Clock.fixed(Instant.parse(String.format("2026-10-19T%02d:%02d:%02dZ", hour, minute, second)),
                ZoneOffset.UTC);
    }

    /**
     * Returns a medication that is taken at the times given as consecutive hour and minute pairs.
     */
    private static Medication medication(String description, int... hoursAndMinutes) {
        List<LocalTime> times = new ArrayList<>();
        for (int i = 0; i < hoursAndMinutes.length; i += 2) {
            times.add(LocalTime.of(hoursAndMinutes[i], hoursAndMinutes[i + 1]));
        }
        return new Medication(description, times);
    }

    private static Patient patient(String name, String nric, Medication... medications) {
        return new PatientBuilder().withName(name).withNric(nric).withMedications(medications).build();
    }

    private static Model createModel(Patient... patients) {
        Model model = new ModelManager();
        for (Patient patient : patients) {
            model.addPatient(patient);
        }
        return model;
    }
}
