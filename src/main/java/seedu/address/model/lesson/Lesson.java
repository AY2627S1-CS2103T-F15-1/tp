package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Represents a Lesson in TutorFlow.
 * Guarantees: student details, subject, date, time, and duration are present and not null.
 */
public class Lesson {

    private final Name studentName;
    private final Phone studentPhone;
    private final Subject subject;
    private final LessonDate date;
    private final LessonTime time;
    private final Duration duration;

    // Optional fields
    private final Venue venue;
    private final LessonStatus status;
    private final String notes;
    private final String cancelReason;

    /**
     * Constructs a Lesson.
     */
    public Lesson(Name studentName, Phone studentPhone, Subject subject, LessonDate date, LessonTime time,
                  Duration duration, Venue venue, LessonStatus status, String notes, String cancelReason) {
        requireAllNonNull(studentName, studentPhone, subject, date, time, duration);
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
        this.status = status != null ? status : LessonStatus.SCHEDULED;
        this.notes = notes;
        this.cancelReason = cancelReason;
    }

    public Name getStudentName() {
        return studentName;
    }

    public Phone getStudentPhone() {
        return studentPhone;
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

    public Duration getDuration() {
        return duration;
    }

    public LessonTime getEndTime() {
        return new LessonTime(time.value.plusMinutes(duration.value).toString());
    }

    public Venue getVenue() {
        return venue;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public boolean isScheduled() {
        return status == LessonStatus.SCHEDULED;
    }

    public boolean isCompleted() {
        return status == LessonStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == LessonStatus.CANCELLED;
    }

    public boolean isMissed() {
        return status == LessonStatus.MISSED;
    }

    /**
     * Returns true if both lessons have the same student (name+phone), subject, date, time, and duration.
     */
    public boolean isSameLesson(Lesson otherLesson) {
        if (otherLesson == this) {
            return true;
        }

        return otherLesson != null
                && otherLesson.getStudentName().fullName.equalsIgnoreCase(getStudentName().fullName)
                && otherLesson.getStudentPhone().equals(getStudentPhone())
                && otherLesson.getSubject().equals(getSubject())
                && otherLesson.getDate().equals(getDate())
                && otherLesson.getTime().equals(getTime())
                && otherLesson.getDuration().equals(getDuration());
    }

    /**
     * Returns true if this lesson overlaps with the given lesson.
     * Both lessons must be on the same date, time ranges overlap by >= 1 minute, and both must be SCHEDULED.
     */
    public boolean overlaps(Lesson other) {
        if (other == null || !isScheduled() || !other.isScheduled()) {
            return false;
        }

        if (!date.equals(other.date)) {
            return false;
        }

        return time.value.isBefore(other.getEndTime().value) && other.time.value.isBefore(getEndTime().value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Lesson otherLesson)) {
            return false;
        }

        return studentName.equals(otherLesson.studentName)
                && studentPhone.equals(otherLesson.studentPhone)
                && subject.equals(otherLesson.subject)
                && date.equals(otherLesson.date)
                && time.equals(otherLesson.time)
                && duration.equals(otherLesson.duration)
                && Objects.equals(venue, otherLesson.venue)
                && status == otherLesson.status
                && Objects.equals(notes, otherLesson.notes)
                && Objects.equals(cancelReason, otherLesson.cancelReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentName, studentPhone, subject, date, time, duration,
                venue, status, notes, cancelReason);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("studentName", studentName)
                .add("studentPhone", studentPhone)
                .add("subject", subject)
                .add("date", date)
                .add("time", time)
                .add("duration", duration)
                .add("status", status);

        if (venue != null) {
            builder.add("venue", venue);
        }
        if (notes != null) {
            builder.add("notes", notes);
        }
        if (cancelReason != null) {
            builder.add("cancelReason", cancelReason);
        }

        return builder.toString();
    }
}
