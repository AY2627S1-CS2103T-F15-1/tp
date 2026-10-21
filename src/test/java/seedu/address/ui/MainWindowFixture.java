package seedu.address.ui;

import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.io.IOException;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Opens a real main window over a temporary data file, for the tests that drive the window as a user does: by
 * typing into the command box and reading what the window then shows. Everything runs on the JavaFX application
 * thread.
 */
final class MainWindowFixture {
    private final Path folder;
    private Stage stage;
    private LogicManager logic;
    private ModelManager model;
    private MainWindow window;
    private boolean shouldFailWrites;

    /** Keeps the data and preferences files of the windows it opens in {@code folder}. */
    MainWindowFixture(Path folder) {
        this.folder = folder;
    }

    /** Opens a window that holds Alice and starts with {@code startupWarning}. */
    MainWindow open(String startupWarning) throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        return open(book, new UserPrefs(), startupWarning);
    }

    /** Opens a window that holds {@code book}, with {@code prefs}, and starts with {@code startupWarning}. */
    MainWindow open(ReadOnlyAddressBook book, UserPrefs prefs, String startupWarning) throws Exception {
        model = new ModelManager(book, prefs);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldFailWrites) {
                    throw new IOException("Disk full");
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))), startupWarning);
        window = onFx(() -> {
            stage = new Stage();
            MainWindow opened = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            opened.fillInnerParts();
            opened.show();
            layout(opened);
            return opened;
        });
        return window;
    }

    /** Makes every later save fail as if the disk were full, or work again. */
    void failWrites(boolean shouldFail) {
        shouldFailWrites = shouldFail;
    }

    Stage stage() {
        return stage;
    }

    LogicManager logic() {
        return logic;
    }

    ModelManager model() {
        return model;
    }

    /** Applies the style sheet and lays out {@code window}, as the toolkit does before it draws a frame. */
    static void layout(MainWindow window) {
        window.getRoot().getScene().getRoot().applyCss();
        window.getRoot().getScene().getRoot().layout();
    }

    /** Finds the node that {@code selector} names in the open window. */
    Node find(String selector) {
        return window.getRoot().getScene().lookup(selector);
    }

    /** Types {@code command} into the command box, presses Enter and returns what the result box then says. */
    String type(String command) throws Exception {
        return onFx(() -> {
            TextField input = (TextField) find("#commandTextField");
            input.setText(command);
            input.fireEvent(new ActionEvent());
            layout(window);
            return resultBox(window);
        });
    }

    /** Returns what the result box says. */
    String resultBox() throws Exception {
        return onFx(() -> resultBox(window));
    }

    private static String resultBox(MainWindow window) {
        return ((TextArea) window.getRoot().getScene().lookup("#resultDisplay")).getText();
    }

    /** Closes the window, if one was opened. */
    void close() throws Exception {
        onFx(() -> {
            if (stage != null) {
                stage.hide();
            }
            return null;
        });
    }
}
