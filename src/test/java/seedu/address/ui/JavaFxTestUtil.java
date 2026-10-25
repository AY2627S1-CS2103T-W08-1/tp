package seedu.address.ui;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Starts the JavaFX toolkit for GUI tests, and runs code on the JavaFX application thread.
 */
public class JavaFxTestUtil {

    private static final long TOOLKIT_TIMEOUT_SECONDS = 5;

    /** True once {@code startToolkit} has started the JavaFX toolkit, or found it running, in this JVM. */
    private static boolean isToolkitStarted;

    private JavaFxTestUtil() {} // prevents instantiation

    /**
     * Starts the JavaFX toolkit, if it is not running yet. In environments without a display, the toolkit is not
     * started and {@code isToolkitAvailable()} stays false, so that the calling tests can be skipped.
     */
    public static void startToolkit() {
        // A failed startup makes later JavaFX calls in the same JVM wait forever,
        // so do not attempt one when there is no display, e.g., on a headless CI runner.
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // toolkit was already started earlier in this JVM; checked below
        } catch (RuntimeException | LinkageError e) {
            return;
        }

        // keeps the toolkit running for later tests after a test closes its last window
        Platform.setImplicitExit(false);
        isToolkitStarted = isToolkitRunning();
    }

    /**
     * Returns true if {@code startToolkit} has started the JavaFX toolkit, or found it running, so that GUI tests
     * can run.
     */
    public static boolean isToolkitAvailable() {
        return isToolkitStarted;
    }

    /**
     * Returns true if the JavaFX application thread runs a task within {@code TOOLKIT_TIMEOUT_SECONDS}.
     */
    private static boolean isToolkitRunning() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.runLater(latch::countDown);
            return latch.await(TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Runs {@code task} on the JavaFX application thread, which windows must be created and shown on, and returns
     * its result.
     *
     * @throws Exception if {@code task} throws one, or does not finish within {@code TOOLKIT_TIMEOUT_SECONDS}.
     */
    public static <T> T runOnFxThread(Callable<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(task.call());
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
