package seedu.address.testutil;

import java.time.LocalDate;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;
import seedu.address.model.task.TaskLessonLink;

/**
 * A utility class to help with building Task objects.
 */
public class TaskBuilder {

    public static final String DEFAULT_DESCRIPTION = "Call parent";

    private String id = "task-1";
    private String description = DEFAULT_DESCRIPTION;
    private LocalDate due;
    private boolean isDone;
    private Person student = TypicalPersons.ALICE;
    private Lesson lesson;

    /** Creates a {@code TaskBuilder} for an open task about Alice without a due date. */
    public TaskBuilder() {}

    /** Sets the permanent id of the {@code Task} that we are building. */
    public TaskBuilder withId(String id) {
        this.id = id;
        return this;
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
        return this;
    }

    /** Makes the {@code Task} that we are building about {@code lesson}, which sets its student. */
    public TaskBuilder withLesson(Lesson lesson) {
        this.lesson = lesson;
        return this;
    }

    /** Returns the {@code Task} that we have built. */
    public Task build() {
        if (lesson != null) {
            return new Task(id, description, due, isDone, lesson.getStudentName(), lesson.getStudentPhone(),
                    TaskLessonLink.of(lesson));
        }
        return new Task(id, description, due, isDone, student.getName(), student.getPhone(), null);
    }
}
