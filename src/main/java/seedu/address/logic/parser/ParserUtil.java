package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.patient.Address;
import seedu.address.model.patient.Dob;
import seedu.address.model.patient.Medication;
import seedu.address.model.patient.Name;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Phone;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_NOT_TWO_INDEXES = "Exactly two indexes must be given.";

    /*
     * Apostrophes that phones, word processors and email programs type in names, e.g. in "O’Brien":
     * the left and right single quotation marks and the modifier letter apostrophe.
     */
    private static final String TYPED_APOSTROPHES_REGEX = "[\\u2018\\u2019\\u02BC]";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code oneBasedIndexes}, two indexes separated by whitespace, into a list of two
     * {@code Index}es, e.g. a patient's index followed by the number of one of their appointments.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if there are not exactly two indexes, or either index is invalid.
     */
    public static List<Index> parseTwoIndexes(String oneBasedIndexes) throws ParseException {
        requireNonNull(oneBasedIndexes);
        String[] indexes = oneBasedIndexes.trim().split("\\s+");
        if (indexes.length != 2) {
            throw new ParseException(MESSAGE_NOT_TWO_INDEXES);
        }
        return List.of(parseIndex(indexes[0]), parseIndex(indexes[1]));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed, and multiple spaces between words reduced to one.
     * Curly apostrophes (‘ and ’) and modifier letter apostrophes (ʼ) are replaced with straight ones (').
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim().replaceAll("\\s+", " ").replaceAll(TYPED_APOSTROPHES_REGEX, "'");
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String nric} into a {@code Nric}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code nric} is invalid.
     */
    public static Nric parseNric(String nric) throws ParseException {
        requireNonNull(nric);
        String trimmedNric = nric.trim();
        if (!Nric.isValidNric(trimmedNric)) {
            throw new ParseException(Nric.MESSAGE_CONSTRAINTS);
        }
        return new Nric(trimmedNric);
    }

    /**
     * Parses a {@code String dob} into a {@code Dob}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code dob} is invalid.
     */
    public static Dob parseDob(String dob) throws ParseException {
        requireNonNull(dob);
        String trimmedDob = dob.trim();
        if (!Dob.isValidDob(trimmedDob)) {
            throw new ParseException(Dob.MESSAGE_CONSTRAINTS);
        }
        return new Dob(trimmedDob);
    }

    /**
     * Parses a {@code String date} into a {@code LocalDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code date} is not a real date in YYYY-MM-DD format, or is before
     *     {@code Appointment.EARLIEST_DATE}.
     */
    public static LocalDate parseDate(String date) throws ParseException {
        requireNonNull(date);
        String trimmedDate = date.trim();
        if (!Appointment.isValidDate(trimmedDate)) {
            throw new ParseException(Appointment.MESSAGE_DATE_CONSTRAINTS);
        }
        return LocalDate.parse(trimmedDate);
    }

    /**
     * Parses a {@code String description} into a medication description.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code description} is blank or longer than the maximum length.
     */
    public static String parseMedicationDescription(String description) throws ParseException {
        requireNonNull(description);
        String trimmedDescription = description.trim();
        if (!Medication.isValidDescription(trimmedDescription)) {
            throw new ParseException(Medication.MESSAGE_DESCRIPTION_CONSTRAINTS);
        }
        if (!Medication.isDescriptionWithinMaxLength(Medication.normalizeDescription(trimmedDescription))) {
            throw new ParseException(Medication.MESSAGE_DESCRIPTION_TOO_LONG);
        }
        return trimmedDescription;
    }

    /**
     * Parses a {@code String time} in 24-hour HH:MM or H:MM format into a {@code LocalTime}.
     * Leading and trailing whitespaces will be trimmed, and an hour of one digit is read as if it had a leading 0.
     *
     * @throws ParseException if the given {@code time} is not one or two digits, a colon and two digits,
     *     or is not a valid time of day.
     */
    public static LocalTime parseTime(String time) throws ParseException {
        requireNonNull(time);
        String trimmedTime = time.trim();
        if (trimmedTime.matches("\\d:\\d{2}")) {
            trimmedTime = "0" + trimmedTime;
        }
        if (!Medication.isValidTime(trimmedTime)) {
            throw new ParseException(Medication.MESSAGE_TIME_CONSTRAINTS);
        }
        return Medication.toTime(trimmedTime);
    }

    /**
     * Parses a {@code String provider}, who an appointment is with.
     * Whitespaces between words, including Unicode ones such as a no-break space, are reduced to one space, and
     * leading and trailing ones are trimmed.
     *
     * @throws ParseException if the given {@code provider} is blank or too long.
     */
    public static String parseProvider(String provider) throws ParseException {
        requireNonNull(provider);
        String trimmedProvider = provider.replaceAll("(?U)\\s+", " ").trim();
        if (!Appointment.isValidProvider(trimmedProvider)) {
            throw new ParseException(Appointment.MESSAGE_PROVIDER_CONSTRAINTS);
        }
        return trimmedProvider;
    }

    /**
     * Parses a {@code String note} of an appointment, where an empty note means no note.
     * Leading and trailing whitespaces, including Unicode ones such as a no-break space, will be trimmed.
     *
     * @throws ParseException if the given {@code note} is too long.
     */
    public static String parseNote(String note) throws ParseException {
        requireNonNull(note);
        String trimmedNote = note.replaceAll("(?U)^\\s+|\\s+$", "");
        if (!Appointment.isValidNote(trimmedNote)) {
            throw new ParseException(Appointment.MESSAGE_NOTE_CONSTRAINTS);
        }
        return trimmedNote;
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * All whitespaces and hyphens will be removed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        // Removes the spaces and hyphens often written between digits, e.g. "+65 9123-4567".
        String compactPhone = phone.replaceAll("[\\s-]", "");
        if (!Phone.isValidPhone(compactPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(compactPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        // Trims Unicode whitespace too, as Address does not accept it at the start.
        String trimmedAddress = address.replaceAll("(?U)^\\s+|\\s+$", "");
        if (!trimmedAddress.matches(Address.VALIDATION_REGEX)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        if (!Address.isWithinMaxLength(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_TOO_LONG);
        }
        return new Address(trimmedAddress);
    }

}
