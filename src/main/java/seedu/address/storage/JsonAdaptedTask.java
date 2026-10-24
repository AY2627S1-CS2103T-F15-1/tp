package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.task.Task;

/**
 * Jackson-friendly version of {@link Task}. A task is stored with the permanent ids of its student and, if it has
 * one, of its lesson or homework item.
 */
class JsonAdaptedTask {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Task's %s field is missing!";
    public static final String MESSAGE_INVALID_DUE = "Task's due field must be a date in YYYY-MM-DD format.";
    public static final String MESSAGE_INVALID_ID_FORMAT = "Task's %s field must be a valid identifier.";
    public static final String MESSAGE_TWO_LINKS = "A task can be about a lesson or about homework, not both.";

    private final String id;
    private final String description;
    private final String due;
    private final Boolean done;
    private final String studentId;
    private final String lessonId;
    private final String homeworkId;

    /**
     * Constructs a {@code JsonAdaptedTask} with the given task details.
     * The {@code due} date, {@code lessonId} and {@code homeworkId} are optional, and may be null. A missing
     * {@code done} means that the task is open.
     */
    @JsonCreator
    public JsonAdaptedTask(@JsonProperty("id") String id, @JsonProperty("description") String description,
            @JsonProperty("due") String due, @JsonProperty("done") Boolean done,
            @JsonProperty("studentId") String studentId, @JsonProperty("lessonId") String lessonId,
            @JsonProperty("homeworkId") String homeworkId) {
        this.id = id;
        this.description = description;
        this.due = due;
        this.done = done;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.homeworkId = homeworkId;
    }

    /**
     * Converts a given {@code Task} into this class for Jackson use.
     */
    public JsonAdaptedTask(Task source) {
        id = source.getId().toString();
        description = source.getDescription();
        due = source.getDue().map(LocalDate::toString).orElse(null);
        done = source.isDone();
        studentId = source.getStudentId().toString();
        lessonId = source.getLessonId().map(UUID::toString).orElse(null);
        homeworkId = source.getHomeworkId().map(UUID::toString).orElse(null);
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
        UUID modelId = parseId("id", id);
        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"));
        }
        if (!Task.isValidDescription(description)) {
            throw new IllegalValueException(Task.MESSAGE_DESCRIPTION_CONSTRAINTS);
        }
        if (studentId == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentId"));
        }
        UUID modelStudentId = parseId("studentId", studentId);
        UUID modelLessonId = lessonId == null ? null : parseId("lessonId", lessonId);
        UUID modelHomeworkId = homeworkId == null ? null : parseId("homeworkId", homeworkId);
        if (modelLessonId != null && modelHomeworkId != null) {
            throw new IllegalValueException(MESSAGE_TWO_LINKS);
        }

        LocalDate modelDue = null;
        if (due != null) {
            try {
                modelDue = LocalDate.parse(due);
            } catch (DateTimeParseException e) {
                throw new IllegalValueException(MESSAGE_INVALID_DUE);
            }
        }
        return new Task(modelId, description, modelDue, done != null && done, modelStudentId, modelLessonId,
                modelHomeworkId);
    }

    private static UUID parseId(String fieldName, String raw) throws IllegalValueException {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(String.format(MESSAGE_INVALID_ID_FORMAT, fieldName));
        }
    }

}
