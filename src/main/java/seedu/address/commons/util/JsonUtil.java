package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Converts a Java object instance to JSON and vice versa.
 */
public class JsonUtil {

    /** Reason given when a file read by {@code readJsonFileStrictly} has a property that its class does not have. */
    public static final String MESSAGE_UNKNOWN_PROPERTY = "Unknown property \"%1$s\". Check its spelling.";

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    /** Character that a UTF-8 byte-order mark at the start of a file is read as. */
    private static final String BYTE_ORDER_MARK = "\uFEFF";

    private static ObjectMapper objectMapper = createObjectMapper();

    /*
     * Also rejects a property that the class does not have, e.g. a misspelled name, and a property given twice,
     * since their values would otherwise be dropped without a warning the next time the file is saved.
     */
    private static ObjectMapper strictObjectMapper = createObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return deserializeObjectFromJsonFile(jsonFile, classOfObjectToDeserialize, objectMapper);
    }

    private static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize,
            ObjectMapper mapper) throws IOException {
        String fileContent = FileUtil.readFromFile(jsonFile);
        return fromJsonString(removeByteOrderMark(fileContent), classOfObjectToDeserialize, mapper);
    }

    /**
     * Returns {@code fileContent} without the byte-order mark at its start, if it has one.
     * Some editors write one when saving a file as UTF-8, e.g., Notepad's "UTF-8 with BOM",
     * and the JSON standard (RFC 8259) lets readers ignore it.
     */
    private static String removeByteOrderMark(String fileContent) {
        return fileContent.startsWith(BYTE_ORDER_MARK)
                ? fileContent.substring(BYTE_ORDER_MARK.length())
                : fileContent;
    }

    /**
     * Returns the JSON object from the given file or {@code Optional.empty()} object if the file is not found.
     * If any values are missing from the file, default values will be used, as long as the file is a valid JSON file.
     *
     * @param filePath cannot be null.
     * @param classOfObjectToDeserialize JSON file has to correspond to the structure in the class given here.
     * @param <T> The type of the object to read.
     * @throws DataLoadingException if loading of the JSON file failed, including when it holds {@code null}.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        return readJsonFile(filePath, classOfObjectToDeserialize, objectMapper);
    }

    private static <T> Optional<T> readJsonFile(Path filePath, Class<T> classOfObjectToDeserialize,
            ObjectMapper mapper) throws DataLoadingException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.info("JSON file " + filePath + " found.");

        T readObject = readObjectFromFile(filePath, classOfObjectToDeserialize, mapper);
        if (readObject == null) {
            logger.warning("JSON file " + filePath + " holds null instead of an object");
            throw new DataLoadingException(new IOException("The JSON file holds null instead of an object"));
        }

        return Optional.of(readObject);
    }

    /**
     * Same as {@link #readJsonFile(Path, Class)}, but also rejects a property that
     * {@code classOfObjectToDeserialize} does not have, e.g. a misspelled name, and a property given twice.
     * Use it for data that is saved again after it is read, since their values would otherwise be lost.
     *
     * @throws DataLoadingException if loading of the JSON file failed, including when it has such a property.
     */
    public static <T> Optional<T> readJsonFileStrictly(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        return readJsonFile(filePath, classOfObjectToDeserialize, strictObjectMapper);
    }

    /**
     * Returns the object in the JSON file at {@code filePath}, which is {@code null} if the file holds only
     * {@code null}.
     *
     * @throws DataLoadingException if the file cannot be read or is not JSON data for the class.
     */
    private static <T> T readObjectFromFile(Path filePath, Class<T> classOfObjectToDeserialize,
            ObjectMapper mapper) throws DataLoadingException {
        try {
            return deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize, mapper);
        } catch (UnrecognizedPropertyException e) {
            // a property's name is not a value from the file, so it can be logged and shown, to help find a typo
            String message = String.format(MESSAGE_UNKNOWN_PROPERTY, e.getPropertyName());
            logger.warning("Error reading from JSON file " + filePath + ": " + message);
            throw new DataLoadingException(new IllegalValueException(message, e));
        } catch (IOException e) {
            // the message of e can quote values from the file, so only its type is logged
            logger.warning("Error reading from JSON file " + filePath + ": " + e.getClass().getSimpleName());
            throw new DataLoadingException(e);
        }
    }

    /**
     * Saves the JSON object to the specified file, through a temporary file in the same folder.
     * Replaces the existing file if it exists, creates a new file if it doesn't.
     *
     * @param objectToSave cannot be null.
     * @param filePath cannot be null. Its folder must exist.
     * @param <T> The type of the object to save.
     * @throws IOException if there was an error during writing to the file.
     */
    public static <T> void saveJsonFile(T objectToSave, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(objectToSave);

        // A save that fails partway, e.g. on a full disk, then leaves the last complete file in place.
        FileUtil.writeThroughTemporaryFile(filePath,
                temporaryFile -> serializeObjectToJsonFile(temporaryFile, objectToSave));
    }

    /**
     * Converts a given string representation of JSON data to an instance of a class.
     *
     * @param json The JSON data to convert.
     * @param instanceClass The class whose structure the JSON data must follow.
     * @param <T> The generic type to create an instance of.
     * @return The instance of T with the specified values in the JSON string.
     * @throws IOException if the string is not JSON data for the class, or has content after that data.
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        return fromJsonString(json, instanceClass, objectMapper);
    }

    private static <T> T fromJsonString(String json, Class<T> instanceClass, ObjectMapper mapper)
            throws IOException {
        try (JsonParser parser = mapper.getFactory().createParser(json)) {
            T instance = mapper.readValue(parser, instanceClass);
            checkNoContentAfterValue(parser);
            return instance;
        }
    }

    /**
     * Throws an {@code IOException} if anything other than whitespace follows the JSON value just read by
     * {@code parser}, e.g., text added to a data file after its data, which would otherwise be ignored.
     */
    private static void checkNoContentAfterValue(JsonParser parser) throws IOException {
        // the next token is null at the end of the input, and reading text that is not JSON throws instead
        if (parser.nextToken() != null) {
            throw new JsonParseException(parser, "Unexpected content after the JSON value");
        }
    }

    /**
     * Converts a given instance of a class into its JSON data string representation.
     *
     * @param instance The T object to be converted into the JSON string.
     * @param <T> The type of the instance.
     * @return JSON data representation of the given class instance, in string.
     * @throws JsonProcessingException if the instance cannot be converted into JSON.
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }
}
