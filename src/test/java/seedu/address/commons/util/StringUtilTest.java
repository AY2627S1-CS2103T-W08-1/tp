package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    @Test
    public void stripUnicodeWhitespace_trimsEndsAndPreservesInternalText() {
        assertEquals("", StringUtil.stripUnicodeWhitespace(""));
        assertEquals("", StringUtil.stripUnicodeWhitespace("\u0000\u001c"));
        assertEquals("", StringUtil.stripUnicodeWhitespace(" \u00a0\u202f\u2007\u2003\u0085\t\n"));
        assertEquals("Call\u00a0first", StringUtil.stripUnicodeWhitespace("\u00a0Call\u00a0first\u202f"));
        assertEquals("Call  first", StringUtil.stripUnicodeWhitespace("Call  first"));
        assertEquals("☎", StringUtil.stripUnicodeWhitespace("\u0085☎\u2007"));
        assertThrows(NullPointerException.class, () -> StringUtil.stripUnicodeWhitespace(null));
    }

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: digits of other scripts
        assertFalse(StringUtil.isNonZeroUnsignedInteger("\uff12")); // Full-width digit
        assertFalse(StringUtil.isNonZeroUnsignedInteger("\u0662")); // Arabic-Indic digit
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1\uff12"));

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for normalizeEntry --------------------------------------

    @Test
    public void normalizeEntry_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeEntry(null));
    }

    @Test
    public void normalizeEntry_whitespace_collapsedAndTrimmed() {
        assertEquals("", StringUtil.normalizeEntry(""));
        assertEquals("", StringUtil.normalizeEntry(" \t\n\u00a0\u2003 "));
        assertEquals("a b c", StringUtil.normalizeEntry("  a \t b\n\n c  "));
        assertEquals("a b", StringUtil.normalizeEntry("a\u00a0\u00a0b")); // no-break spaces
        assertEquals("a b", StringUtil.normalizeEntry("a\u2003b")); // em space
    }

    @Test
    public void normalizeEntry_unicodeForms_composed() {
        assertEquals("caf\u00e9", StringUtil.normalizeEntry("cafe\u0301"));
        assertEquals("caf\u00e9", StringUtil.normalizeEntry("caf\u00e9"));
    }

    @Test
    public void normalizeEntry_keepsLetterCase() {
        assertEquals("Aspirin 100MG", StringUtil.normalizeEntry(" Aspirin  100MG "));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
