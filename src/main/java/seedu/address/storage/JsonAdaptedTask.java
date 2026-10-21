package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.task.Task;
import seedu.address.model.task.TaskLessonLink;

/**
 * Jackson-friendly version of {@link Task}.
 */
class JsonAdaptedTask {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Task's %s field is missing!";
    public static final String MESSAGE_INVALID_DUE = "Task's due field must be a date in YYYY-MM-DD format.";
    public static final String MESSAGE_INCOMPLETE_LESSON_LINK = "A task about a lesson needs the subject, date, "
            + "time and duration of the lesson.";

    private final String id;
    private final String description;
    private final String due;
    private final Boolean done;
    private final String studentName;
    private final String studentPhone;
    private final String lessonSubject;
    private final String lessonDate;
    private final String lessonTime;
    private final Integer lessonDuration;

    /**
     * Constructs a {@code JsonAdaptedTask} with the given task details.
     * The {@code due} date and the four lesson fields are optional, and may be null. A missing {@code done}
     * means that the task is open.
     */
    @JsonCreator
    public JsonAdaptedTask(@JsonProperty("id") String id, @JsonProperty("description") String description,
            @JsonProperty("due") String due, @JsonProperty("done") Boolean done,
            @JsonProperty("studentName") String studentName, @JsonProperty("studentPhone") String studentPhone,
            @JsonProperty("lessonSubject") String lessonSubject, @JsonProperty("lessonDate") String lessonDate,
            @JsonProperty("lessonTime") String lessonTime, @JsonProperty("lessonDuration") Integer lessonDuration) {
        this.id = id;
        this.description = description;
        this.due = due;
        this.done = done;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.lessonSubject = lessonSubject;
        this.lessonDate = lessonDate;
        this.lessonTime = lessonTime;
        this.lessonDuration = lessonDuration;
    }

    /**
     * Converts a given {@code Task} into this class for Jackson use.
     */
    public JsonAdaptedTask(Task source) {
        id = source.getId();
        description = source.getDescription();
        due = source.getDue().map(LocalDate::toString).orElse(null);
        done = source.isDone();
        studentName = source.getStudentName().fullName;
        studentPhone = source.getStudentPhone().value;
        lessonSubject = source.getLessonLink().map(link -> link.getSubject().value).orElse(null);
        lessonDate = source.getLessonLink().map(link -> link.getDate().toStorageString()).orElse(null);
        lessonTime = source.getLessonLink().map(link -> link.getTime().toString()).orElse(null);
        lessonDuration = source.getLessonLink().map(link -> link.getDuration().value).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted task object into the model's {@code Task} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted task.
     */
    public Task toModelType() throws IllegalValueException {
        if (id == null || id.isBlank()) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"));
        }
        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"));
        }
        if (!Task.isValidDescription(description)) {
            throw new IllegalValueException(Task.MESSAGE_DESCRIPTION_CONSTRAINTS);
        }
        final Name modelStudentName = convert("studentName", studentName, Name::isValidStoredName,
                Name.MESSAGE_CONSTRAINTS, Name::fromStored);
        final Phone modelStudentPhone = convert("studentPhone", studentPhone, Phone::isValidStoredPhone,
                Phone.MESSAGE_CONSTRAINTS, Phone::fromStored);

        LocalDate modelDue = null;
        if (due != null) {
            try {
                modelDue = LocalDate.parse(due);
            } catch (DateTimeParseException e) {
                throw new IllegalValueException(MESSAGE_INVALID_DUE);
            }
        }
        return new Task(id, description, modelDue, done != null && done, modelStudentName, modelStudentPhone,
                toLessonLink());
    }

    private TaskLessonLink toLessonLink() throws IllegalValueException {
        boolean hasAnyLessonField = lessonSubject != null || lessonDate != null || lessonTime != null
                || lessonDuration != null;
        if (!hasAnyLessonField) {
            return null;
        }
        if (lessonSubject == null || lessonDate == null || lessonTime == null || lessonDuration == null) {
            throw new IllegalValueException(MESSAGE_INCOMPLETE_LESSON_LINK);
        }
        return new TaskLessonLink(
                convert("lessonSubject", lessonSubject, Subject::isValidSubject, Subject.MESSAGE_CONSTRAINTS,
                        Subject::new),
                convert("lessonDate", lessonDate, LessonDate::isValidDate, LessonDate.MESSAGE_CONSTRAINTS,
                        LessonDate::new),
                convert("lessonTime", lessonTime, LessonTime::isValidTime, LessonTime.MESSAGE_CONSTRAINTS,
                        LessonTime::new),
                convert("lessonDuration", lessonDuration, LessonDuration::isValidDuration,
                        LessonDuration.MESSAGE_CONSTRAINTS, LessonDuration::new));
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
