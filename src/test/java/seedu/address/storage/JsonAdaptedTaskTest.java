package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedTask.MESSAGE_INVALID_ID_FORMAT;
import static seedu.address.storage.JsonAdaptedTask.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.homework.Homework;
import seedu.address.model.task.Task;
import seedu.address.testutil.TaskBuilder;

public class JsonAdaptedTaskTest {

    private static final String STUDENT = ALICE.getId().toString();

    private static JsonAdaptedTask task(String id, String description, String due, Boolean done, String studentId) {
        return new JsonAdaptedTask(id, description, due, done, studentId, null, null);
    }

    @Test
    public void toModelType_studentTask_roundTrip() throws Exception {
        Task task = new TaskBuilder().withDue(LocalDate.of(2026, 12, 1)).done().build();
        assertEquals(task, new JsonAdaptedTask(task).toModelType());
    }

    @Test
    public void toModelType_lessonTask_roundTripKeepsTheLessonId() throws Exception {
        Task task = new TaskBuilder().withLesson(ALICE_MATH).build();
        Task restored = new JsonAdaptedTask(task).toModelType();

        assertEquals(task, restored);
        assertEquals(ALICE_MATH.getId(), restored.getLessonId().get());
        assertEquals(ALICE.getId(), restored.getStudentId());
    }

    @Test
    public void toModelType_homeworkTask_roundTripKeepsTheHomeworkId() throws Exception {
        Homework homework = Homework.assign(ALICE.getId(), "Practice set", LocalDate.of(2030, 1, 1));
        Task task = new TaskBuilder().withHomework(homework).build();
        Task restored = new JsonAdaptedTask(task).toModelType();

        assertEquals(task, restored);
        assertEquals(homework.getId(), restored.getHomeworkId().get());
        assertTrue(restored.getLessonId().isEmpty());
    }

    @Test
    public void toModelType_missingDoneAndDue_meansOpenWithoutDueDate() throws Exception {
        Task task = task("id", "Call", null, null, STUDENT).toModelType();

        assertEquals(false, task.isDone());
        assertTrue(task.getDue().isEmpty());
    }

    @Test
    public void toModelType_missingRequiredFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"),
                task(null, "Call", null, false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"),
                task(" ", "Call", null, false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"),
                task("id", null, null, false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentId"),
                task("id", "Call", null, false, null)::toModelType);
    }

    @Test
    public void toModelType_invalidValues_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS,
                task("id", " ", null, false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS,
                task("id", "a".repeat(201), null, false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INVALID_DUE,
                task("id", "Call", "01/12/2026", false, STUDENT)::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INVALID_DUE,
                task("id", "Call", "2026-02-30", false, STUDENT)::toModelType);
    }

    @Test
    public void toModelType_malformedIds_throwsIllegalValueException() {
        UUID valid = UUID.randomUUID();
        assertThrows(IllegalValueException.class, String.format(MESSAGE_INVALID_ID_FORMAT, "studentId"),
                task("id", "Call", null, false, "Alice Pauline")::toModelType);
        assertThrows(IllegalValueException.class, String.format(MESSAGE_INVALID_ID_FORMAT, "lessonId"),
                new JsonAdaptedTask("id", "Call", null, false, STUDENT, "not-a-uuid", null)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MESSAGE_INVALID_ID_FORMAT, "homeworkId"),
                new JsonAdaptedTask("id", "Call", null, false, STUDENT, null, "")::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_TWO_LINKS,
                new JsonAdaptedTask("id", "Call", null, false, STUDENT, valid.toString(),
                        valid.toString())::toModelType);
    }
}
