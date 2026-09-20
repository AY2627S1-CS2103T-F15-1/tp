package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Duration;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Jackson-friendly version of {@link Lesson}.
 */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String studentName;
    private final String studentPhone;
    private final String subject;
    private final String date;
    private final String time;
    private final int duration;
    private final String venue;
    private final String status;
    private final String notes;
    private final String cancelReason;

    /**
     * Constructs a {@code JsonAdaptedLesson} with the given lesson details.
     */
    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("studentName") String studentName,
            @JsonProperty("studentPhone") String studentPhone,
            @JsonProperty("subject") String subject,
            @JsonProperty("date") String date,
            @JsonProperty("time") String time,
            @JsonProperty("duration") int duration,
            @JsonProperty("venue") String venue,
            @JsonProperty("status") String status,
            @JsonProperty("notes") String notes,
            @JsonProperty("cancelReason") String cancelReason) {
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
     * Converts a given {@code Lesson} into this class for Jackson use.
     */
    public JsonAdaptedLesson(Lesson source) {
        studentName = source.getStudentName().fullName;
        studentPhone = source.getStudentPhone().value;
        subject = source.getSubject().value;
        date = source.getDate().toStorageString();
        time = source.getTime().toStorageString();
        duration = source.getDuration().value;
        venue = source.getVenue() != null ? source.getVenue().value : null;
        status = source.getStatus().name();
        notes = source.getNotes();
        cancelReason = source.getCancelReason();
    }

    /**
     * Converts this Jackson-friendly adapted lesson object into the model's {@code Lesson} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted lesson.
     */
    public Lesson toModelType() throws IllegalValueException {
        if (studentName == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(studentName)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelStudentName = new Name(studentName);

        if (studentPhone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(studentPhone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelStudentPhone = new Phone(studentPhone);

        if (subject == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Subject.class.getSimpleName()));
        }
        if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        final Subject modelSubject = new Subject(subject);

        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    LessonDate.class.getSimpleName()));
        }
        if (!LessonDate.isValidDate(date)) {
            throw new IllegalValueException(LessonDate.MESSAGE_CONSTRAINTS);
        }
        final LessonDate modelDate = new LessonDate(date);

        if (time == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    LessonTime.class.getSimpleName()));
        }
        if (!LessonTime.isValidTime(time)) {
            throw new IllegalValueException(LessonTime.MESSAGE_CONSTRAINTS);
        }
        final LessonTime modelTime = new LessonTime(time);

        if (!Duration.isValidDuration(duration)) {
            throw new IllegalValueException(Duration.MESSAGE_CONSTRAINTS);
        }
        final Duration modelDuration = new Duration(duration);

        Venue modelVenue = null;
        if (venue != null) {
            if (!Venue.isValidVenue(venue)) {
                throw new IllegalValueException(Venue.MESSAGE_CONSTRAINTS);
            }
            modelVenue = new Venue(venue);
        }

        if (status == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    LessonStatus.class.getSimpleName()));
        }
        if (!LessonStatus.isValidStatus(status)) {
            throw new IllegalValueException(LessonStatus.MESSAGE_CONSTRAINTS);
        }
        final LessonStatus modelStatus = LessonStatus.valueOf(status.toUpperCase());

        return new Lesson(modelStudentName, modelStudentPhone, modelSubject, modelDate, modelTime,
                modelDuration, modelVenue, modelStatus, notes, cancelReason);
    }

}
