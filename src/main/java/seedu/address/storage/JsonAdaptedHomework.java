package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** JSON adapter retaining every homework field, including historical feedback. */
class JsonAdaptedHomework {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Homework's %s field is missing!";
    public static final String INVALID_FIELD_MESSAGE_FORMAT = "Homework's %s field is invalid: %s";

    private final String id;
    private final String studentId;
    private final String description;
    private final String dueDate;
    private final String status;
    private final String feedback;

    /** Creates an adapter from persisted fields. */
    @JsonCreator
    public JsonAdaptedHomework(@JsonProperty("id") String id, @JsonProperty("studentId") String studentId,
            @JsonProperty("description") String description, @JsonProperty("dueDate") String dueDate,
            @JsonProperty("status") String status, @JsonProperty("feedback") String feedback) {
        this.id = id;
        this.studentId = studentId;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.feedback = feedback;
    }

    /** Copies the assignment's complete persisted state. */
    public JsonAdaptedHomework(Homework homework) {
        this(homework.getId().toString(), homework.getStudentId().toString(), homework.getDescription(),
                homework.getDueDate().toString(), homework.getStatus().name(), homework.getFeedback().orElse(null));
    }

    /**
     * Restores an assignment, rejecting corrupt or missing required values with a message that names the field.
     * The status is accepted in any letter case, because a data file may be edited by hand.
     *
     * @throws IllegalValueException if a field is missing or invalid.
     */
    public Homework toModelType() throws IllegalValueException {
        UUID modelId = parseIdentifier(id, "id");
        UUID modelStudentId = parseIdentifier(studentId, "studentId");
        requirePresent(description, "description");
        requirePresent(dueDate, "dueDate");
        requirePresent(status, "status");
        LocalDate modelDueDate;
        try {
            modelDueDate = LocalDate.parse(dueDate);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "dueDate",
                    "a date is written as YYYY-MM-DD"));
        }
        HomeworkStatus modelStatus = HomeworkStatus.fromText(status)
                .orElseThrow(() -> new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, "status",
                        HomeworkStatus.MESSAGE_CONSTRAINTS)));
        try {
            return new Homework(modelId, modelStudentId, description, modelDueDate, modelStatus, feedback);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private static void requirePresent(String value, String fieldName) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
    }

    private static UUID parseIdentifier(String value, String fieldName) throws IllegalValueException {
        requirePresent(value, fieldName);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(String.format(INVALID_FIELD_MESSAGE_FORMAT, fieldName,
                    "an identifier has 36 characters, as in 123e4567-e89b-12d3-a456-426614174000"));
        }
    }
}
