package seedu.address.model.task;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Subject;

/**
 * Identifies the lesson that a task is about, by the subject, date, time and duration of the lesson. Together with
 * the student of the task this is the identity of a lesson. Immutable.
 */
public class TaskLessonLink {

    private final Subject subject;
    private final LessonDate date;
    private final LessonTime time;
    private final LessonDuration duration;

    /**
     * Every field must be present and not null.
     */
    public TaskLessonLink(Subject subject, LessonDate date, LessonTime time, LessonDuration duration) {
        requireAllNonNull(subject, date, time, duration);
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }

    /**
     * Returns the link to {@code lesson}.
     */
    public static TaskLessonLink of(Lesson lesson) {
        requireNonNull(lesson);
        return new TaskLessonLink(lesson.getSubject(), lesson.getDate(), lesson.getTime(), lesson.getDuration());
    }

    public Subject getSubject() {
        return subject;
    }

    public LessonDate getDate() {
        return date;
    }

    public LessonTime getTime() {
        return time;
    }

    public LessonDuration getDuration() {
        return duration;
    }

    /**
     * Returns true if {@code lesson} has the subject, date, time and duration of this link.
     * The student of the lesson is not compared.
     */
    public boolean matches(Lesson lesson) {
        requireNonNull(lesson);
        return subject.equals(lesson.getSubject())
                && date.equals(lesson.getDate())
                && time.equals(lesson.getTime())
                && duration.equals(lesson.getDuration());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskLessonLink otherLink)) {
            return false;
        }

        return subject.equals(otherLink.subject)
                && date.equals(otherLink.date)
                && time.equals(otherLink.time)
                && duration.equals(otherLink.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, date, time, duration);
    }

    @Override
    public String toString() {
        return subject + " " + date + " " + time + " " + duration;
    }
}
