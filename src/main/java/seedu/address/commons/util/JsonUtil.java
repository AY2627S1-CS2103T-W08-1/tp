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

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Converts a Java object instance to JSON and vice versa.
 */
public class JsonUtil {

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
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
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.info("JSON file " + filePath + " found.");

        T readObject = readObjectFromFile(filePath, classOfObjectToDeserialize);
        if (readObject == null) {
            logger.warning("JSON file " + filePath + " holds null instead of an object");
            throw new DataLoadingException(new IOException("The JSON file holds null instead of an object"));
        }

        return Optional.of(readObject);
    }

    /**
     * Returns the object in the JSON file at {@code filePath}, which is {@code null} if the file holds only
     * {@code null}.
     *
     * @throws DataLoadingException if the file cannot be read or is not JSON data for the class.
     */
    private static <T> T readObjectFromFile(Path filePath, Class<T> classOfObjectToDeserialize)
            throws DataLoadingException {
        try {
            return deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
        } catch (IOException e) {
            // the message of e can quote values from the file, so only its type is logged
            logger.warning("Error reading from JSON file " + filePath + ": " + e.getClass().getSimpleName());
            throw new DataLoadingException(e);
        }
    }

    /**
     * Saves the JSON object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     *
     * @param objectToSave cannot be null.
     * @param filePath cannot be null.
     * @param <T> The type of the object to save.
     * @throws IOException if there was an error during writing to the file.
     */
    public static <T> void saveJsonFile(T objectToSave, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(objectToSave);

        serializeObjectToJsonFile(filePath, objectToSave);
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
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            T instance = objectMapper.readValue(parser, instanceClass);
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
