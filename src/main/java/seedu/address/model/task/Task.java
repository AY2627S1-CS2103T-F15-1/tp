package seedu.address.model.task;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Represents a follow-up task in TutorFlow, attached to the student, lesson or homework item that it is about
 * (user story 31).
 * Guarantees: details are present and not null, except for the optional due date, lesson and homework links, field
 * values are validated, immutable.
 *
 * <p>Every task has a permanent {@code id}, so that two tasks with the same text stay distinct. A task refers to
 * its student, and to its lesson or homework item if it has one, by their permanent UUIDs. Editing a student,
 * moving, cancelling or rebooking a lesson, or editing homework therefore never changes what a task is about, and
 * {@code AddressBook} only has to delete the tasks about a record when that record is deleted.
 */
public class Task {

    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final String MESSAGE_DESCRIPTION_CONSTRAINTS =
            "Task descriptions must be 1-" + MAX_DESCRIPTION_LENGTH + " characters and cannot contain line breaks.";

    private final UUID id;
    private final String description;
    private final LocalDate due; // optional, null if the task has no due date
    private final boolean isDone;
    private final UUID studentId;
    private final UUID lessonId; // null unless the task is about a lesson
    private final UUID homeworkId; // null unless the task is about a homework item

    /**
     * Every field must be present and not null, except for {@code due}, {@code lessonId} and {@code homeworkId}.
     * A task is about at most one lesson or homework item.
     */
    public Task(UUID id, String description, LocalDate due, boolean isDone, UUID studentId, UUID lessonId,
            UUID homeworkId) {
        requireAllNonNull(id, description, studentId);
        checkArgument(isValidDescription(description), MESSAGE_DESCRIPTION_CONSTRAINTS);
        checkArgument(lessonId == null || homeworkId == null, "A task cannot be about a lesson and homework.");
        this.id = id;
        this.description = description.strip();
        this.due = due;
        this.isDone = isDone;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.homeworkId = homeworkId;
    }

    /**
     * Creates a new open task about {@code student} alone. The {@code due} date is optional, and the other
     * arguments must not be null.
     */
    public static Task create(String description, LocalDate due, Person student) {
        requireAllNonNull(description, student);
        return new Task(UUID.randomUUID(), description, due, false, student.getId(), null, null);
    }

    /**
     * Creates a new open task about {@code lesson} of {@code student}. The {@code due} date is optional, and the
     * other arguments must not be null.
     */
    public static Task create(String description, LocalDate due, Person student, Lesson lesson) {
        requireAllNonNull(description, student, lesson);
        return new Task(UUID.randomUUID(), description, due, false, student.getId(), lesson.getId(),
                null);
    }

    /**
     * Creates a new open task about {@code homework} of {@code student}. The {@code due} date is optional, and the
     * other arguments must not be null.
     */
    public static Task create(String description, LocalDate due, Person student, Homework homework) {
        requireAllNonNull(description, student, homework);
        return new Task(UUID.randomUUID(), description, due, false, student.getId(), null,
                homework.getId());
    }

    /**
     * Returns true if a given string can be the description of a task: 1-200 printable characters on one line.
     */
    public static boolean isValidDescription(String test) {
        return test != null && !test.isBlank() && test.strip().length() <= MAX_DESCRIPTION_LENGTH
                && test.codePoints().noneMatch(character -> Character.isISOControl(character)
                        || character == '\u2028' || character == '\u2029');
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Optional<LocalDate> getDue() {
        return Optional.ofNullable(due);
    }

    public boolean isDone() {
        return isDone;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Optional<UUID> getLessonId() {
        return Optional.ofNullable(lessonId);
    }

    public Optional<UUID> getHomeworkId() {
        return Optional.ofNullable(homeworkId);
    }

    /**
     * Returns true if this task is about {@code person}, directly or through one of their lessons or homework.
     */
    public boolean isAbout(Person person) {
        return studentId.equals(person.getId());
    }

    /**
     * Returns true if this task is about {@code lesson}. Another lesson in the same slot, such as a rebooking of
     * a cancelled lesson, is a different lesson.
     */
    public boolean isAbout(Lesson lesson) {
        return lessonId != null && lessonId.equals(lesson.getId());
    }

    /**
     * Returns true if this task is about {@code item}.
     */
    public boolean isAbout(Homework item) {
        return homeworkId != null && homeworkId.equals(item.getId());
    }

    /**
     * Returns a copy of this task with the given description.
     */
    public Task withDescription(String newDescription) {
        return new Task(id, newDescription, due, isDone, studentId, lessonId, homeworkId);
    }

    /**
     * Returns a copy of this task with the given due date, which is null to remove the due date.
     */
    public Task withDue(LocalDate newDue) {
        return new Task(id, description, newDue, isDone, studentId, lessonId, homeworkId);
    }

    /**
     * Returns a completed copy of this task.
     */
    public Task markDone() {
        return new Task(id, description, due, true, studentId, lessonId, homeworkId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Task otherTask)) {
            return false;
        }

        return id.equals(otherTask.id)
                && description.equals(otherTask.description)
                && Objects.equals(due, otherTask.due)
                && isDone == otherTask.isDone
                && studentId.equals(otherTask.studentId)
                && Objects.equals(lessonId, otherTask.lessonId)
                && Objects.equals(homeworkId, otherTask.homeworkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, due, isDone, studentId, lessonId, homeworkId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("description", description)
                .add("due", due)
                .add("isDone", isDone)
                .add("studentId", studentId)
                .add("lessonId", lessonId)
                .add("homeworkId", homeworkId)
                .toString();
    }
}
