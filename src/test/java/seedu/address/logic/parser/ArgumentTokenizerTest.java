package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix nSlash = new Prefix("n/");
    private final Prefix aSlash = new Prefix("a/");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void verifyNoInvalidPrefixes_freeText_allowsNonReservedSlashText() throws Exception {
        for (String text : List.of("and/or", "mg/ml", "mg/kg", "ml/hr", "N/A", "N/A,", "N/A.",
                "x/123", "xyz/", "and/or,")) {
            for (String args : List.of("a/" + text, " a/" + text, " a/Text " + text, " a/Text\t" + text)) {
                ArgumentTokenizer.verifyNoInvalidPrefixes(args, Set.of(), Set.of(aSlash), aSlash, pSlash);
            }
        }
        ArgumentTokenizer.verifyNoInvalidPrefixes(" a/d/o", Set.of("d/o"), Set.of(aSlash), aSlash);
    }

    @Test
    public void verifyNoInvalidPrefixes_freeText_rejectsReservedPrefixes() {
        for (Prefix prefix : CliSyntax.RESERVED_PREFIXES) {
            String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, prefix);
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(" a/" + prefix + "value",
                            Set.of(), Set.of(aSlash), aSlash));
            if (!prefix.equals(aSlash)) {
                assertThrows(ParseException.class, expected, () ->
                        ArgumentTokenizer.verifyNoInvalidPrefixes(" a/Text " + prefix + "value",
                                Set.of(), Set.of(aSlash), aSlash));
            }
        }
        assertThrows(ParseException.class,
                String.format(Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, "d/o,", "d/o"), () ->
                ArgumentTokenizer.verifyNoInvalidPrefixes(" a/d/o,", Set.of("d/o"), Set.of(aSlash), aSlash));
    }

    @Test
    public void verifyNoInvalidPrefixes_freeText_rejectsWrongCaseReservedPrefixes() {
        for (Prefix prefix : CliSyntax.RESERVED_PREFIXES) {
            String candidate = prefix.getPrefix().toUpperCase(Locale.ROOT);
            for (String args : List.of(" a/" + candidate + "value", " a/Text " + candidate + "value",
                    " a/Text\t" + candidate + "value")) {
                assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_PREFIX, candidate), () ->
                        ArgumentTokenizer.verifyNoInvalidPrefixes(args, Set.of(), Set.of(aSlash), aSlash, pSlash));
            }
        }
        for (String candidate : List.of("Nt/", "nT/")) {
            assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_PREFIX, candidate), () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(" a/Text " + candidate + "value",
                            Set.of(), Set.of(aSlash), aSlash));
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_notApplicableOutsideFreeText_rejected() {
        for (String args : List.of("N/A a/Text", " p/N/A a/Text", " a/Text p/N/A")) {
            assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "N/"), () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(args, Set.of(), Set.of(aSlash), aSlash, pSlash));
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_outsideFreeText_staysStrict() {
        for (String args : List.of("and/or a/Text", " p/and/or a/Text", " a/Text p/and/or",
                " a/Text p/123 and/or", " n/Patient\ta/and/or")) {
            assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "and/"), () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(args, Set.of(), Set.of(aSlash),
                            aSlash, pSlash, nSlash));
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_allowedTokens_success() throws Exception {
        ArgumentTokenizer.verifyNoInvalidPrefixes("p/21/3 custom/token CUSTOM/TOKEN",
                Set.of("custom/token"), pSlash);
        ArgumentTokenizer.verifyNoInvalidPrefixes(" -t value ^Q value 21/3 x1/123 /hello _/value https://example.com",
                Set.of(), dashT, hatQ);
    }

    @Test
    public void verifyNoInvalidPrefixes_firstValueWord_rejectsPrefixLikeText() {
        for (String token : List.of("x/123", "p/123", "value/suffix", "med/p/9")) {
            String prefix = token.substring(0, token.indexOf('/') + 1);
            assertThrows(ParseException.class, String.format(Messages.MESSAGE_UNKNOWN_PREFIX, prefix), () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes("p/" + token, Set.of(), pSlash));
        }
        assertThrows(ParseException.class,
                String.format(Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, "s/o,", "s/o"), () ->
                ArgumentTokenizer.verifyNoInvalidPrefixes("p/s/o,", Set.of("s/o"), pSlash));
    }

    @Test
    public void verifyNoInvalidPrefixes_firstValueWord_keepsAllowedText() throws Exception {
        for (String token : List.of("21/3", "https://example.com/path", "custom/token", "CUSTOM/TOKEN")) {
            ArgumentTokenizer.verifyNoInvalidPrefixes("p/" + token, Set.of("custom/token"), pSlash);
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_unknownToken_throwsParseException() {
        for (String token : List.of("x/123", "x/", "s/123", "d/123", "x/s/o", "s/oops",
                "A/B", "s/o", "d/o", "c/o", "s/o,")) {
            String prefix = token.substring(0, token.indexOf('/') + 1);
            String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, prefix);
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(token, Set.of(), pSlash));
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(" p/value\t" + token, Set.of(), pSlash));
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_punctuatedAllowedToken_throwsParseException() {
        for (String token : List.of("custom/token,", "CUSTOM/TOKEN.", "custom/token…", "custom/token𐄀")) {
            String unpunctuatedToken = token.substring(0, token.offsetByCodePoints(token.length(), -1));
            String expected = String.format(
                    Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, token, unpunctuatedToken);
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(token, Set.of("custom/token"), pSlash));
        }
    }

    @Test
    public void verifyNoInvalidPrefixes_longPunctuationRun_throwsPromptly() {
        String punctuation = ",".repeat(60_000);
        assertTimeout(Duration.ofSeconds(2), () -> {
            for (String token : List.of("x/" + punctuation + "a", "x/" + punctuation + "a,")) {
                String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "x/");
                assertThrows(ParseException.class, expected, () ->
                        ArgumentTokenizer.verifyNoInvalidPrefixes(token, Set.of("s/o"), pSlash));
            }
            String token = "s/o" + punctuation;
            String expected = String.format(Messages.MESSAGE_PUNCTUATED_SLASH_TOKEN, token, "s/o");
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(token, Set.of("s/o"), pSlash));
        });
    }

    @Test
    public void verifyNoInvalidPrefixes_allowedTokenSubstring_throwsParseException() {
        for (String token : List.of("custom/tokenized", "custom/token/extra", "custom/token🙂,")) {
            String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "custom/");
            assertThrows(ParseException.class, expected, () ->
                    ArgumentTokenizer.verifyNoInvalidPrefixes(token, Set.of("custom/token"), pSlash));
        }
    }

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_ignoredTokenStartsWithPrefix_tokenKeptInPreviousValue() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                " n/Muthu a/l Ramasamy a/Blk 1", Set.of("a/l"), nSlash, aSlash);

        assertArgumentPresent(argMultimap, nSlash, "Muthu a/l Ramasamy");
        assertArgumentPresent(argMultimap, aSlash, "Blk 1");
    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

}
