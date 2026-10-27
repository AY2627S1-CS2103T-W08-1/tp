package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Prevents two application instances of the same user from opening the same patient data path.
 * The separate lock file stays in place because deleting it could let processes lock different files.
 */
public final class DataFileLock implements AutoCloseable {
    private static final String LOCK_FILE_SUFFIX = ".lock";
    private static final Set<Path> HELD_LOCK_FILES = new HashSet<>();

    private final Path lockFile;
    private final FileChannel channel;

    private DataFileLock(Path lockFile, FileChannel channel) {
        this.lockFile = lockFile;
        this.channel = channel;
    }

    /**
     * Attempts to lock {@code dataFile} using a metadata file in {@code lockDirectory}.
     * Returns empty if another instance already holds the lock. Does not create or change patient data.
     *
     * @throws IOException if the paths cannot be resolved or the lock cannot be opened or acquired.
     */
    public static synchronized Optional<DataFileLock> tryAcquire(Path dataFile, Path lockDirectory) throws IOException {
        requireNonNull(dataFile);
        requireNonNull(lockDirectory);
        Path identity = resolveDataPath(dataFile);
        String name = UUID.nameUUIDFromBytes(identity.toString().getBytes(StandardCharsets.UTF_8)) + LOCK_FILE_SUFFIX;
        Path lockFile = Files.createDirectories(lockDirectory).toRealPath().resolve(name);
        // Opening and closing another channel for a held lock can release that process's locks on some systems.
        if (HELD_LOCK_FILES.contains(lockFile)) {
            return Optional.empty();
        }
        FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        boolean acquired = false;
        try {
            if (channel.tryLock() == null) {
                return Optional.empty();
            }
            HELD_LOCK_FILES.add(lockFile);
            acquired = true;
            return Optional.of(new DataFileLock(lockFile, channel));
        } finally {
            if (!acquired) {
                channel.close();
            }
        }
    }

    private static Path resolveDataPath(Path dataFile) throws IOException {
        Path absolute = dataFile.toAbsolutePath();
        if (absolute.getFileName() == null) {
            throw new IOException("The data path must name a file.");
        }
        Path parent = absolute.getParent();
        Path existing = parent;
        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }
        if (existing == null) {
            throw new IOException("The data path has no accessible parent folder.");
        }
        // Resolve directory aliases, but not the data file: saving replaces that directory entry atomically.
        return existing.toRealPath().resolve(existing.relativize(parent)).resolve(absolute.getFileName()).normalize();
    }

    @Override
    public void close() throws IOException {
        synchronized (DataFileLock.class) {
            if (channel.isOpen()) {
                try {
                    channel.close();
                } finally {
                    HELD_LOCK_FILES.remove(lockFile);
                }
            }
        }
    }
}
