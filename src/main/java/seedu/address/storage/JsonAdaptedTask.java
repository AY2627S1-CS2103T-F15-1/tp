package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.task.Task;

/**
 * Jackson-friendly version of {@link Task}. A task is stored with the permanent ids of its student and, if it has
 * one, of its lesson, homework item or payment.
 */
class JsonAdaptedTask {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Task's %s field is missing!";
    public static final String MESSAGE_INVALID_DUE = "Task's due field must be a date in YYYY-MM-DD format.";
    public static final String MESSAGE_INVALID_ID_FORMAT = "Task's %s field must be a valid identifier.";
    public static final String MESSAGE_TWO_LINKS =
            "A task can be about only one lesson, homework item or payment.";

    private final String id;
    private final String description;
    private final String due;
    private final Boolean done;
    private final String studentId;
    private final String lessonId;
    private final String homeworkId;
    private final String paymentId;

    /**
     * Constructs a {@code JsonAdaptedTask} with the given task details.
     * The {@code due} date, {@code lessonId}, {@code homeworkId} and {@code paymentId} are optional, and may be
     * null. A missing {@code done} means that the task is open.
     */
    @JsonCreator
    public JsonAdaptedTask(@JsonProperty("id") String id, @JsonProperty("description") String description,
            @JsonProperty("due") String due, @JsonProperty("done") Boolean done,
            @JsonProperty("studentId") String studentId, @JsonProperty("lessonId") String lessonId,
            @JsonProperty("homeworkId") String homeworkId, @JsonProperty("paymentId") String paymentId) {
        this.id = id;
        this.description = description;
        this.due = due;
        this.done = done;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.homeworkId = homeworkId;
        this.paymentId = paymentId;
    }

    /** Constructs an adapted task without a payment link, for older callers and tests. */
    public JsonAdaptedTask(String id, String description, String due, Boolean done, String studentId,
            String lessonId, String homeworkId) {
        this(id, description, due, done, studentId, lessonId, homeworkId, null);
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
        paymentId = source.getPaymentId().map(UUID::toString).orElse(null);
    }

    /** Returns whether the stored JSON contains a payment link at all. */
    public boolean hasPaymentLink() {
        return paymentId != null;
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
        UUID modelPaymentId = paymentId == null ? null : parseId("paymentId", paymentId);
        long links = Stream.of(modelLessonId, modelHomeworkId, modelPaymentId)
                .filter(Objects::nonNull).count();
        if (links > 1) {
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
                modelHomeworkId, modelPaymentId);
    }

    private static UUID parseId(String fieldName, String raw) throws IllegalValueException {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(String.format(MESSAGE_INVALID_ID_FORMAT, fieldName));
        }
    }

}
