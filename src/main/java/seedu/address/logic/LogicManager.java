package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SaveCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UndoSnapshot;
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
        this(model, storage, startupWarning, false);
    }

    /** Creates logic with an optional pending migration save. */
    public LogicManager(Model model, Storage storage, String startupWarning, boolean requiresInitialSave) {
        this.model = model;
        this.storage = storage;
        String warning = startupWarning;
        if (requiresInitialSave) {
            dirty = true;
            try {
                saveChanges();
            } catch (CommandException e) {
                warning += "\nMigration is pending. " + e.getMessage() + "\n" + UNSAVED_WARNING;
            }
        }
        this.startupWarning = warning;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.fine("Executing command");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        String before = snapshot();
        // A destructive command needs a private copy of the data from before it runs.
        AddressBook undoCopy = command.undoLabel().isPresent() ? new AddressBook(model.getAddressBook()) : null;
        List<String> shownBefore = lessonSelection();
        boolean wasDirty = dirty;
        boolean changed;
        try {
            commandResult = command.execute(model);
        } finally {
            // An unavailable snapshot must never clear a previous save failure or permit an unchecked exit.
            dirty = true;
            changed = !before.equals(snapshot());
            dirty = wasDirty || changed;
            // Also when the command failed after it changed data, so that an old snapshot cannot outlive the change
            updateUndoSnapshot(command, undoCopy, changed);
        }

        String numbering = "";
        List<String> shownAfter = lessonSelection();
        boolean preservesShownRows = shownAfter.size() >= shownBefore.size()
                && shownAfter.subList(0, shownBefore.size()).equals(shownBefore);
        if (changed && !preservesShownRows) {
            numbering = Messages.formatLessonList(model.getFilteredLessonList());
            if (commandResult.getDisplayedLessons().isEmpty()) {
                commandResult = commandResult.withFeedback(commandResult.getFeedbackToUser() + numbering)
                        .withDisplayedLessons(model.getFilteredLessonList());
            }
        }
        if (dirty && (commandResult.isExit() || command instanceof SaveCommand
                || changed)) {
            try {
                saveChanges();
            } catch (CommandException e) {
                throw new CommandException(e.getMessage() + numbering, e);
            }
        }
        if (dirty) {
            return commandResult.withFeedback(commandResult.getFeedbackToUser() + "\n" + UNSAVED_WARNING);
        }
        return commandResult;
    }

    /**
     * Keeps the undo step in step with what a command did. A destructive command that changed data keeps the
     * copy taken before it ran, any other change discards the step so that an undo cannot erase newer work,
     * and a command that changed nothing leaves it alone. The undo command manages the step itself.
     */
    private void updateUndoSnapshot(Command command, AddressBook undoCopy, boolean changed) {
        if (!changed || command instanceof UndoCommand) {
            return;
        }
        if (undoCopy != null) {
            model.setUndoSnapshot(new UndoSnapshot(command.undoLabel().orElseThrow(), undoCopy));
        } else {
            model.clearUndoSnapshot();
        }
    }

    /** Captures visible row identity and labels before mutation can reorder a live sorted list. */
    private List<String> lessonSelection() {
        return model.getFilteredLessonList().stream()
                .map(lesson -> lesson.getId() + " " + Messages.format(lesson) + " " + lesson.getStatus()).toList();
    }

    /** Captures serialized state without writing, keeping persistence comparison separate from domain equality. */
    private String snapshot() throws CommandException {
        try {
            return storage.snapshotAddressBook(model.getAddressBook());
        } catch (IOException e) {
            throw new CommandException("Could not inspect the data to save. Retry 'save' before closing TutorFlow.", e);
        }
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
