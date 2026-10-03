package seedu.address.testutil;

import java.time.LocalDate;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * A utility class to help with building Lesson objects.
 */
public class LessonBuilder {

    public static final String DEFAULT_STUDENT_NAME = "Alice Pauline";
    public static final String DEFAULT_STUDENT_PHONE = "94351253";
    public static final String DEFAULT_SUBJECT = "Math";
    public static final String DEFAULT_DATE = "2026-10-12";
    public static final String DEFAULT_TIME = "16:30";
    public static final int DEFAULT_DURATION = 90;

    private Name studentName;
    private Phone studentPhone;
    private Subject subject;
    private LessonDate date;
    private LessonTime time;
    private LessonDuration duration;
    private Venue venue;
    private LessonStatus status;
    private String notes;
    private String cancelReason;

    /**
     * Creates a {@code LessonBuilder} with the default details: a scheduled lesson without a venue, notes or
     * cancel reason.
     */
    public LessonBuilder() {
        studentName = new Name(DEFAULT_STUDENT_NAME);
        studentPhone = new Phone(DEFAULT_STUDENT_PHONE);
        subject = new Subject(DEFAULT_SUBJECT);
        date = new LessonDate(DEFAULT_DATE);
        time = new LessonTime(DEFAULT_TIME);
        duration = new LessonDuration(DEFAULT_DURATION);
        status = LessonStatus.SCHEDULED;
    }

    /**
     * Initializes the LessonBuilder with the data of {@code lessonToCopy}.
     */
    public LessonBuilder(Lesson lessonToCopy) {
        studentName = lessonToCopy.getStudentName();
        studentPhone = lessonToCopy.getStudentPhone();
        subject = lessonToCopy.getSubject();
        date = lessonToCopy.getDate();
        time = lessonToCopy.getTime();
        duration = lessonToCopy.getDuration();
        venue = lessonToCopy.getVenue().orElse(null);
        status = lessonToCopy.getStatus();
        notes = lessonToCopy.getNotes().orElse(null);
        cancelReason = lessonToCopy.getCancelReason().orElse(null);
    }

    /**
     * Sets the student of the {@code Lesson} that we are building to {@code student}.
     */
    public LessonBuilder withStudent(Person student) {
        this.studentName = student.getName();
        this.studentPhone = student.getPhone();
        return this;
    }

    /**
     * Sets the student of the {@code Lesson} that we are building.
     */
    public LessonBuilder withStudent(String studentName, String studentPhone) {
        this.studentName = new Name(studentName);
        this.studentPhone = new Phone(studentPhone);
        return this;
    }

    /**
     * Sets the {@code Subject} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withSubject(String subject) {
        this.subject = new Subject(subject);
        return this;
    }

    /**
     * Sets the {@code LessonDate} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withDate(String date) {
        this.date = new LessonDate(date);
        return this;
    }

    /**
     * Sets the {@code LessonDate} of the {@code Lesson} that we are building to {@code date}.
     */
    public LessonBuilder withDate(LocalDate date) {
        return withDate(date.toString());
    }

    /**
     * Sets the {@code LessonTime} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withTime(String time) {
        this.time = new LessonTime(time);
        return this;
    }

    /**
     * Sets the {@code LessonDuration} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withDuration(int minutes) {
        this.duration = new LessonDuration(minutes);
        return this;
    }

    /**
     * Sets the {@code Venue} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withVenue(String venue) {
        this.venue = new Venue(venue);
        return this;
    }

    /**
     * Sets the {@code LessonStatus} of the {@code Lesson} that we are building.
     */
    public LessonBuilder withStatus(LessonStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the notes of the {@code Lesson} that we are building.
     */
    public LessonBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Sets the cancel reason of the {@code Lesson} that we are building.
     */
    public LessonBuilder withCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
        return this;
    }

    /**
     * Returns the {@code Lesson} that we have built.
     */
    public Lesson build() {
        return new Lesson(studentName, studentPhone, subject, date, time, duration, venue, status, notes,
                cancelReason);
    }
}
