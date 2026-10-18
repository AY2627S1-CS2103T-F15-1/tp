package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.FollowUpTask;

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

    /** Returns persisted follow-ups; older datasets have none. */
    default ObservableList<FollowUpTask> getTaskList() {
        return FXCollections.emptyObservableList();
    }

    /** Returns the offline travel buffer; older datasets default to thirty minutes. */
    default int getTravelBufferMinutes() {
        return 30;
    }

    /** Returns append-only financial history; legacy models have no financial events. */
    default ObservableList<LedgerEntry> getLedgerEntries() {
        return FXCollections.emptyObservableList();
    }

    /** Returns persisted homework; pre-homework implementations represent the legacy empty domain. */
    default ObservableList<Homework> getHomeworkList() {
        return FXCollections.emptyObservableList();
    }
}
