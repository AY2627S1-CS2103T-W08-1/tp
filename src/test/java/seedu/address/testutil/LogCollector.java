package seedu.address.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Collects the messages that the logger of a class writes, until it is closed.
 */
public class LogCollector extends Handler implements AutoCloseable {

    private final Logger logger;
    private final Level originalLevel;
    private final List<String> messages = new ArrayList<>();

    /**
     * Starts to collect the messages of every level that the logger of {@code loggedClass} writes.
     */
    public LogCollector(Class<?> loggedClass) {
        logger = LogsCenter.getLogger(loggedClass);
        originalLevel = logger.getLevel();
        setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.addHandler(this);
    }

    /**
     * Returns the messages collected so far.
     */
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void publish(LogRecord logRecord) {
        messages.add(logRecord.getMessage());
    }

    @Override
    public void flush() { }

    @Override
    public void close() {
        logger.removeHandler(this);
        logger.setLevel(originalLevel);
    }
}
