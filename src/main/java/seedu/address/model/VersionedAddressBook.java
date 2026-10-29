package seedu.address.model;

import java.util.List;

import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * An extension to AddressBook, with additional undo capabilities.
 */
public class VersionedAddressBook extends AddressBook {
    private DestructiveActionType lastDestructiveAction = DestructiveActionType.NONE;
    private ReadOnlyAddressBook lastState;

    public VersionedAddressBook() {
        super();
    }

    public VersionedAddressBook(ReadOnlyAddressBook toBeCopied) {
        super(toBeCopied);
    }

    private void saveState() {
        this.lastState = new AddressBook(this);
        // Resets, in case the action fails
        resetDestructiveAction();
    }

    private void saveDestructiveAction(DestructiveActionType actionType) {
        this.lastDestructiveAction = actionType;
    }

    private void resetDestructiveAction() {
        lastDestructiveAction = DestructiveActionType.NONE;
    }

    /**
     * Undoes the last destructive action performed on this AddressBook,
     * provided no attempts to modify it have been made.
     */
    public DestructiveActionType undoDestructiveAction() {
        if (lastDestructiveAction == DestructiveActionType.NONE) {
            return lastDestructiveAction;
        }
        DestructiveActionType type = lastDestructiveAction;
        // also resets destructive action
        this.resetData(this.lastState);
        return type;
    }

    // Overrides

    @Override
    public void setPersons(List<Person> persons) {
        super.setPersons(persons);
        resetDestructiveAction();
    }

    @Override
    public void setLessons(List<Lesson> lessons) {
        super.setLessons(lessons);
        resetDestructiveAction();
    }

    @Override
    public void setTasks(List<Task> tasks) {
        super.setTasks(tasks);
        resetDestructiveAction();
    }

    // Overrides
    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        this.resetDestructiveAction();
        super.resetData(newData);
    }

    @Override
    public void clearData() {
        saveState();
        super.clearData();
        saveDestructiveAction(DestructiveActionType.CLEAR);
    }

    @Override
    public void addPerson(Person p) {
        super.addPerson(p);
        resetDestructiveAction();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        super.setPerson(target, editedPerson);
        resetDestructiveAction();
    }

    @Override
    public void removePerson(Person key) {
        saveState();
        super.removePerson(key);
        saveDestructiveAction(DestructiveActionType.DELETE_STUDENT);
    }

    @Override
    public void addLesson(Lesson lesson) {
        super.addLesson(lesson);
        resetDestructiveAction();
    }

    @Override
    public void setLesson(Lesson target, Lesson editedLesson) {
        super.setLesson(target, editedLesson);
        resetDestructiveAction();
    }

    @Override
    public void removeLesson(Lesson key) {
        saveState();
        super.removeLesson(key);
        saveDestructiveAction(DestructiveActionType.DELETE_LESSON);
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        resetDestructiveAction();
    }

    @Override
    public void setTask(Task target, Task editedTask) {
        super.setTask(target, editedTask);
        resetDestructiveAction();
    }

    @Override
    public void removeTask(Task key) {
        saveState();
        super.removeTask(key);
        saveDestructiveAction(DestructiveActionType.DELETE_TASK);
    }

    @Override
    public void sortPersons(StudentOrder order) {
        super.sortPersons(order);
        resetDestructiveAction();
    }

    @Override
    public void addLedgerEntry(LedgerEntry entry) {
        super.addLedgerEntry(entry);
        resetDestructiveAction();
    }

    @Override
    public void setLedgerEntries(List<LedgerEntry> entries) {
        super.setLedgerEntries(entries);
        resetDestructiveAction();
    }

    @Override
    public void setHomework(Homework target, Homework replacement) {
        super.setHomework(target, replacement);
        resetDestructiveAction();
    }

    @Override
    public void addHomework(Homework item) {
        super.addHomework(item);
        resetDestructiveAction();
    }

    @Override
    public void removeHomework(Homework target) {
        saveState();
        super.removeHomework(target);
        saveDestructiveAction(DestructiveActionType.DELETE_HW);
    }

    @Override
    public void setTravelBuffer(int minutes) {
        super.setTravelBuffer(minutes);
        resetDestructiveAction();
    }
}
