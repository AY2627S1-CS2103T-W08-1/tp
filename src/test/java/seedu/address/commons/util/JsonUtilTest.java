package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
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

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_nullContent_throwsDataLoadingException() throws IOException {
        Path nullFile = temporaryFolder.resolve("null.json");
        Path paddedNullFile = temporaryFolder.resolve("paddedNull.json");
        FileUtil.writeToFile(nullFile, "null");
        FileUtil.writeToFile(paddedNullFile, "  null\n");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(nullFile, SerializableTestClass.class));
        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(paddedNullFile, SerializableTestClass.class));
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
}
