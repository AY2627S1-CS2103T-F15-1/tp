package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
import seedu.address.model.lesson.UniqueLessonList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.task.FollowUpTask;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison for persons and .isSameLesson comparison for lessons).
 * A lesson belongs to a person, so deleting or editing a person also updates that person's lessons.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private static final Logger logger = LogsCenter.getLogger(AddressBook.class);

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueLessonList lessons = new UniqueLessonList();
    private final ObservableList<LedgerEntry> ledgerEntries = FXCollections.observableArrayList();
    private final ObservableList<Homework> homework = FXCollections.observableArrayList();

    private final ObservableList<FollowUpTask> tasks = FXCollections.observableArrayList();

    private int travelBufferMinutes = 30;

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons and Lessons in the {@code toBeCopied}
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
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setLessons(newData.getLessonList());
        homework.setAll(newData.getHomeworkList());
        ledgerEntries.setAll(newData.getLedgerEntries());
        setTravelBufferMinutes(newData.getTravelBufferMinutes());
        tasks.setAll(newData.getTaskList());
    }

    @Override
    public int getTravelBufferMinutes() {
        return travelBufferMinutes;
    }

    /** Sets the offline global buffer in minutes. */
    public void setTravelBufferMinutes(int minutes) {
        if (minutes < 0 || minutes > 180) {
            throw new IllegalArgumentException("Travel buffer must be 0-180 minutes.");
        }
        travelBufferMinutes = minutes;
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

        Person stored = storedPerson(target);
        editedPerson = editedPerson.withId(stored.getId());
        persons.setPerson(stored, editedPerson);
        updateStudentOfLessons(stored, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        Person stored = storedPerson(key);
        persons.remove(stored);
        tasks.removeIf(task -> task.studentId().equals(stored.getId()));
        ledgerEntries.removeIf(entry -> entry.studentId().equals(stored.getId()));
        homework.removeIf(item -> item.getStudentId().equals(stored.getId()));
        boolean hasRemovedLessons = lessons.removeIf(lesson -> lesson.isWith(stored));
        if (hasRemovedLessons) {
            logger.fine("Removed linked lessons");
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
        requireNonNull(editedLesson);

        Lesson stored = storedLesson(target);
        boolean hasHistory = ledgerEntries.stream().anyMatch(entry -> stored.getId().equals(entry.lessonId()));
        boolean hasTasks = tasks.stream().anyMatch(task -> task.linkType() == FollowUpTask.LinkType.LESSON
                && task.targetId().equals(stored.getId()));
        if (hasHistory || hasTasks) {
            boolean sameStudent = persons.asUnmodifiableObservableList().stream()
                    .anyMatch(person -> stored.isWith(person) && editedLesson.isWith(person));
            if (!stored.getId().equals(editedLesson.getId()) || !sameStudent) {
                throw new IllegalArgumentException(
                        "A lesson with linked history must retain its identity and student.");
            }
            if (Ledger.activeCharge(this, stored.getId()).isPresent()
                    && (editedLesson.isCancelled() || !stored.isSameLesson(editedLesson)
                    || !stored.getVenue().equals(editedLesson.getVenue()))) {
                throw new IllegalArgumentException("Reverse the active charge before cancelling or rescheduling.");
            }
        }
        lessons.setLesson(stored, editedLesson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeLesson(Lesson key) {
        Lesson stored = storedLesson(key);
        if (ledgerEntries.stream().anyMatch(entry -> stored.getId().equals(entry.lessonId()))) {
            throw new IllegalArgumentException("A lesson with financial history cannot be deleted.");
        }
        lessons.remove(stored);
        tasks.removeIf(task -> task.linkType() == FollowUpTask.LinkType.LESSON
                && task.targetId().equals(stored.getId()));
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
        tasks.removeIf(task -> task.linkType() == FollowUpTask.LinkType.HOMEWORK
                && task.targetId().equals(target.getId()));
    }

    /** Rejects duplicate identities, duplicate assignments, and dangling student links. */
    private void validateHomework(Homework item, Homework target) {
        requireNonNull(item);
        if (persons.asUnmodifiableObservableList().stream()
                .noneMatch(person -> person.getId().equals(item.getStudentId()))) {
            throw new IllegalArgumentException("Homework student does not exist.");
        }
        if (homework.stream().filter(existing -> target == null || !existing.getId().equals(target.getId()))
                .anyMatch(existing ->
                existing.getId().equals(item.getId()) || existing.isSameAssignment(item))) {
            throw new IllegalArgumentException("This student already has homework '" + item.getDescription()
                    + "' due " + item.getDueDate() + ".");
        }
    }

    @Override
    public ObservableList<Homework> getHomeworkList() {
        return FXCollections.unmodifiableObservableList(homework);
    }

    @Override
    public ObservableList<FollowUpTask> getTaskList() {
        return FXCollections.unmodifiableObservableList(tasks);
    }

    /** Adds a follow-up only when its target and owning student exist. */
    public void addTask(FollowUpTask task) {
        validateTask(task);
        if (tasks.stream().anyMatch(existing -> existing.id().equals(task.id()))) {
            throw new IllegalArgumentException("Duplicate task ID.");
        }
        tasks.add(task);
    }

    /** Replaces content or status without changing a task's identity or link. */
    public void setTask(FollowUpTask target, FollowUpTask replacement) {
        FollowUpTask stored = tasks.stream().filter(task -> task.id().equals(target.id())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task no longer exists."));
        if (!stored.id().equals(replacement.id()) || !stored.studentId().equals(replacement.studentId())
                || stored.linkType() != replacement.linkType() || !stored.targetId().equals(replacement.targetId())) {
            throw new IllegalArgumentException("Task identity and link must be preserved.");
        }
        validateTask(replacement);
        tasks.set(tasks.indexOf(stored), replacement);
    }

    /** Removes only the requested persistent task identity. */
    public void removeTask(FollowUpTask target) {
        if (!tasks.removeIf(task -> task.id().equals(target.id()))) {
            throw new IllegalArgumentException("Task no longer exists.");
        }
    }

    /** Rejects orphaned, mismatched, or non-payment financial links. */
    private void validateTask(FollowUpTask task) {
        requireNonNull(task);
        Person student = persons.asUnmodifiableObservableList().stream()
                .filter(person -> person.getId().equals(task.studentId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task student does not exist."));
        boolean valid = switch (task.linkType()) {
            case STUDENT -> task.targetId().equals(student.getId());
            case LESSON -> lessons.asUnmodifiableObservableList().stream()
                    .anyMatch(lesson -> lesson.getId().equals(task.targetId()) && lesson.isWith(student));
            case HOMEWORK -> homework.stream().anyMatch(item -> item.getId().equals(task.targetId())
                    && item.getStudentId().equals(student.getId()));
            case PAYMENT -> ledgerEntries.stream().anyMatch(entry -> entry.id().equals(task.targetId())
                    && entry.kind() == LedgerEntry.Kind.PAYMENT && entry.studentId().equals(student.getId()));
        };
        if (!valid) {
            throw new IllegalArgumentException("Task target does not exist or belongs to another student.");
        }
    }

    /** Appends a validated immutable event. Financial corrections use compensating entries. */
    public void addLedgerEntry(LedgerEntry entry) {
        Ledger.validateAppend(this, requireNonNull(entry));
        ledgerEntries.add(entry);
    }

    @Override
    public ObservableList<LedgerEntry> getLedgerEntries() {
        return FXCollections.unmodifiableObservableList(ledgerEntries);
    }

    /** Resolves a stored student before cascades; detached equal values must not substitute their new UUID. */
    private Person storedPerson(Person target) {
        return persons.asUnmodifiableObservableList().stream()
                .filter(person -> person.getId().equals(target.getId())).findFirst()
                .orElseGet(() -> persons.asUnmodifiableObservableList().stream()
                        .filter(target::equals).findFirst().orElse(target));
    }

    /** Matches the lesson list's ID-first lookup before applying financial integrity guards. */
    private Lesson storedLesson(Lesson target) {
        return lessons.asUnmodifiableObservableList().stream()
                .filter(lesson -> lesson.getId().equals(target.getId())).findFirst()
                .orElseGet(() -> lessons.asUnmodifiableObservableList().stream()
                        .filter(target::equals).findFirst().orElse(target));
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("lessons", lessons)
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
                && travelBufferMinutes == otherAddressBook.travelBufferMinutes
                && tasks.equals(otherAddressBook.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, lessons, homework, ledgerEntries, travelBufferMinutes, tasks);
    }
}
