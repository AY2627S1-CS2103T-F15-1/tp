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
import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.commands.SaveCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.view.DetailView;
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

    public static final String MESSAGE_SAVE_BEFORE_DEMO =
            "You have unsaved changes. Type 'save' first, so that the demo cannot lose them.";

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
        if (dirty && command instanceof DemoCommand demoCommand && demoCommand.isStart()) {
            throw new CommandException(MESSAGE_SAVE_BEFORE_DEMO);
        }
        boolean wasInDemo = model.isDemoMode();
        String before = snapshot();
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
            if (wasInDemo || model.isDemoMode()) {
                // sample data is never saved, and the data that comes back after a demo is what the file holds
                dirty = false;
            }
        }

        String numbering = "";
        List<String> shownAfter = lessonSelection();
        boolean preservesShownRows = shownAfter.size() >= shownBefore.size()
                && shownAfter.subList(0, shownBefore.size()).equals(shownBefore);
        if (changed && !preservesShownRows) {
            numbering = Messages.formatLessonList(model.getFilteredLessonList());
            if (commandResult.getDisplayedLessons().isEmpty()) {
                commandResult = commandResult.withAppendedText(numbering)
                        .withDisplayedLessons(model.getFilteredLessonList());
            }
        }
        if (dirty && (commandResult.isExit() || command instanceof SaveCommand
                || changed)) {
            try {
                saveChanges();
            } catch (CommandException e) {
                throw new CommandException(e.getMessage() + numbering + "\n" + UNSAVED_WARNING, e);
            }
        }
        if (dirty) {
            return commandResult.withAppendedText("\n" + UNSAVED_WARNING);
        }
        return commandResult;
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
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, describe(e)), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, describe(ioe)), ioe);
        }
    }

    /** Returns the reason a write failed, naming the kind of failure if the exception gives no message. */
    private static String describe(IOException e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return dirty;
    }

    @Override
    public boolean isDemoMode() {
        return model.isDemoMode();
    }

    @Override
    public String getDashboard() {
        return new TodayCommand(LocalDate.now()).execute(model).getFeedbackToUser();
    }

    @Override
    public DetailView getDashboardView() {
        return new TodayCommand(LocalDate.now()).execute(model).getDetailView().orElseThrow();
    }

    @Override
    public String getStartupWarning() {
        return startupWarning;
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
