package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataFileRecovery;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.VersionedAddressBookStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.4 alpha";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "tutorflow-v14.json");
    private static final Path LEGACY_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected String startupWarning = "";
    protected String startupRefusal; // null unless the data file was written by an unsupported version

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        VersionedAddressBookStorage addressBookStorage = createAddressBookStorage();
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        try {
            model = initModelManager(storage, userPrefs, addressBookStorage);
        } catch (DataLoadingException e) {
            // The data version is not supported, so nothing more is built: start() tells the user and stops
            startupRefusal = e.getCause().getMessage();
            return;
        }

        logic = new LogicManager(model, storage, startupWarning, addressBookStorage.requiresInitialSave());

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /** Creates the data storage used during startup. */
    protected VersionedAddressBookStorage createAddressBookStorage() {
        return new VersionedAddressBookStorage(ADDRESS_BOOK_FILE_PATH, LEGACY_FILE_PATH);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * An empty address book will be used if {@code storage}'s address book is not found,
     * or if recoverable errors occur when reading {@code storage}'s address book.
     * Unsupported data versions stop startup without moving or replacing the source.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs,
            VersionedAddressBookStorage addressBookStorage) throws DataLoadingException {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " with no student records.");
                startupWarning = "Welcome to TutorFlow. Add a student or type demo to preview sample data.";
            }
            initialData = addressBookOptional.orElseGet(AddressBook::new);
        } catch (DataLoadingException e) {
            if (e.getCause() instanceof UnsupportedSchemaException) {
                logger.severe(e.getCause().getMessage());
                throw e;
            }
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with no data.");
            startupWarning = DataFileRecovery.setAside(addressBookStorage.getReadSource());
            initialData = new AddressBook();
        }

        ModelManager modelManager = new ModelManager(initialData, userPrefs);
        // No lesson numbers are on screen at startup, so lesson commands must not act on a hidden list.
        modelManager.updateFilteredLessonList(unused -> false);
        return modelManager;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        if (startupRefusal != null) {
            // A user who starts the JAR from a file manager has no console, so the reason is shown in a window
            UiManager.showStartupRefusal(startupRefusal);
            Platform.exit();
            System.exit(1);
        }
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        if (model == null) {
            return;
        }
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
