package seedu.address.storage;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** JSON adapter retaining every homework field, including historical feedback. */
class JsonAdaptedHomework {
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

    /** Restores an assignment, rejecting corrupt or missing required values. */
    public Homework toModelType() throws IllegalValueException {
        try {
            return new Homework(UUID.fromString(id), UUID.fromString(studentId), description,
                    LocalDate.parse(dueDate), HomeworkStatus.valueOf(status), feedback);
        } catch (RuntimeException e) {
            throw new IllegalValueException("Invalid homework: " + e.getMessage());
        }
    }
}
