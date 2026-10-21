package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.UniqueLessonList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.task.Task;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison for persons and .isSameLesson comparison for lessons).
 * A lesson belongs to a person, so deleting or editing a person also updates that person's lessons.
 * A task is about a person or one of their lessons, so deleting or editing either also updates the tasks about it.
 * Tasks may have the same text, but never the same id.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private static final Logger logger = LogsCenter.getLogger(AddressBook.class);

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueLessonList lessons = new UniqueLessonList();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Task> unmodifiableTasks = FXCollections.unmodifiableObservableList(tasks);

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons, Lessons and Tasks in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Replaces the contents of the lesson list with {@code lessons}.
     * {@code lessons} must not contain duplicate lessons.
     */
    public void setLessons(List<Lesson> lessons) {
        this.lessons.setLessons(lessons);
    }

    /**
     * Replaces the contents of the task list with {@code tasks}.
     * {@code tasks} must not contain two tasks with the same id.
     */
    public void setTasks(List<Task> tasks) {
        requireNonNull(tasks);
        Set<String> ids = new HashSet<>();
        for (Task task : tasks) {
            if (!ids.add(task.getId())) {
                throw new IllegalArgumentException("Two tasks cannot have the same id.");
            }
        }
        this.tasks.setAll(tasks);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setLessons(newData.getLessonList());
        setTasks(newData.getTaskList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        updateStudentOfLessons(target, editedPerson);
        updateStudentOfTasks(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
        boolean hasRemovedLessons = lessons.removeIf(lesson -> lesson.isWith(key));
        if (hasRemovedLessons) {
            logger.fine("Removed linked lessons");
        }
        if (tasks.removeIf(task -> task.isAbout(key))) {
            logger.fine("Removed linked tasks");
        }
    }

    /**
     * Makes the tasks about {@code target} the tasks about {@code editedPerson}, so that they are not left behind
     * when the name or the phone number that identifies the person is edited.
     */
    private void updateStudentOfTasks(Person target, Person editedPerson) {
        if (target.getName().equals(editedPerson.getName()) && target.getPhone().equals(editedPerson.getPhone())) {
            return;
        }

        logger.fine("Updating linked tasks");
        tasks.replaceAll(task -> task.isAbout(target) ? task.withStudent(editedPerson) : task);
    }

    /**
     * Makes the lessons of {@code target} the lessons of {@code editedPerson}, so that they are not left behind
     * when the name or the phone number that identifies the person is edited.
     */
    private void updateStudentOfLessons(Person target, Person editedPerson) {
        if (target.getName().equals(editedPerson.getName()) && target.getPhone().equals(editedPerson.getPhone())) {
            return;
        }

        logger.fine("Updating linked lessons");
        List<Lesson> updatedLessons = lessons.asUnmodifiableObservableList().stream()
                .map(lesson -> lesson.isWith(target) ? lesson.withStudent(editedPerson) : lesson)
                .collect(Collectors.toList());
        lessons.setLessons(updatedLessons);
    }

    //// lesson-level operations

    /**
     * Returns true if a lesson with the same identity as {@code lesson} exists in the address book.
     */
    public boolean hasLesson(Lesson lesson) {
        requireNonNull(lesson);
        return lessons.contains(lesson);
    }

    /**
     * Adds a lesson to the address book.
     * The lesson must not already exist in the address book.
     */
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    /**
     * Replaces the given lesson {@code target} in the list with {@code editedLesson}.
     * {@code target} must exist in the address book.
     * The lesson identity of {@code editedLesson} must not be the same as another existing lesson in the address book.
     */
    public void setLesson(Lesson target, Lesson editedLesson) {
        requireNonNull(editedLesson);

        lessons.setLesson(target, editedLesson);
        if (!target.isSameLesson(editedLesson)) {
            // The lesson was moved, so the tasks about it follow it to its new slot
            tasks.replaceAll(task -> task.isAbout(target) ? task.withLesson(editedLesson) : task);
        }
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeLesson(Lesson key) {
        lessons.remove(key);
        tasks.removeIf(task -> task.isAbout(key));
    }

    //// task-level operations

    /**
     * Adds a task to the address book.
     * No task with the same id may exist.
     */
    public void addTask(Task task) {
        requireNonNull(task);
        if (findTask(task.getId()).isPresent()) {
            throw new IllegalArgumentException("Two tasks cannot have the same id.");
        }
        tasks.add(task);
    }

    /**
     * Returns the task with the given {@code id}, or empty if it has been deleted.
     */
    public Optional<Task> findTask(String id) {
        requireNonNull(id);
        return tasks.stream().filter(task -> task.getId().equals(id)).findFirst();
    }

    /**
     * Replaces the given task {@code target} with {@code editedTask}, which must have the same id.
     * {@code target} must exist in the address book.
     */
    public void setTask(Task target, Task editedTask) {
        requireNonNull(editedTask);
        if (!target.getId().equals(editedTask.getId())) {
            throw new IllegalArgumentException("An edited task must keep its id.");
        }
        int index = tasks.indexOf(target);
        if (index < 0) {
            throw new IllegalArgumentException("The task to replace does not exist.");
        }
        tasks.set(index, editedTask);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeTask(Task key) {
        if (!tasks.remove(key)) {
            throw new IllegalArgumentException("The task to remove does not exist.");
        }
    }

    /**
     * Returns the lessons in the address book that overlap with {@code lesson}.
     */
    public List<Lesson> getConflictingLessons(Lesson lesson) {
        return lessons.getOverlapping(lesson);
    }

    /**
     * Sorts the persons in this {@code AddressBook} using the given {@code comparator}.
     */
    public void sortPersons(Comparator<Person> comparator) {
        persons.sort(comparator);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("lessons", lessons)
                .add("tasks", tasks)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Lesson> getLessonList() {
        return lessons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Task> getTaskList() {
        return unmodifiableTasks;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && lessons.equals(otherAddressBook.lessons)
                && tasks.equals(otherAddressBook.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, lessons, tasks);
    }
}
