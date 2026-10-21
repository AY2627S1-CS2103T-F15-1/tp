package seedu.address.storage;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Jackson-friendly version of {@link Lesson}.
 */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";
    public static final String INVALID_SERIES_ID_MESSAGE = "Lesson's seriesId is not a valid ID.";

    private String id;
    private String studentId;
    private String academicLevel;
    private String preparation;
    private String seriesId;
    private final String studentName;
    private final String studentPhone;
    private final String subject;
    private final String date;
    private final String time;
    private final Integer duration;
    private final String venue;
    private final String status;
    private final String notes;
    private final String cancelReason;

    /**
     * Constructs a {@code JsonAdaptedLesson} with the given lesson details.
     * The {@code venue}, {@code notes} and {@code cancelReason} are optional, and may be null. A missing
     * {@code status} means that the lesson is scheduled.
     */
    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("studentName") String studentName,
            @JsonProperty("studentPhone") String studentPhone, @JsonProperty("subject") String subject,
            @JsonProperty("date") String date, @JsonProperty("time") String time,
            @JsonProperty("duration") Integer duration, @JsonProperty("venue") String venue,
            @JsonProperty("status") String status, @JsonProperty("notes") String notes,
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
        id = source.getId().toString();
        studentId = source.getStudentId().map(UUID::toString).orElse(null);
        academicLevel = source.getAcademicLevel().map(level -> level.value).orElse(null);
        preparation = source.getPreparation().orElse(null);
        seriesId = source.getSeriesId().map(UUID::toString).orElse(null);
        studentName = source.getStudentName().fullName;
        studentPhone = source.getStudentPhone().value;
        subject = source.getSubject().value;
        date = source.getDate().toStorageString();
        time = source.getTime().toString();
        duration = source.getDuration().value;
        venue = source.getVenue().map(value -> value.value).orElse(null);
        status = source.getStatus().toString();
        notes = source.getNotes().orElse(null);
        cancelReason = source.getCancelReason().orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted lesson object into the model's {@code Lesson} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted lesson.
     */
    public Lesson toModelType() throws IllegalValueException {
        final Name modelStudentName = convert("studentName", studentName, Name::isValidStoredName,
                Name.MESSAGE_CONSTRAINTS, Name::fromStored);
        final Phone modelStudentPhone = convert("studentPhone", studentPhone, Phone::isValidStoredPhone,
                Phone.MESSAGE_CONSTRAINTS, Phone::fromStored);
        final Subject modelSubject = convert("subject", subject, Subject::isValidSubject,
                Subject.MESSAGE_CONSTRAINTS, Subject::new);
        final LessonDate modelDate = convert("date", date, LessonDate::isValidDate,
                LessonDate.MESSAGE_CONSTRAINTS, LessonDate::new);
        final LessonTime modelTime = convert("time", time, LessonTime::isValidTime,
                LessonTime.MESSAGE_CONSTRAINTS, LessonTime::new);
        final LessonDuration modelDuration = convert("duration", duration, LessonDuration::isValidDuration,
                LessonDuration.MESSAGE_CONSTRAINTS, LessonDuration::new);
        if (!Lesson.isValidTimeRange(modelTime, modelDuration)) {
            throw new IllegalValueException(Lesson.MESSAGE_CONSTRAINTS);
        }

        final Venue modelVenue = venue == null ? null : convert("venue", venue, Venue::isValidStoredVenue,
                Venue.MESSAGE_CONSTRAINTS, Venue::fromStored);
        final LessonStatus modelStatus = status == null ? LessonStatus.SCHEDULED
                : convert("status", status, LessonStatus::isValidStatus,
                        LessonStatus.MESSAGE_CONSTRAINTS, LessonStatus::fromString);
        if (notes != null && !Lesson.isValidNotes(notes)) {
            throw new IllegalValueException(Lesson.MESSAGE_NOTES_CONSTRAINTS);
        }

        if (cancelReason != null && !Lesson.isValidCancelReason(cancelReason)) {
            throw new IllegalValueException(Lesson.MESSAGE_REASON_CONSTRAINTS);
        }

        final UUID modelSeriesId = parseSeriesId();

        try {
            return new Lesson(id == null ? UUID.randomUUID() : UUID.fromString(id),
                    studentId == null ? null : UUID.fromString(studentId),
                    academicLevel == null ? null : new Level(academicLevel), preparation,
                    modelSeriesId,
                    modelStudentName, modelStudentPhone, modelSubject, modelDate, modelTime, modelDuration,
                    modelVenue, modelStatus, notes, cancelReason);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException("Invalid lesson ID, historical level, or preparation notes.");
        }
    }

    /**
     * Returns the series id of the lesson, or null if it is not part of a series.
     *
     * @throws IllegalValueException if the series id is present but is not a valid UUID, which includes a blank one.
     */
    private UUID parseSeriesId() throws IllegalValueException {
        if (seriesId == null) {
            return null;
        }
        try {
            return UUID.fromString(seriesId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(INVALID_SERIES_ID_MESSAGE);
        }
    }

    /**
     * Returns {@code raw} converted by {@code factory} after checking that it is present and passes
     * {@code isValid}.
     *
     * @throws IllegalValueException if {@code raw} is missing or is not valid.
     */
    private static <R, T> T convert(String fieldName, R raw, Predicate<R> isValid, String constraints,
            Function<R, T> factory) throws IllegalValueException {
        if (raw == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
        if (!isValid.test(raw)) {
            throw new IllegalValueException(constraints);
        }
        return factory.apply(raw);
    }

}
