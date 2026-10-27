package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.TravelGap;
import seedu.address.model.lesson.UniqueLessonList;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.task.Task;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison for persons and .isSameLesson comparison for lessons).
 * A lesson belongs to a person, so deleting or editing a person also updates that person's lessons.
 * A task is about a person and optionally one of their lessons or homework items, by permanent id, so deleting any
 * of them also deletes the tasks about it, while editing them leaves the tasks as they are.
 * Tasks may have the same text, but never the same id.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private static final Logger logger = LogsCenter.getLogger(AddressBook.class);

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueLessonList lessons = new UniqueLessonList();
    private int travelBuffer = TravelGap.DEFAULT_BUFFER;
    private StudentOrder studentOrder; // null until the students are sorted
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Task> unmodifiableTasks = FXCollections.unmodifiableObservableList(tasks);
    private final ObservableList<LedgerEntry> ledgerEntries = FXCollections.observableArrayList();
    private final ObservableList<LedgerEntry> unmodifiableLedgerEntries =
            FXCollections.unmodifiableObservableList(ledgerEntries);
    private final ObservableList<Homework> homework = FXCollections.observableArrayList();

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
        List<Task> replacement = List.copyOf(tasks);
        validateTasks(replacement);
        this.tasks.setAll(replacement);
    }

    /**
     * Checks that no two of the given {@code tasks} share an id, without changing any data.
     *
     * @throws IllegalArgumentException if two tasks have the same id.
     */
    private static void validateTasks(List<Task> tasks) {
        Set<UUID> ids = new HashSet<>();
        for (Task task : tasks) {
            if (!ids.add(task.getId())) {
                throw new IllegalArgumentException("Two tasks cannot have the same id.");
            }
        }
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        // Copy and validate everything first, so that a rejected replacement leaves this address book unchanged
        List<LedgerEntry> replacementEntries = List.copyOf(newData.getLedgerEntries());
        List<Task> replacementTasks = List.copyOf(newData.getTaskList());
        if (!TravelGap.isValidBuffer(newData.getTravelBuffer())) {
            throw new IllegalArgumentException(TravelGap.MESSAGE_CONSTRAINTS);
        }
        validateLedgerEntries(newData.getPersonList(), newData.getLessonList(), replacementEntries);
        validateTasks(replacementTasks);
        setPersons(newData.getPersonList());
        setLessons(newData.getLessonList());
        ledgerEntries.setAll(replacementEntries);
        tasks.setAll(replacementTasks);
        homework.setAll(newData.getHomeworkList());
        setTravelBuffer(newData.getTravelBuffer());
        studentOrder = newData.getStudentOrder().orElse(null);
        keepStudentOrder();
    }

    @Override
    public int getTravelBuffer() {
        return travelBuffer;
    }

    /** Sets the minimum gap for journeys between physical venues. */
    public void setTravelBuffer(int minutes) {
        if (!TravelGap.isValidBuffer(minutes)) {
            throw new IllegalArgumentException(TravelGap.MESSAGE_CONSTRAINTS);
        }
        travelBuffer = minutes;
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
        keepStudentOrder();
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        editedPerson = editedPerson.withId(target.getId());
        persons.setPerson(target, editedPerson);
        keepStudentOrder();
        updateStudentOfLessons(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
        ledgerEntries.removeIf(entry -> entry.isWith(key));
        homework.removeIf(item -> item.getStudentId().equals(key.getId()));
        boolean hasRemovedLessons = lessons.removeIf(lesson -> lesson.isWith(key));
        if (hasRemovedLessons) {
            logger.fine("Removed linked lessons");
        }
        if (tasks.removeIf(task -> task.isAbout(key))) {
            logger.fine("Removed linked tasks");
        }
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
        requireNonNull(target);
        requireNonNull(editedLesson);
        requireSameLessonId(target, editedLesson);
        // Tasks refer to the permanent lesson id, so moving, cancelling or rebooking never changes what they are about
        lessons.setLesson(target, editedLesson);
    }

    /** Completes a lesson and captures its fee atomically, returning no charge for a free lesson. */
    public Optional<LedgerEntry> chargeLesson(Lesson target, Lesson completedLesson) {
        requireNonNull(target);
        requireNonNull(completedLesson);
        requireSameLessonId(target, completedLesson);
        if ((!target.isScheduled() && target.getStatus() != LessonStatus.MISSED)
                || completedLesson.getStatus() != LessonStatus.COMPLETED) {
            throw new IllegalArgumentException("Only scheduled or missed lessons can be completed and charged.");
        }
        Lesson replacement = completedLesson;
        Person student = persons.asUnmodifiableObservableList().stream().filter(replacement::isWith)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Lesson student does not exist."));
        LedgerEntry charge = null;
        if (LedgerEntry.lessonFee(student, replacement).signum() > 0) {
            charge = LedgerEntry.charge(student, replacement);
            validateLedgerEntry(charge);
        }
        lessons.setLesson(target, replacement);
        if (charge != null) {
            ledgerEntries.add(charge);
            logger.fine("Recorded automatic lesson charge");
        }
        return Optional.ofNullable(charge);
    }

    /** Rejects replacement values that change a lesson's permanent identity. */
    private static void requireSameLessonId(Lesson target, Lesson replacement) {
        if (!target.getId().equals(replacement.getId())) {
            throw new IllegalArgumentException("A replacement lesson must retain its permanent ID.");
        }
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeLesson(Lesson key) {
        requireNonNull(key);
        Lesson stored = lessons.asUnmodifiableObservableList().stream()
                .filter(lesson -> lesson.getId().equals(key.getId())).findFirst()
                .orElseGet(() -> lessons.asUnmodifiableObservableList().stream()
                        .filter(key::equals).findFirst().orElse(key));
        if (ledgerEntries.stream().anyMatch(entry -> stored.getId().equals(entry.lessonId()))) {
            throw new IllegalArgumentException(
                    "Cannot delete a billed lesson. Its financial history must be retained.");
        }
        lessons.remove(key);
        // Only the tasks of this very lesson go, not those of another lesson that shares its slot
        tasks.removeIf(task -> task.isAbout(stored));
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
    public Optional<Task> findTask(UUID id) {
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

    /** Adds a charge or payment for an existing student. Duplicate entry IDs are rejected. */
    public void addLedgerEntry(LedgerEntry entry) {
        validateLedgerEntry(entry);
        ledgerEntries.add(entry);
        logger.fine("Recorded ledger entry");
    }

    /** Restores a complete ledger with one validation pass rather than repeated individual insertions. */
    public void setLedgerEntries(List<LedgerEntry> entries) {
        List<LedgerEntry> replacement = List.copyOf(entries);
        validateLedgerEntries(getPersonList(), getLessonList(), replacement);
        ledgerEntries.setAll(replacement);
    }

    private static void validateLedgerEntries(List<Person> students, List<Lesson> lessonList,
            List<LedgerEntry> entries) {
        Map<UUID, Person> studentsById = new HashMap<>();
        students.forEach(student -> studentsById.put(student.getId(), student));
        Map<UUID, Lesson> lessonsById = new HashMap<>();
        lessonList.forEach(lesson -> lessonsById.put(lesson.getId(), lesson));
        Set<UUID> entryIds = new HashSet<>();
        Set<UUID> chargedLessons = new HashSet<>();
        for (LedgerEntry entry : entries) {
            Person student = studentsById.get(entry.studentId());
            if (student == null) {
                throw new IllegalArgumentException("Ledger student does not exist.");
            }
            if (!entryIds.add(entry.id())) {
                throw new IllegalArgumentException("Duplicate ledger entry ID.");
            }
            if (entry.kind() == LedgerEntry.Kind.CHARGE) {
                Lesson lesson = lessonsById.get(entry.lessonId());
                if (lesson == null || !lesson.isWith(student)) {
                    throw new IllegalArgumentException("Ledger lesson does not exist or belongs to another student.");
                }
                if (!chargedLessons.add(entry.lessonId())) {
                    throw new IllegalArgumentException("That lesson already has a charge.");
                }
            }
        }
    }

    private void validateLedgerEntry(LedgerEntry entry) {
        requireNonNull(entry);
        Person student = persons.asUnmodifiableObservableList().stream().filter(entry::isWith).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ledger student does not exist."));
        if (entry.kind() == LedgerEntry.Kind.CHARGE) {
            if (lessons.asUnmodifiableObservableList().stream().noneMatch(lesson ->
                    lesson.getId().equals(entry.lessonId()) && lesson.isWith(student))) {
                throw new IllegalArgumentException("Ledger lesson does not exist or belongs to another student.");
            }
            ledgerEntries.stream().filter(existing -> entry.lessonId().equals(existing.lessonId()))
                    .findFirst().ifPresent(existing -> {
                        throw new IllegalArgumentException("That lesson already has a charge of "
                                + Ledger.format(existing.amount()) + ".");
                    });
        }
        if (ledgerEntries.stream().anyMatch(existing -> existing.id().equals(entry.id()))) {
            throw new IllegalArgumentException("Duplicate ledger entry ID.");
        }
    }

    /**
     * Sorts the persons in this {@code AddressBook} in {@code order}, and keeps that order when persons are added or
     * edited later.
     */
    public void sortPersons(StudentOrder order) {
        requireNonNull(order);
        studentOrder = order;
        persons.sort(order.comparator());
    }

    @Override
    public Optional<StudentOrder> getStudentOrder() {
        return Optional.ofNullable(studentOrder);
    }

    /** Puts the persons back in the order chosen with {@code sort}, if one was chosen. */
    private void keepStudentOrder() {
        if (studentOrder != null) {
            persons.sort(studentOrder.comparator());
        }
    }

    /** Adds a unique assignment belonging to a known student. */
    public void addHomework(Homework item) {
        validateHomework(item, null);
        homework.add(item);
    }

    /** Replaces an assignment while preserving its identity. */
    public void setHomework(Homework target, Homework replacement) {
        if (!target.getId().equals(replacement.getId()) || !target.getStudentId().equals(replacement.getStudentId())
                || !homework.contains(target)) {
            throw new IllegalArgumentException("Homework identity must be preserved.");
        }
        validateHomework(replacement, target);
        homework.set(homework.indexOf(target), replacement);
    }

    /** Removes an assignment. */
    public void removeHomework(Homework target) {
        if (!homework.remove(target)) {
            throw new IllegalArgumentException("Homework no longer exists.");
        }
        if (tasks.removeIf(task -> task.isAbout(target))) {
            logger.fine("Removed linked tasks");
        }
    }

    /** Rejects duplicate identities, duplicate assignments, and dangling student links. */
    private void validateHomework(Homework item, Homework target) {
        requireNonNull(item);
        if (persons.asUnmodifiableObservableList().stream()
                .noneMatch(person -> person.getId().equals(item.getStudentId()))) {
            throw new IllegalArgumentException("Homework student does not exist.");
        }
        List<Homework> others = homework.stream()
                .filter(existing -> target == null || !existing.getId().equals(target.getId())).toList();
        if (others.stream().anyMatch(existing -> existing.getId().equals(item.getId()))) {
            throw new IllegalArgumentException("Homework identity already exists.");
        }
        Optional<Homework> duplicate = others.stream().filter(existing -> existing.isSameAssignment(item)).findFirst();
        if (duplicate.isPresent()) {
            throw new IllegalArgumentException(String.format(Homework.MESSAGE_DUPLICATE,
                    duplicate.get().getDescription(), duplicate.get().getDueDate()));
        }
    }

    @Override
    public ObservableList<Homework> getHomeworkList() {
        return FXCollections.unmodifiableObservableList(homework);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("lessons", lessons)
                .add("ledgerEntries", ledgerEntries)
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
    public ObservableList<LedgerEntry> getLedgerEntries() {
        return unmodifiableLedgerEntries;
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
                && homework.equals(otherAddressBook.homework)
                && ledgerEntries.equals(otherAddressBook.ledgerEntries)
                && tasks.equals(otherAddressBook.tasks)
                && travelBuffer == otherAddressBook.travelBuffer
                && studentOrder == otherAddressBook.studentOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, lessons, homework, ledgerEntries, tasks, travelBuffer, studentOrder);
    }
}
