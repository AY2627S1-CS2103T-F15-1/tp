package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javafx.application.Platform;

/** Starts the JavaFX toolkit once for all the tests of a run, and runs test code on its application thread. */
final class FxTestSupport {
    private static final long TIMEOUT_SECONDS = 30;

    private static boolean isStarted;

    private FxTestSupport() {
    }

    /** Starts the toolkit unless a test class has already started it. */
    static synchronized void start() throws Exception {
        if (isStarted) {
            return;
        }
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
        } catch (IllegalStateException e) {
            started.countDown(); // another test class started it
        }
        assertTrue(started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "JavaFX did not start");
        isStarted = true;
    }

    /** Runs {@code action} on the application thread and returns what it returns. */
    static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /** Waits, on a slow machine for as long as half a minute, until {@code condition} holds on the FX thread. */
    static void awaitFx(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (!onFx(condition::getAsBoolean)) {
            assertTrue(System.nanoTime() < deadline,
                    "The condition did not hold within " + TIMEOUT_SECONDS + " seconds");
            Thread.sleep(25);
        }
    }
}
