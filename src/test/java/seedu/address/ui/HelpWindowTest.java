package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests the real FXML viewer on the JavaFX application thread, without a network connection. */
public class HelpWindowTest {
    private static final String LOAD_ERROR =
            "Could not load the bundled User Guide. Click Reload guide to try again.";
    @TempDir
    public Path temporaryFolder;

    private HelpWindow window;

    @Test
    public void execute_successRejectionAndMalformedFile_logsNoPrivatePayload() throws Exception {
        String marker = "PrivateContactAlpha";
        String phone = "98765432101234";
        String rate = "876.54";
        StringBuilder records = new StringBuilder();
        seedu.address.commons.core.LogsCenter.getLogger(HelpWindowTest.class);
        Logger applicationLogger = Logger.getLogger("ab3");
        Level previousLevel = applicationLogger.getLevel();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.append(record.getMessage()).append(record.getThrown());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        applicationLogger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
        applicationLogger.addHandler(handler);
        try {
            onFx(() -> {
                StorageManager storage = new StorageManager(
                        new JsonAddressBookStorage(temporaryFolder.resolve("privacy.json")),
                        new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
                MainWindow main = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage),
                        storage.getAddressBookFilePath());
                main.fillInnerParts();
                TextField input = (TextField) main.getRoot().getScene().lookup("#commandTextField");
                input.setText("add n/" + marker + " p/" + phone + " l/J1 s/Math r/" + rate);
                input.fireEvent(new ActionEvent());
                input.setText("unknown " + marker + " " + phone + " " + rate);
                input.fireEvent(new ActionEvent());
                main.getRoot().hide();
                return null;
            });
            Path malformed = temporaryFolder.resolve("malformed.json");
            Files.writeString(malformed, "{\"persons\": [\"" + marker + phone + rate + "\"] invalid }");
            org.junit.jupiter.api.Assertions.assertThrows(
                    seedu.address.commons.exceptions.DataLoadingException.class, () -> {
                        new JsonAddressBookStorage(malformed).readAddressBook();
                    });
            assertFalse(records.toString().contains(marker), records.toString());
            assertFalse(records.toString().contains(phone), records.toString());
            assertFalse(records.toString().contains(rate), records.toString());
        } finally {
            applicationLogger.removeHandler(handler);
            applicationLogger.setLevel(previousLevel);
        }
    }

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            started.countDown();
        });
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @AfterEach
    public void closeWindow() throws Exception {
        onFx(() -> {
            if (window != null) {
                viewer().getEngine().load(null);
                window.hide();
            }
            return null;
        });
    }

    @Test
    public void show_bundledGuide_rendersContentAndClearsLoadingStatus() throws Exception {
        CompletableFuture<Void> loaded = onFx(() -> {
            window = new HelpWindow();
            assertFalse(window.isShowing());
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            window.show();
            assertTrue(window.isShowing());
            assertTrue(window.getRoot().isResizable());
            assertEquals("Loading User Guide...", status().getText());
            return result;
        });
        loaded.get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertTrue(viewer().getEngine().getDocument().getDocumentElement().getTextContent()
                    .contains("TutorFlow User Guide"));
            assertEquals(1, viewer().getEngine().getDocument().getElementsByTagName("nav").getLength());
            assertTrue((Boolean) viewer().getEngine().executeScript(
                    "document.querySelector('nav a[href=\"#recording-a-missed-lesson-lesson-missed\"]') !== null"));
            assertTrue(viewer().getEngine().getDocument().getDocumentElement().getTextContent()
                    .contains("A student must have between 1 and 10 subjects."));
            assertEquals("", status().getText());
            window.focus();
            window.hide();
            assertFalse(window.isShowing());
            window.show();
            assertEquals(Worker.State.SUCCEEDED, viewer().getEngine().getLoadWorker().getState());
            return null;
        });
    }

    @Test
    public void reloadButton_returnsToGuideAfterNavigation() throws Exception {
        CompletableFuture<Void> loaded = onFx(() -> {
            window = new HelpWindow();
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            viewer().getEngine().loadContent("<html><body>Another page</body></html>");
            return result;
        });
        loaded.get(10, TimeUnit.SECONDS);
        CompletableFuture<Void> reloaded = onFx(() -> {
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            reloadButton().fire();
            return result;
        });
        reloaded.get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertTrue(viewer().getEngine().getDocument().getDocumentElement().getTextContent()
                    .contains("TutorFlow User Guide"));
            assertEquals("", status().getText());
            return null;
        });
    }

    @Test
    public void reload_readFailure_showsErrorAndCanRetry() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        CompletableFuture<Void> reloaded = onFx(() -> {
            window = new HelpWindow(new Stage(), () -> {
                if (attempts.getAndIncrement() == 0) {
                    throw new IOException("Cannot read guide");
                }
                return UserGuide.loadHtml();
            });
            window.show();
            assertEquals("Could not read the bundled User Guide. Click Reload guide to try again.",
                    status().getText());
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            reloadButton().fire();
            return result;
        });
        reloaded.get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertEquals(2, attempts.get());
            assertEquals("", status().getText());
            return null;
        });
    }

    @Test
    public void show_failedPage_retriesBundledGuide() throws Exception {
        CompletableFuture<Void> failed = onFx(() -> {
            window = new HelpWindow();
            CompletableFuture<Void> result = awaitState(Worker.State.FAILED);
            viewer().getEngine().load(temporaryFolder.resolve("missing.html").toUri().toString());
            return result;
        });
        failed.get(10, TimeUnit.SECONDS);
        CompletableFuture<Void> loaded = onFx(() -> {
            assertEquals(LOAD_ERROR, status().getText());
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            window.show();
            return result;
        });
        loaded.get(10, TimeUnit.SECONDS);
        onFx(() -> {
            assertEquals("", status().getText());
            return null;
        });
    }

    @Test
    public void show_cancelledLoad_retriesBundledGuide() throws Exception {
        CompletableFuture<Void> loaded = onFx(() -> {
            window = new HelpWindow();
            viewer().getEngine().loadContent("<html><body>Cancelled page</body></html>");
            assertTrue(viewer().getEngine().getLoadWorker().cancel());
            assertEquals(Worker.State.CANCELLED, viewer().getEngine().getLoadWorker().getState());
            assertEquals(LOAD_ERROR, status().getText());
            CompletableFuture<Void> result = awaitState(Worker.State.SUCCEEDED);
            window.show();
            return result;
        });
        loaded.get(10, TimeUnit.SECONDS);
    }

    private WebView viewer() {
        return (WebView) window.getRoot().getScene().lookup("#userGuide");
    }

    private Label status() {
        return (Label) toolbar().getItems().stream().filter(Label.class::isInstance).findFirst().orElseThrow();
    }

    private Button reloadButton() {
        return (Button) toolbar().getItems().stream().filter(Button.class::isInstance).findFirst().orElseThrow();
    }

    private ToolBar toolbar() {
        return (ToolBar) window.getRoot().getScene().lookup(".tool-bar");
    }

    private CompletableFuture<Void> awaitState(Worker.State state) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        viewer().getEngine().getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == state) {
                result.complete(null);
            } else if (state == Worker.State.SUCCEEDED && newState == Worker.State.FAILED) {
                result.completeExceptionally(new AssertionError("Guide failed to load"));
            }
        });
        return result;
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
