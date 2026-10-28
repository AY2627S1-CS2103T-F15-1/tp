package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
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
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests the real FXML viewer on the JavaFX application thread, without a network connection. */
public class HelpWindowTest {
    @TempDir
    public Path temporaryFolder;

    private HelpWindow window;

    @Test
    public void exit_failedSave_guardsCommandWindowAndMenuUntilRetrySucceeds() throws Exception {
        onFx(() -> {
            boolean[] fail = {true};
            JsonAddressBookStorage addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("exit.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    if (fail[0]) {
                        throw new IOException("Synthetic write failure");
                    }
                    super.saveAddressBook(addressBook);
                }
            };
            LogicManager logic = new LogicManager(new ModelManager(), new StorageManager(addressStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            MainWindow main = new MainWindow(new Stage(), logic, addressStorage.getAddressBookFilePath());
            main.fillInnerParts();
            main.show();
            try {
                TextField input = (TextField) main.getRoot().getScene().lookup("#commandTextField");
                input.setText("add n/Alice p/91234567 l/J1 s/Math r/100");
                input.fireEvent(new ActionEvent());
                assertTrue(logic.hasUnsavedChanges());
                input.setText("exit");
                input.fireEvent(new ActionEvent());
                assertTrue(main.getRoot().isShowing());
                main.getRoot().fireEvent(new WindowEvent(main.getRoot(), WindowEvent.WINDOW_CLOSE_REQUEST));
                assertTrue(main.getRoot().isShowing());
                MenuBar menu = (MenuBar) main.getRoot().getScene().lookup("#menuBar");
                menu.getMenus().getFirst().getItems().getFirst().fire();
                assertTrue(main.getRoot().isShowing());
                fail[0] = false;
                menu.getMenus().getFirst().getItems().getFirst().fire();
                assertFalse(main.getRoot().isShowing());
                assertFalse(logic.hasUnsavedChanges());
                assertEquals(1, addressStorage.readAddressBook().orElseThrow().getPersonList().size());
            } finally {
                main.getRoot().hide();
            }
            return null;
        });
    }

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
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            // Another UI test started JavaFX earlier in the same test run
            started.countDown();
        }
        Platform.setImplicitExit(false);
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @AfterEach
    public void closeWindow() throws Exception {
        onFx(() -> {
            if (window != null) {
                window.hide();
            }
            return null;
        });
    }

    @Test
    public void show_bundledGuide_rendersContentAndNavigatesContents() throws Exception {
        onFx(() -> {
            window = new HelpWindow();
            window.show();
            assertTrue(window.isShowing());
            assertTrue(window.getRoot().isResizable());
            String content = renderedText(viewer().getContent());
            assertTrue(content.contains("TutorFlow User Guide"));
            assertTrue(content.contains("A student must have between 1 and 10 subjects."));
            assertFalse(viewer().lookupAll("ImageView").isEmpty());
            assertFalse(viewer().lookupAll("GridPane").isEmpty());
            window.getRoot().getScene().getRoot().applyCss();
            window.getRoot().getScene().getRoot().layout();
            savePreview();
            var link = viewer().lookupAll(".hyperlink").stream()
                    .map(javafx.scene.control.Hyperlink.class::cast)
                    .filter(item -> item.getText().contains("Recording a missed lesson"))
                    .findFirst().orElseThrow();
            link.fire();
            assertTrue(viewer().getVvalue() > 0);
            assertEquals("", status().getText());
            window.focus();
            window.hide();
            assertFalse(window.isShowing());
            window.show();
            assertEquals(content, renderedText(viewer().getContent()));
            return null;
        });
    }

    @Test
    public void click_externalLinksAndImages_neverFetchesRemoteResources() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        var server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/private";
            onFx(() -> {
                window = new HelpWindow(new Stage(), () -> "<html><body><h1>Local guide</h1>"
                        + "<a href='" + url + "'>Remote</a><a target='_blank' href='" + url + "'>New window</a>"
                        + "<img src='" + url + "' alt='Blocked image'></body></html>");
                window.show();
                viewer().lookupAll(".hyperlink").forEach(node -> ((javafx.scene.control.Hyperlink) node).fire());
                assertTrue(status().getText().contains("offline"));
                assertTrue(renderedText(viewer().getContent()).contains("Local guide"));
                assertTrue(renderedText(viewer().getContent()).contains("Blocked image"));
                return null;
            });
            Thread.sleep(500); // Detect any accidental asynchronous network request.
            assertEquals(0, requests.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void reloadButton_restoresGuideAndResetsScroll() throws Exception {
        onFx(() -> {
            window = new HelpWindow();
            window.show();
            viewer().loadHtml("<html><body>Another page</body></html>");
            reloadButton().fire();
            assertTrue(renderedText(viewer().getContent()).contains("TutorFlow User Guide"));
            assertEquals(0, viewer().getVvalue());
            assertEquals("", status().getText());
            return null;
        });
    }

    @Test
    public void reload_readFailure_showsErrorAndCanRetry() throws Exception {
        onFx(() -> {
            AtomicInteger attempts = new AtomicInteger();
            window = new HelpWindow(new Stage(), () -> {
                if (attempts.getAndIncrement() == 0) {
                    throw new IOException("Cannot read guide");
                }
                return UserGuide.loadHtml();
            });
            window.show();
            assertTrue(status().getText().startsWith("Could not read"));
            reloadButton().fire();
            assertEquals(2, attempts.get());
            assertEquals("", status().getText());
            return null;
        });
    }

    @Test
    public void show_afterReadFailure_retries() throws Exception {
        onFx(() -> {
            AtomicInteger attempts = new AtomicInteger();
            window = new HelpWindow(new Stage(), () -> {
                if (attempts.getAndIncrement() == 0) {
                    throw new IOException("Cannot read guide");
                }
                return "<html><body>Recovered guide</body></html>";
            });
            window.show();
            window.hide();
            window.show();
            assertEquals("", status().getText());
            assertTrue(renderedText(viewer().getContent()).contains("Recovered guide"));
            return null;
        });
    }

    private static String renderedText(javafx.scene.Node node) {
        if (node instanceof javafx.scene.text.Text text) {
            return text.getText();
        }
        if (node instanceof javafx.scene.control.Hyperlink link) {
            return link.getText();
        }
        if (node instanceof javafx.scene.Parent parent) {
            return parent.getChildrenUnmodifiable().stream().map(HelpWindowTest::renderedText)
                    .collect(java.util.stream.Collectors.joining(" "));
        }
        return "";
    }

    private void savePreview() throws IOException {
        var snapshot = window.getRoot().getScene().snapshot(null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        var image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        Path output = Path.of("build", "help-preview.png");
        Files.createDirectories(output.getParent());
        javax.imageio.ImageIO.write(image, "png", output.toFile());
    }

    private GuideView viewer() {
        return (GuideView) window.getRoot().getScene().lookup("#userGuide");
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

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
