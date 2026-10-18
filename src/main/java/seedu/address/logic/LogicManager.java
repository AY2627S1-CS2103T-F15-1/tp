package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SaveCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Your change was applied in this session but could not be "
            + "saved: %s. Fix this before closing TutorFlow.";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT = "Your change was applied in this session but "
            + "could not be saved: TutorFlow cannot write to %s. Fix this before closing TutorFlow.";

    public static final String UNSAVED_WARNING = "Unsaved changes: type 'save' to retry before closing TutorFlow.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final String startupWarning;
    private boolean dirty;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, "");
    }

    /**
     * Constructs a {@code LogicManager} that shows {@code startupWarning}, which may be empty, before the dashboard
     * when TutorFlow starts.
     */
    public LogicManager(Model model, Storage storage, String startupWarning) {
        this.model = model;
        this.storage = storage;
        this.startupWarning = startupWarning;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.fine("Executing command");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        AddressBook before = new AddressBook(model.getAddressBook());
        try {
            commandResult = command.execute(model);
        } finally {
            dirty |= !before.equals(model.getAddressBook());
        }

        if (dirty && (commandResult.isExit() || command instanceof SaveCommand
                || !before.equals(model.getAddressBook()))) {
            saveChanges();
        }
        if (dirty) {
            return new CommandResult(commandResult.getFeedbackToUser() + "\n" + UNSAVED_WARNING,
                    commandResult.isShowHelp(), commandResult.isExit());
        }
        return commandResult;
    }

    /** Saves pending changes, retaining the retry state if persistence fails. */
    private void saveChanges() throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
            dirty = false;
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

    }

    @Override
    public boolean hasUnsavedChanges() {
        return dirty;
    }

    @Override
    public String getDashboard() {
        return new TodayCommand(LocalDate.now()).execute(model).getFeedbackToUser();
    }

    @Override
    public String getStartupMessage() {
        return startupWarning.isEmpty() ? getDashboard() : startupWarning + "\n\n" + getDashboard();
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
