package seedu.address.testutil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * A utility class to help with building Task objects.
 */
public class TaskBuilder {

    public static final String DEFAULT_DESCRIPTION = "Call parent";

    public static final UUID DEFAULT_ID = idOf("task-1");

    private UUID id = DEFAULT_ID;
    private String description = DEFAULT_DESCRIPTION;
    private LocalDate due;
    private boolean isDone;
    private Person student = TypicalPersons.ALICE;
    private Lesson lesson;
    private Homework homework;

    /** Creates a {@code TaskBuilder} for an open task about Alice without a due date. */
    public TaskBuilder() {}

    /** Returns the same id for the same {@code label}, so that tests can name their tasks. */
    public static UUID idOf(String label) {
        return UUID.nameUUIDFromBytes(label.getBytes(StandardCharsets.UTF_8));
    }

    /** Sets the permanent id of the {@code Task} that we are building. */
    public TaskBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    /** Sets the permanent id of the {@code Task} to the one that {@link #idOf(String)} gives {@code label}. */
    public TaskBuilder withId(String label) {
        return withId(idOf(label));
    }

    /** Sets the description of the {@code Task} that we are building. */
    public TaskBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    /** Sets the due date of the {@code Task} that we are building. */
    public TaskBuilder withDue(LocalDate due) {
        this.due = due;
        return this;
    }

    /** Marks the {@code Task} that we are building as done. */
    public TaskBuilder done() {
        this.isDone = true;
        return this;
    }

    /** Makes the {@code Task} that we are building about {@code student}. */
    public TaskBuilder withStudent(Person student) {
        this.student = student;
        this.lesson = null;
        this.homework = null;
        return this;
    }

    /** Makes the {@code Task} that we are building about {@code lesson} of the current student. */
    public TaskBuilder withLesson(Lesson lesson) {
        this.lesson = lesson;
        this.homework = null;
        return this;
    }

    /** Makes the {@code Task} that we are building about {@code homework}, which sets its student. */
    public TaskBuilder withHomework(Homework homework) {
        this.homework = homework;
        this.lesson = null;
        return this;
    }

    /** Returns the {@code Task} that we have built. */
    public Task build() {
        if (homework != null) {
            return new Task(id, description, due, isDone, homework.getStudentId(), null, homework.getId());
        }
        if (lesson != null) {
            UUID studentId = lesson.getStudentId().orElse(student.getId());
            return new Task(id, description, due, isDone, studentId, lesson.getId(), null);
        }
        return new Task(id, description, due, isDone, student.getId(), null, null);
    }
}
