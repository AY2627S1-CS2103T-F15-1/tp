package seedu.address.model;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

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

    /** Returns whether the user has finished or left the guided tour. */
    boolean isTourSeen();

    /** Remembers that the user has finished or left the guided tour, so that it is not offered again. */
    void setTourSeen();

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns whether the data is sample data from a demo. It stands in for the user's own data until the demo
     * ends, and it is never saved.
     */
    boolean isDemoMode();

    /**
     * Starts a demo: the present data is kept safe and {@code sampleData} stands in for it, with no list
     * numbering left over.
     *
     * @throws IllegalStateException if a demo is already running.
     */
    void startDemo(ReadOnlyAddressBook sampleData);

    /**
     * Ends the demo and brings back the data that was kept when it started, with no list numbering left over.
     *
     * @throws IllegalStateException if no demo is running.
     */
    void endDemo();

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
     * Sorts the persons in the address book using the given {@code comparator}.
     * Persons that compare as equal keep their relative order.
     */
    void sortPersons(Comparator<Person> comparator);

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
