package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
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
    private final Clock clock;
    private boolean dirty;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, "");
    }

    /** Shares a fixed date source with parsers and commands for deterministic workflow tests. */
    public LogicManager(Model model, Storage storage, Clock clock) {
        this(model, storage, "", false, clock);
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
        this(model, storage, startupWarning, requiresInitialSave, Clock.systemDefaultZone());
    }

    /** Creates logic with explicit migration state and a shared date source. */
    public LogicManager(Model model, Storage storage, String startupWarning, boolean requiresInitialSave, Clock clock) {
        this.model = model;
        this.storage = storage;
        this.clock = Objects.requireNonNull(clock);
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
        addressBookParser = new AddressBookParser(clock);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.fine("Executing command");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        String before = snapshot();
        List<UUID> shownBefore = lessonSelection();
        boolean wasDirty = dirty;
        boolean changed;
        try {
            commandResult = command.execute(model);
        } finally {
            // An unavailable snapshot must never clear a previous save failure or permit an unchecked exit.
            dirty = true;
            changed = !before.equals(snapshot());
            dirty = wasDirty || changed;
        }

        String numbering = "";
        List<UUID> shownAfter = lessonSelection();
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
                throw new CommandException(e.getMessage() + numbering + "\n" + UNSAVED_WARNING, e);
            }
        }
        if (dirty) {
            return commandResult.withFeedback(commandResult.getFeedbackToUser() + "\n" + UNSAVED_WARNING);
        }
        return commandResult;
    }

    /**
     * Captures which lesson each visible number selects, before a mutation can reorder the live sorted list.
     * Only the lesson ids are compared: a lesson whose time, status or student name changed but that keeps its
     * number has not been renumbered.
     */
    private List<UUID> lessonSelection() {
        return model.getFilteredLessonList().stream().map(Lesson::getId).toList();
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
    public String getDashboard() {
        return new TodayCommand(LocalDate.now(clock)).execute(model).getFeedbackToUser();
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
