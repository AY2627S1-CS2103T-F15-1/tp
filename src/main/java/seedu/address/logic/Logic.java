package seedu.address.logic;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.view.DetailView;
import seedu.address.logic.view.HomeworkRow;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns whether session changes still need to be saved. */
    default boolean hasUnsavedChanges() {
        return false;
    }

    /** Returns whether the user has finished or left the guided tour. */
    boolean isTourSeen();

    /** Remembers that the user has finished or left the guided tour, so that it is not offered at every start. */
    void markTourSeen();

    /** Returns whether the data is sample data from a demo, which is never saved. */
    boolean isDemoMode();

    /**
     * Returns the dashboard of what today requires, as shown by the {@code today} command.
     * The data is only read, so nothing is saved.
     */
    String getDashboard();

    /** Returns the dashboard as the detail pane draws it. The data is only read, so nothing is saved. */
    DetailView getDashboardView();

    /** Returns the warning about the data file that is shown when TutorFlow starts, or an empty text if none. */
    String getStartupWarning();

    /**
     * Returns what is shown when TutorFlow starts: a warning about the data file, if there is one, followed by the
     * dashboard.
     */
    String getStartupMessage();

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /** Returns an unmodifiable view of the lessons that the lesson commands count from. */
    ObservableList<Lesson> getFilteredLessonList();

    /** Returns the homework that the last {@code hw list} showed, as it is now and numbered as the commands do. */
    List<HomeworkRow> getHomeworkRows();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
