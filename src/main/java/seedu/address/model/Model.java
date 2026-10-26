package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;
    Predicate<Person> PREDICATE_SHOW_ACTIVE_PERSONS = person -> !person.isArchived();

    /** {@code Predicate} that always evaluates to true for lessons */
    Predicate<Lesson> PREDICATE_SHOW_ALL_LESSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Sets the persistent minimum gap between lessons at different physical venues. */
    void setTravelBuffer(int minutes);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /** Records a charge or payment for an existing student. */
    void addLedgerEntry(LedgerEntry entry);

    /** Completes a lesson and records its fee in one operation; free lessons return no charge. */
    Optional<LedgerEntry> chargeLesson(Lesson target, Lesson completedLesson);

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Sorts the persons in the address book in {@code order}, and keeps them in that order when persons are added or
     * edited later. Persons that compare as equal keep their relative order.
     */
    void sortPersons(StudentOrder order);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Returns true if a lesson with the same identity as {@code lesson} exists in the address book.
     */
    boolean hasLesson(Lesson lesson);

    /**
     * Adds the given lesson.
     * {@code lesson} must not already exist in the address book.
     */
    void addLesson(Lesson lesson);

    /**
     * Deletes the given lesson.
     * The lesson must exist in the address book.
     */
    void deleteLesson(Lesson target);

    /**
     * Replaces the given lesson {@code target} with {@code editedLesson}.
     * {@code target} must exist in the address book.
     * The lesson identity of {@code editedLesson} must not be the same as another existing lesson in the address book.
     */
    void setLesson(Lesson target, Lesson editedLesson);

    /**
     * Returns the lessons in the address book that overlap with the given lesson.
     */
    List<Lesson> getConflictingLessons(Lesson lesson);

    /** Returns an unmodifiable view of the filtered lesson list, in the order of the agenda. */
    ObservableList<Lesson> getFilteredLessonList();

    /**
     * Updates the filter of the filtered lesson list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredLessonList(Predicate<Lesson> predicate);

    /**
     * Adds the given task. No task with the same id may exist.
     */
    void addTask(Task task);

    /**
     * Returns the task with the given {@code id}, or empty if it does not exist (any more).
     */
    Optional<Task> findTask(UUID id);

    /**
     * Replaces the given task {@code target} with {@code editedTask}, which keeps the id of {@code target}.
     * {@code target} must exist in the address book.
     */
    void setTask(Task target, Task editedTask);

    /**
     * Deletes the given task.
     * The task must exist in the address book.
     */
    void deleteTask(Task target);

    /**
     * Returns the tasks as the last {@code task list} showed them. The task commands take their indices from this
     * list, which stays unchanged until the next {@code task list}, so that earlier numbers keep their meaning.
     */
    List<Task> getShownTasks();

    /**
     * Replaces the tasks that the task commands take their indices from with a copy of {@code tasks}.
     */
    void setShownTasks(List<Task> tasks);

    /** Adds an assignment belonging to a known student. */
    void addHomework(Homework homework);

    /** Replaces an assignment without changing its persistent identity. */
    void setHomework(Homework target, Homework replacement);

    /** Deletes an assignment. */
    void deleteHomework(Homework homework);

    /** Returns the immutable rows last explicitly numbered by hw list, including deleted placeholders. */
    List<Homework> getDisplayedHomework();

    /** Replaces the numbered selection only when a new list is explicitly displayed. */
    void showHomework(List<Homework> homework);
}
