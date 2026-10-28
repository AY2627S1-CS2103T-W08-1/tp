package seedu.address.logic.parser;

import java.util.Set;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_NRIC = new Prefix("ic/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_DOB = new Prefix("dob/");
    public static final Prefix PREFIX_MEDICATION = new Prefix("med/");
    public static final Prefix PREFIX_ALLERGEN = new Prefix("al/");
    public static final Prefix PREFIX_CARE_INSTRUCTION = new Prefix("ci/");
    public static final Prefix PREFIX_ROLE = new Prefix("r/");
    public static final Prefix PREFIX_DATE = new Prefix("d/");
    public static final Prefix PREFIX_TIME = new Prefix("t/");
    public static final Prefix PREFIX_WITH = new Prefix("w/");
    public static final Prefix PREFIX_NOTE = new Prefix("nt/");

    /** All field prefixes, including ones that are not valid for the current command. */
    public static final Set<Prefix> RESERVED_PREFIXES = Set.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_NRIC, PREFIX_ADDRESS,
            PREFIX_DOB, PREFIX_MEDICATION, PREFIX_ALLERGEN, PREFIX_CARE_INSTRUCTION, PREFIX_ROLE,
            PREFIX_DATE, PREFIX_TIME, PREFIX_WITH, PREFIX_NOTE);

}
