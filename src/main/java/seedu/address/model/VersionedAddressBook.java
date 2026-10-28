package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * A Model based on ModelManager, with additional undo capabilities.
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

    private void saveState(DestructiveActionType actionType) {
        this.lastState = new AddressBook(this);
        this.lastDestructiveAction = actionType;
    }

    private void resetDestructiveAction() {
        lastDestructiveAction = DestructiveActionType.NONE;
    }

    public DestructiveActionType undoDestructiveAction() {
        if (lastDestructiveAction == DestructiveActionType.NONE) {
            return lastDestructiveAction;
        }
        this.resetData(this.lastState);
        DestructiveActionType type = lastDestructiveAction;
        resetDestructiveAction();
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
        this.saveState(DestructiveActionType.CLEAR);
        super.clearData();
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
        saveState(DestructiveActionType.DELETE_STUDENT);
        super.removePerson(key);
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
        saveState(DestructiveActionType.DELETE_LESSON);
        super.removeLesson(key);
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
        saveState(DestructiveActionType.DELETE_TASK);
        super.removeTask(key);
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
        saveState(DestructiveActionType.DELETE_HW);
        super.removeHomework(target);
    }
}