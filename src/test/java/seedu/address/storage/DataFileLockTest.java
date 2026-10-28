package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataFileLockTest {
    private static final int LOCK_AVAILABLE = 0;
    private static final int LOCK_IN_USE = 2;
    private static final int PROCESS_TIMEOUT_SECONDS = 10;

    @TempDir
    public Path temporaryFolder;

    @Test
    public void tryAcquire_samePath_exclusiveUntilClosed() throws Exception {
        Path dataFile = temporaryFolder.resolve("data/mediconnect.json");
        Path lockFolder = temporaryFolder.resolve("locks");
        DataFileLock first = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow();
        try {
            assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
            assertFalse(Files.exists(dataFile.getParent()));
        } finally {
            first.close();
        }
        try (DataFileLock reopened = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow()) {
            first.close(); // Repeated close must not release the new owner's lock.
            assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
        }
    }

    @Test
    public void tryAcquire_dataCreatedAndReplaced_keepsSameLock() throws Exception {
        Path dataFile = temporaryFolder.resolve("data/mediconnect.json");
        Path lockFolder = temporaryFolder.resolve("locks");
        try (DataFileLock first = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow()) {
            Files.createDirectories(dataFile.getParent());
            Files.writeString(dataFile, "original");
            Path replacement = Files.writeString(temporaryFolder.resolve("replacement"), "replacement");
            Files.move(replacement, dataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
            assertEquals("replacement", Files.readString(dataFile));
        }
    }

    @Test
    public void tryAcquire_directoryAliases_shareLockButDifferentFilesDoNot() throws Exception {
        Path folder = Files.createDirectories(temporaryFolder.resolve("data/child"));
        Path dataFile = folder.getParent().resolve("mediconnect.json");
        Path alias = folder.resolve("../mediconnect.json");
        Path lockFolder = temporaryFolder.resolve("locks");
        try (DataFileLock first = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow();
                DataFileLock other = DataFileLock.tryAcquire(folder.resolve("other.json"), lockFolder).orElseThrow()) {
            assertTrue(DataFileLock.tryAcquire(alias, lockFolder).isEmpty());
        }
    }

    @Test
    public void tryAcquire_rootLevelDataPath_doesNotCreatePatientData() throws Exception {
        Path dataFile = temporaryFolder.getRoot().resolve("mediconnect-lock-test-" + temporaryFolder.getFileName());
        try (DataFileLock lock = DataFileLock.tryAcquire(dataFile, temporaryFolder.resolve("locks")).orElseThrow()) {
            assertFalse(Files.exists(dataFile));
            assertTrue(DataFileLock.tryAcquire(dataFile, temporaryFolder.resolve("locks")).isEmpty());
        }
    }

    @Test
    public void tryAcquire_invalidPaths_reportFailureWithoutChangingData() throws Exception {
        Path dataFile = Files.writeString(temporaryFolder.resolve("mediconnect.json"), "original");
        Path blockedFolder = Files.writeString(temporaryFolder.resolve("locks"), "keep this file");
        assertThrows(IOException.class, () -> DataFileLock.tryAcquire(dataFile, blockedFolder));
        assertThrows(IOException.class, () -> DataFileLock.tryAcquire(dataFile.getRoot(), temporaryFolder));
        assertThrows(NullPointerException.class, () -> DataFileLock.tryAcquire(null, temporaryFolder));
        assertThrows(NullPointerException.class, () -> DataFileLock.tryAcquire(dataFile, null));
        assertEquals("original", Files.readString(dataFile));
        assertEquals("keep this file", Files.readString(blockedFolder));
    }

    @Test
    public void tryAcquire_otherProcess_blockedUntilOwnerCloses() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        Path lockFolder = temporaryFolder.resolve("locks");
        try (DataFileLock first = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow()) {
            assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
            assertEquals(LOCK_IN_USE, runLockProcess(dataFile, lockFolder));
        }
        assertEquals(LOCK_AVAILABLE, runLockProcess(dataFile, lockFolder));
    }

    private int runLockProcess(Path dataFile, Path lockFolder) throws Exception {
        Process process = startLockProcess(dataFile, lockFolder);
        try {
            assertTrue(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS), "The lock probe must finish.");
            return process.exitValue();
        } finally {
            process.destroyForcibly();
        }
    }

    @Test
    public void tryAcquire_otherProcessHoldsLock_reopensAfterProcessTermination() throws Exception {
        Path dataFile = temporaryFolder.resolve("mediconnect.json");
        Path lockFolder = temporaryFolder.resolve("locks");
        Path readyFile = temporaryFolder.resolve("ready");
        Process process = startLockProcess(dataFile, lockFolder, readyFile.toString());
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(PROCESS_TIMEOUT_SECONDS);
            while (!Files.exists(readyFile) && process.isAlive() && System.nanoTime() < deadline) {
                TimeUnit.MILLISECONDS.sleep(10);
            }
            assertTrue(Files.exists(readyFile), "The other JVM must acquire its lock.");
            assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
            process.destroyForcibly();
            assertTrue(process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            try (DataFileLock reopened = DataFileLock.tryAcquire(dataFile, lockFolder).orElseThrow()) {
                assertTrue(DataFileLock.tryAcquire(dataFile, lockFolder).isEmpty());
            }
        } finally {
            process.destroyForcibly().waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    private Process startLockProcess(Path dataFile, Path lockFolder, String... extraArguments) throws Exception {
        String classPath = Path.of(DataFileLock.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LockProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> command = new ArrayList<>(List.of(ProcessHandle.current().info().command().orElseThrow(),
                "-cp", classPath, LockProcess.class.getName(), dataFile.toString(), lockFolder.toString()));
        command.addAll(List.of(extraArguments));
        return new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.INHERIT).start();
    }

    /** A separate JVM verifies that exclusion is enforced by the OS rather than just the static registry. */
    public static class LockProcess {
        /** Attempts a lock and reports availability through the process exit code. */
        public static void main(String[] args) throws IOException {
            Optional<DataFileLock> lock = DataFileLock.tryAcquire(Path.of(args[0]), Path.of(args[1]));
            if (lock.isEmpty()) {
                System.exit(LOCK_IN_USE);
            }
            try (DataFileLock held = lock.orElseThrow()) {
                if (args.length > 2) {
                    Files.writeString(Path.of(args[2]), "ready");
                    System.in.read();
                }
            }
        }
    }
}
