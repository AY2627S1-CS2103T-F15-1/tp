package seedu.address.model;

import java.util.Optional;

import javafx.collections.ObservableList;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TravelGap;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

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

    /** Returns the offline travel buffer; older data sets use thirty minutes. */
    default int getTravelBuffer() {
        return TravelGap.DEFAULT_BUFFER;
    }

    /** Returns the order chosen with {@code sort}, or empty if the students were never sorted. */
    default Optional<StudentOrder> getStudentOrder() {
        return Optional.empty();
    }

    /** Returns the immutable view of recorded charges and payments. */
    ObservableList<LedgerEntry> getLedgerEntries();

    /**
     * Returns an unmodifiable view of the tasks list, in the order in which the tasks were added.
     * Tasks may have the same text, but no two tasks have the same id.
     */
    ObservableList<Task> getTaskList();

    /**
     * Returns an unmodifiable view of the homework list.
     * Every implementation must provide it, so that none can be saved or copied without its homework.
     */
    ObservableList<Homework> getHomeworkList();
}
