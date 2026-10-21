package seedu.address.model.task;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;

/**
 * Represents a follow-up task in TutorFlow, attached to the student or the lesson that it is about (user story 31).
 * Guarantees: details are present and not null, except for the optional due date and lesson link, field values are
 * validated, immutable.
 *
 * <p>Every task has a permanent {@code id}, so that two tasks with the same text stay distinct. A task refers to
 * its student by name and phone number, as a lesson does, and {@code AddressBook} keeps these links up to date
 * when a student or a lesson is edited or deleted.
 */
public class Task {

    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final String MESSAGE_DESCRIPTION_CONSTRAINTS =
            "Task descriptions must be 1-" + MAX_DESCRIPTION_LENGTH + " characters and cannot contain line breaks.";

    private final String id;
    private final String description;
    private final LocalDate due; // optional, null if the task has no due date
    private final boolean isDone;
    private final Name studentName;
    private final Phone studentPhone;
    private final TaskLessonLink lessonLink; // null if the task is linked to the student alone

    /**
     * Every field must be present and not null, except for {@code due} and {@code lessonLink}.
     */
    public Task(String id, String description, LocalDate due, boolean isDone, Name studentName, Phone studentPhone,
            TaskLessonLink lessonLink) {
        requireAllNonNull(id, description, studentName, studentPhone);
        checkArgument(!id.isBlank(), "A task needs an id.");
        checkArgument(isValidDescription(description), MESSAGE_DESCRIPTION_CONSTRAINTS);
        this.id = id;
        this.description = description.strip();
        this.due = due;
        this.isDone = isDone;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.lessonLink = lessonLink;
    }

    /**
     * Creates a new open task about {@code student}, which is about {@code lesson} if that is given.
     * The {@code due} date and {@code lesson} are optional, and the other arguments must not be null.
     */
    public static Task create(String description, LocalDate due, Person student, Lesson lesson) {
        requireAllNonNull(description, student);
        return new Task(UUID.randomUUID().toString(), description, due, false, student.getName(),
                student.getPhone(), lesson == null ? null : TaskLessonLink.of(lesson));
    }

    /**
     * Returns true if a given string can be the description of a task: 1-200 printable characters on one line.
     */
    public static boolean isValidDescription(String test) {
        return test != null && !test.isBlank() && test.strip().length() <= MAX_DESCRIPTION_LENGTH
                && test.codePoints().noneMatch(character -> Character.isISOControl(character)
                        || character == ' ' || character == ' ');
    }

    public String getId() {
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

    public Name getStudentName() {
        return studentName;
    }

    public Phone getStudentPhone() {
        return studentPhone;
    }

    public Optional<TaskLessonLink> getLessonLink() {
        return Optional.ofNullable(lessonLink);
    }

    /**
     * Returns true if this task is about {@code person}, directly or through one of the lessons of the person.
     */
    public boolean isAbout(Person person) {
        return person.hasIdentity(studentName, studentPhone);
    }

    /**
     * Returns true if this task is about {@code lesson}.
     */
    public boolean isAbout(Lesson lesson) {
        return lessonLink != null && lessonLink.matches(lesson)
                && lesson.getStudentName().fullName.equalsIgnoreCase(studentName.fullName)
                && lesson.getStudentPhone().equals(studentPhone);
    }

    /**
     * Returns a copy of this task with the given description.
     */
    public Task withDescription(String newDescription) {
        return new Task(id, newDescription, due, isDone, studentName, studentPhone, lessonLink);
    }

    /**
     * Returns a copy of this task with the given due date, which is null to remove the due date.
     */
    public Task withDue(LocalDate newDue) {
        return new Task(id, description, newDue, isDone, studentName, studentPhone, lessonLink);
    }

    /**
     * Returns a completed copy of this task.
     */
    public Task markDone() {
        return new Task(id, description, due, true, studentName, studentPhone, lessonLink);
    }

    /**
     * Returns a copy of this task that is about {@code person} instead of the current student.
     */
    public Task withStudent(Person person) {
        return new Task(id, description, due, isDone, person.getName(), person.getPhone(), lessonLink);
    }

    /**
     * Returns a copy of this task that is about {@code lesson} instead of the lesson it is about now.
     */
    public Task withLesson(Lesson lesson) {
        return new Task(id, description, due, isDone, lesson.getStudentName(), lesson.getStudentPhone(),
                TaskLessonLink.of(lesson));
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
                && studentName.equals(otherTask.studentName)
                && studentPhone.equals(otherTask.studentPhone)
                && Objects.equals(lessonLink, otherTask.lessonLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, due, isDone, studentName, studentPhone, lessonLink);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("description", description)
                .add("due", due)
                .add("isDone", isDone)
                .add("studentName", studentName)
                .add("studentPhone", studentPhone)
                .add("lessonLink", lessonLink)
                .toString();
    }
}
