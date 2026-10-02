package seedu.address.model.lesson;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Represents a lesson with a student in TutorFlow.
 * Guarantees: details are present and not null, except for the optional venue, notes and cancel reason,
 * field values are validated, immutable.
 *
 * <p>A lesson refers to its student by the student's name and phone number, which together identify a
 * {@code Person}. {@code AddressBook} keeps this link up to date when a student is edited or deleted.
 */
public class Lesson {

    public static final String MESSAGE_CONSTRAINTS = "A lesson cannot run past midnight.";

    // Identity fields
    private final Name studentName;
    private final Phone studentPhone;
    private final Subject subject;
    private final LessonDate date;
    private final LessonTime time;
    private final LessonDuration duration;

    // Data fields
    private final Venue venue; // optional, null if the lesson has no venue
    private final LessonStatus status;
    private final String notes; // optional, null if no notes were recorded
    private final String cancelReason; // optional, null if no reason was recorded

    /**
     * Creates a scheduled lesson without a venue, notes or cancel reason.
     * Every field must be present and not null.
     */
    public Lesson(Name studentName, Phone studentPhone, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration) {
        this(studentName, studentPhone, subject, date, time, duration, null, LessonStatus.SCHEDULED, null, null);
    }

    /**
     * Every field must be present and not null, except for {@code venue}, {@code notes} and {@code cancelReason}.
     */
    public Lesson(Name studentName, Phone studentPhone, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration, Venue venue, LessonStatus status, String notes, String cancelReason) {
        requireAllNonNull(studentName, studentPhone, subject, date, time, duration, status);
        checkArgument(isValidTimeRange(time, duration), MESSAGE_CONSTRAINTS);
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
        this.status = status;
        this.notes = notes;
        this.cancelReason = cancelReason;
    }

    /**
     * Returns true if a lesson that starts at {@code time} and lasts for {@code duration} ends on the same day.
     * A lesson that runs past midnight would end before it starts, which breaks the check for overlapping lessons.
     */
    public static boolean isValidTimeRange(LessonTime time, LessonDuration duration) {
        return time.value.toSecondOfDay() / 60 + duration.value <= 24 * 60;
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

    public LessonDuration getDuration() {
        return duration;
    }

    /**
     * Returns the time at which the lesson ends. A lesson that ends at midnight ends at 00:00.
     */
    public LocalTime getEndTime() {
        return time.value.plusMinutes(duration.value);
    }

    public Optional<Venue> getVenue() {
        return Optional.ofNullable(venue);
    }

    public LessonStatus getStatus() {
        return status;
    }

    public Optional<String> getNotes() {
        return Optional.ofNullable(notes);
    }

    public Optional<String> getCancelReason() {
        return Optional.ofNullable(cancelReason);
    }

    public boolean isScheduled() {
        return status == LessonStatus.SCHEDULED;
    }

    /**
     * Returns true if this lesson is with the given {@code person}.
     */
    public boolean isWith(Person person) {
        return person.hasIdentity(studentName, studentPhone);
    }

    /**
     * Returns a copy of this lesson that is with {@code person} instead of the current student.
     */
    public Lesson withStudent(Person person) {
        assert person != null : "The new student of a lesson must not be null";
        return new Lesson(person.getName(), person.getPhone(), subject, date, time, duration, venue, status, notes,
                cancelReason);
    }

    /**
     * Returns true if both lessons are with the same student, ignoring the case of the student's name, and have the
     * same subject, date, start time and duration.
     * This defines a weaker notion of equality between two lessons.
     */
    public boolean isSameLesson(Lesson otherLesson) {
        if (otherLesson == this) {
            return true;
        }

        return otherLesson != null
                && otherLesson.studentName.fullName.equalsIgnoreCase(studentName.fullName)
                && otherLesson.studentPhone.equals(studentPhone)
                && otherLesson.subject.equals(subject)
                && otherLesson.date.equals(date)
                && otherLesson.time.equals(time)
                && otherLesson.duration.equals(duration);
    }

    /**
     * Returns true if this lesson overlaps with {@code otherLesson} by at least one minute.
     * Only scheduled lessons on the same date can overlap, and a lesson that starts when the other ends does not
     * overlap with it.
     */
    public boolean overlaps(Lesson otherLesson) {
        if (!isScheduled() || !otherLesson.isScheduled() || !date.equals(otherLesson.date)) {
            return false;
        }

        // A lesson that ends at midnight ends at 00:00, so compare the minutes since the start of the day instead
        return startMinute() < otherLesson.endMinute() && otherLesson.startMinute() < endMinute();
    }

    private int startMinute() {
        return time.value.toSecondOfDay() / 60;
    }

    private int endMinute() {
        assert startMinute() + duration.value <= 24 * 60 : "A lesson must end by midnight";
        return startMinute() + duration.value;
    }

    /**
     * Returns true if both lessons have the same identity and data fields.
     * This defines a stronger notion of equality between two lessons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
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
        return Objects.hash(studentName, studentPhone, subject, date, time, duration, venue, status, notes,
                cancelReason);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentName", studentName)
                .add("studentPhone", studentPhone)
                .add("subject", subject)
                .add("date", date)
                .add("time", time)
                .add("duration", duration)
                .add("venue", venue)
                .add("status", status)
                .add("notes", notes)
                .add("cancelReason", cancelReason)
                .toString();
    }
}
