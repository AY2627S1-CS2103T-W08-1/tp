package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_NOT_TWO_INDEXES;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PATIENT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PATIENT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Phone;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_DATE = "2026-02-30";
    private static final String INVALID_PROVIDER = " ";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "91234567";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_DATE = "2026-10-25";
    private static final String VALID_PROVIDER = "Dr Tan";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PATIENT, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PATIENT, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseTwoIndexes_wrongNumberOfIndexes_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_NOT_TWO_INDEXES, () -> ParserUtil.parseTwoIndexes("1"));
        assertThrows(ParseException.class, MESSAGE_NOT_TWO_INDEXES, ()
            -> ParserUtil.parseTwoIndexes("1 2 3"));
    }

    @Test
    public void parseTwoIndexes_invalidIndex_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, () -> ParserUtil.parseTwoIndexes("1 a"));
    }

    @Test
    public void parseTwoIndexes_validInput_success() throws Exception {
        assertEquals(List.of(INDEX_FIRST_PATIENT, INDEX_SECOND_PATIENT),
                ParserUtil.parseTwoIndexes(" 1   2 "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_multipleSpacesBetweenWords_returnsCollapsedName() throws Exception {
        Name expectedName = new Name("Rachel Walker");
        assertEquals(expectedName, ParserUtil.parseName("Rachel   Walker"));
    }

    @Test
    public void parseName_curlyApostrophe_returnsNameWithStraightApostrophe() throws Exception {
        assertEquals(new Name("O'Brien"), ParserUtil.parseName("O’Brien"));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parsePhone_spacesAndHyphensBetweenDigits_returnsPhoneWithoutThem() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone("9123 4567"));
        assertEquals(expectedPhone, ParserUtil.parsePhone("+65 9123-4567"));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_tooLong_throwsParseException() {
        assertThrows(ParseException.class, Address.MESSAGE_TOO_LONG, () ->
                ParserUtil.parseAddress("a".repeat(Address.MAX_LENGTH + 1)));
    }

    @Test
    public void parseAddress_unicodeWhitespaceOnly_throwsParseException() {
        // an em space (U+2003) and a no-break space (U+00A0), which trim() does not remove
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(" "));
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress("  "));
    }

    @Test
    public void parseAddress_unicodeWhitespaceAround_returnsTrimmedAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(" " + VALID_ADDRESS + " "));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDate((String) null));
    }

    @Test
    public void parseDate_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Appointment.MESSAGE_DATE_CONSTRAINTS, ()
            -> ParserUtil.parseDate(INVALID_DATE));
        assertThrows(ParseException.class, Appointment.MESSAGE_DATE_CONSTRAINTS, ()
            -> ParserUtil.parseDate("25/10/2026"));
    }

    @Test
    public void parseDate_validValueWithWhitespace_returnsTrimmedDate() throws Exception {
        assertEquals(LocalDate.of(2026, 10, 25), ParserUtil.parseDate(WHITESPACE + VALID_DATE + WHITESPACE));
    }

    @Test
    public void parseProvider_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseProvider((String) null));
    }

    @Test
    public void parseProvider_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Appointment.MESSAGE_PROVIDER_CONSTRAINTS, ()
            -> ParserUtil.parseProvider(INVALID_PROVIDER));
    }

    @Test
    public void parseProvider_extraSpaces_returnsTrimmedAndCollapsedProvider() throws Exception {
        assertEquals(VALID_PROVIDER, ParserUtil.parseProvider(WHITESPACE + "Dr   Tan" + WHITESPACE));
    }

    @Test
    public void parseProvider_unicodeSpaces_treatedAsSpaces() throws Exception {
        // a no-break space, e.g. copied from a web page, which trim() and isBlank() do not treat as a space
        assertEquals(VALID_PROVIDER, ParserUtil.parseProvider(" Dr Tan "));
        assertThrows(ParseException.class, Appointment.MESSAGE_PROVIDER_CONSTRAINTS, ()
            -> ParserUtil.parseProvider(" "));
    }

    @Test
    public void parseProvider_tooLong_throwsParseException() {
        assertThrows(ParseException.class, Appointment.MESSAGE_PROVIDER_CONSTRAINTS, ()
            -> ParserUtil.parseProvider("a".repeat(Appointment.MAX_PROVIDER_LENGTH + 1)));
    }

    @Test
    public void parseNote_unicodeSpacesAround_returnsTrimmedNote() throws Exception {
        assertEquals("Bring  scans", ParserUtil.parseNote("  Bring  scans "));
        assertEquals("", ParserUtil.parseNote(" ")); // no note
    }

    @Test
    public void parseNote_tooLong_throwsParseException() {
        assertThrows(ParseException.class, Appointment.MESSAGE_NOTE_CONSTRAINTS, ()
            -> ParserUtil.parseNote("a".repeat(Appointment.MAX_NOTE_LENGTH + 1)));
    }



    @Test
    public void parseTime_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTime(null));
    }

    @Test
    public void parseTime_invalidInput_throwsParseException() {
        String message = Medication.MESSAGE_TIME_CONSTRAINTS;
        for (String invalidTime : new String[] {"", WHITESPACE, "24:00", "08:60", "9:60", "08:5", "0830", "08.30",
            "08:30:00", "ab:cd", "-1:30", "+8:30", "08:30pm", "08 : 30", "8:5", ":30", "1:2:3",
            "\uff10\uff18:\uff13\uff10", "0\u0663:30"}) {
            assertThrows(ParseException.class, message, () -> ParserUtil.parseTime(invalidTime));
        }
    }

    @Test
    public void parseTime_validInput_returnsLocalTime() throws Exception {
        assertEquals(LocalTime.MIDNIGHT, ParserUtil.parseTime("00:00"));
        assertEquals(LocalTime.of(8, 30), ParserUtil.parseTime("08:30"));
        assertEquals(LocalTime.of(23, 59), ParserUtil.parseTime("23:59"));
    }

    @Test
    public void parseTime_oneDigitHour_returnsLocalTime() throws Exception {
        assertEquals(LocalTime.of(9, 30), ParserUtil.parseTime("9:30"));
        assertEquals(LocalTime.of(8, 30), ParserUtil.parseTime("8:30"));
        assertEquals(LocalTime.of(0, 5), ParserUtil.parseTime("0:05"));
        assertEquals(LocalTime.of(9, 0), ParserUtil.parseTime(WHITESPACE + "9:00" + WHITESPACE));
    }

    @Test
    public void parseTime_oneDigitHourWithInvalidMinute_throwsParseException() {
        assertThrows(ParseException.class, Medication.MESSAGE_TIME_CONSTRAINTS, () -> ParserUtil.parseTime("9:60"));
    }

    @Test
    public void parseTime_inputWithWhitespace_returnsTrimmedLocalTime() throws Exception {
        assertEquals(LocalTime.of(8, 30), ParserUtil.parseTime(WHITESPACE + "08:30" + WHITESPACE));
    }

    @Test
    public void parseMedicationDescription_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseMedicationDescription(null));
    }

    @Test
    public void parseMedicationDescription_blank_throwsParseException() {
        String message = Medication.MESSAGE_DESCRIPTION_CONSTRAINTS;
        assertThrows(ParseException.class, message, () -> ParserUtil.parseMedicationDescription(""));
        assertThrows(ParseException.class, message, () -> ParserUtil.parseMedicationDescription(WHITESPACE));
    }

    @Test
    public void parseMedicationDescription_valid_returnsTrimmedDescription() throws Exception {
        assertEquals("Metformin 500mg", ParserUtil.parseMedicationDescription("  Metformin 500mg" + WHITESPACE));
    }
}
