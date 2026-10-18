package seedu.address.storage;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.task.FollowUpTask;

/** JSON representation retaining task identity, stable link, due date, and completion state. */
class JsonAdaptedTask {
    private final String id;
    private final String studentId;
    private final String linkType;
    private final String targetId;
    private final String description;
    private final String dueDate;
    private final Boolean done;

    /** Binds stored fields without concealing corrupt or missing state. */
    @JsonCreator
    public JsonAdaptedTask(@JsonProperty("id") String id, @JsonProperty("studentId") String studentId,
            @JsonProperty("linkType") String linkType, @JsonProperty("targetId") String targetId,
            @JsonProperty("description") String description, @JsonProperty("dueDate") String dueDate,
            @JsonProperty("done") Boolean done) {
        this.id = id;
        this.studentId = studentId;
        this.linkType = linkType;
        this.targetId = targetId;
        this.description = description;
        this.dueDate = dueDate;
        this.done = done;
    }

    /** Copies every persisted field. */
    public JsonAdaptedTask(FollowUpTask task) {
        this(task.id().toString(), task.studentId().toString(), task.linkType().name(), task.targetId().toString(),
                task.description(), task.dueDate() == null ? null : task.dueDate().toString(), task.done());
    }

    /** Restores a complete task; target integrity is validated after all parent domains load. */
    public FollowUpTask toModelType() throws IllegalValueException {
        try {
            return new FollowUpTask(UUID.fromString(id), UUID.fromString(studentId),
                    FollowUpTask.LinkType.valueOf(linkType), UUID.fromString(targetId), description,
                    dueDate == null ? null : LocalDate.parse(dueDate), done);
        } catch (RuntimeException e) {
            throw new IllegalValueException("Invalid task: " + e.getMessage());
        }
    }
}
