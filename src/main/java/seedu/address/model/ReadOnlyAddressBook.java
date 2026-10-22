package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns an unmodifiable view of the lessons list.
     * This list will not contain any duplicate lessons.
     */
    ObservableList<Lesson> getLessonList();

    /** Returns the immutable view of recorded charges and payments. */
    ObservableList<LedgerEntry> getLedgerEntries();

    /**
     * Returns an unmodifiable view of the homework list.
     * Every implementation must provide it, so that none can be saved or copied without its homework.
     */
    ObservableList<Homework> getHomeworkList();
}
