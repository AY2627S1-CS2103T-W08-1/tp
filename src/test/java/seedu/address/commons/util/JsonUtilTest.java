package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.addUtf8ByteOrderMark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON read and write.
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    private Path temporaryFolder;

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(SerializableTestClass.JSON_STRING_REPRESENTATION, FileUtil.readFromFile(SERIALIZATION_FILE));
    }

    @Test
    public void saveJsonFile_existingFile_replacesFileAndLeavesNoTemporaryFile() throws IOException {
        Path file = temporaryFolder.resolve("serialize.json");
        FileUtil.writeToFile(file, "old content");
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.saveJsonFile(serializableTestClass, file);

        assertEquals(SerializableTestClass.JSON_STRING_REPRESENTATION, FileUtil.readFromFile(file));
        assertEquals(List.of(file), listFiles(temporaryFolder));
    }

    @Test
    public void saveJsonFile_serializationFails_keepsFileAndLeavesNoTemporaryFile() throws IOException {
        Path file = temporaryFolder.resolve("serialize.json");
        FileUtil.writeToFile(file, "old content");

        // an object with no properties cannot be serialized
        assertThrows(IOException.class, () -> JsonUtil.saveJsonFile(new Object(), file));

        assertEquals("old content", FileUtil.readFromFile(file));
        assertEquals(List.of(file), listFiles(temporaryFolder));
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), serializableTestClass.getName());
        assertEquals(SerializableTestClass.getListTestValues(), serializableTestClass.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), serializableTestClass.getMapOfIntegerToString());
    }

    @Test
    public void readJsonFileStrictly_unknownOrRepeatedProperty_throwsDataLoadingException() throws IOException {
        // Equivalence partitioning: a property that the class does not have, e.g. a misspelled one, and a
        // property given twice, whose values would otherwise be lost the next time the file is saved
        Path unknownPropertyFile = temporaryFolder.resolve("unknownProperty.json");
        Path repeatedPropertyFile = temporaryFolder.resolve("repeatedProperty.json");
        FileUtil.writeToFile(unknownPropertyFile, "{ \"nmae\" : \"Misspelled\" }");
        FileUtil.writeToFile(repeatedPropertyFile, "{ \"name\" : \"First\", \"name\" : \"Second\" }");

        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFileStrictly(unknownPropertyFile, SerializableTestClass.class));
        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFileStrictly(repeatedPropertyFile, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_unknownProperty_ignored() throws Exception {
        // preferences are read this way, so that a file from an older version still gives its known settings
        Path file = temporaryFolder.resolve("unknownProperty.json");
        FileUtil.writeToFile(file, "{ \"name\" : \"Kept\", \"nmae\" : \"Ignored\" }");

        assertEquals("Kept", JsonUtil.readJsonFile(file, SerializableTestClass.class).get().getName());
    }

    @Test
    public void readJsonFile_nullContent_throwsDataLoadingException() throws IOException {
        // Equivalence partitioning: a file holding only null, which is valid JSON but no object, tested both bare
        // and with whitespace around it, as Jackson skips whitespace before reading the value
        Path nullFile = temporaryFolder.resolve("null.json");
        Path paddedNullFile = temporaryFolder.resolve("paddedNull.json");
        FileUtil.writeToFile(nullFile, "null");
        FileUtil.writeToFile(paddedNullFile, "  null\n");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(nullFile, SerializableTestClass.class));
        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(paddedNullFile, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_byteOrderMarkBeforeData_returnsData() throws Exception {
        // Equivalence partitioning: valid data saved by an editor that starts the file with a UTF-8
        // byte-order mark
        Path file = temporaryFolder.resolve("byteOrderMark.json");
        FileUtil.writeToFile(file, SerializableTestClass.JSON_STRING_REPRESENTATION);
        addUtf8ByteOrderMark(file);

        SerializableTestClass readBack =
                JsonUtil.readJsonFile(file, SerializableTestClass.class).orElseThrow();

        assertEquals(SerializableTestClass.getNameTestValue(), readBack.getName());
        assertEquals(SerializableTestClass.getListTestValues(), readBack.getListOfLocalDateTimes());
    }

    @Test
    public void readJsonFile_onlyByteOrderMark_throwsDataLoadingException() throws Exception {
        // Boundary value analysis: a byte-order mark with no data after it, which is an empty file once it is
        // removed
        Path file = temporaryFolder.resolve("onlyByteOrderMark.json");
        FileUtil.writeToFile(file, "");
        addUtf8ByteOrderMark(file);

        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_twoByteOrderMarks_throwsDataLoadingException() throws Exception {
        // Boundary value analysis: only one byte-order mark is removed, so a second one is not JSON
        Path file = temporaryFolder.resolve("twoByteOrderMarks.json");
        FileUtil.writeToFile(file, SerializableTestClass.JSON_STRING_REPRESENTATION);
        addUtf8ByteOrderMark(file);
        addUtf8ByteOrderMark(file);

        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(file, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_whitespaceAfterValue_returnsValue() throws IOException {
        // Boundary value analysis: whitespace is the most that can follow the JSON value and still be accepted
        SerializableTestClass readBack = JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION + " \n\t", SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), readBack.getName());
    }

    @Test
    public void fromJsonString_oneCharacterAfterValue_throwsIoException() {
        // Boundary value analysis: a single extra closing brace is the least content after the JSON value
        assertRejectsContentAfterValue("}");
    }

    @Test
    public void fromJsonString_textAfterValue_throwsIoException() {
        // Equivalence partitioning: content after the JSON value that is not JSON
        assertRejectsContentAfterValue("\nTHIS IS NOT JSON");
    }

    @Test
    public void fromJsonString_secondValueAfterValue_throwsIoException() {
        // Equivalence partitioning: content after the JSON value that is itself valid JSON
        assertRejectsContentAfterValue("\n{}");
    }

    private static void assertRejectsContentAfterValue(String contentAfterValue) {
        assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION + contentAfterValue, SerializableTestClass.class));
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()

    /**
     * Returns the files in {@code folder}.
     */
    private static List<Path> listFiles(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.toList();
        }
    }
}
