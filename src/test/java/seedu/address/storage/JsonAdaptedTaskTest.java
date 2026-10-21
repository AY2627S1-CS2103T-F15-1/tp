package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedTask.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.task.Task;
import seedu.address.testutil.TaskBuilder;

public class JsonAdaptedTaskTest {

    private static final String NAME = ALICE_MATH.getStudentName().fullName;
    private static final String PHONE = ALICE_MATH.getStudentPhone().value;

    private static JsonAdaptedTask task(String id, String description, String due, Boolean done, String name,
            String phone) {
        return new JsonAdaptedTask(id, description, due, done, name, phone, null, null, null, null);
    }

    @Test
    public void toModelType_studentTask_roundTrip() throws Exception {
        Task task = new TaskBuilder().withDue(LocalDate.of(2026, 12, 1)).done().build();
        assertEquals(task, new JsonAdaptedTask(task).toModelType());
    }

    @Test
    public void toModelType_lessonTask_roundTrip() throws Exception {
        Task task = new TaskBuilder().withLesson(ALICE_MATH).build();
        assertEquals(task, new JsonAdaptedTask(task).toModelType());
    }

    @Test
    public void toModelType_missingDoneAndDue_meansOpenWithoutDueDate() throws Exception {
        Task task = task("id", "Call", null, null, NAME, PHONE).toModelType();

        assertEquals(false, task.isDone());
        assertTrue(task.getDue().isEmpty());
    }

    @Test
    public void toModelType_missingRequiredFields_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"),
                task(null, "Call", null, false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"),
                task(" ", "Call", null, false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"),
                task("id", null, null, false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentName"),
                task("id", "Call", null, false, null, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentPhone"),
                task("id", "Call", null, false, NAME, null)::toModelType);
    }

    @Test
    public void toModelType_invalidValues_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS,
                task("id", " ", null, false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS,
                task("id", "a".repeat(201), null, false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INVALID_DUE,
                task("id", "Call", "01/12/2026", false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INVALID_DUE,
                task("id", "Call", "2026-02-30", false, NAME, PHONE)::toModelType);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS,
                task("id", "Call", null, false, "R*chel", PHONE)::toModelType);
        assertThrows(IllegalValueException.class, Phone.MESSAGE_CONSTRAINTS,
                task("id", "Call", null, false, NAME, "91a4")::toModelType);
    }

    @Test
    public void toModelType_invalidOrIncompleteLessonLink_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INCOMPLETE_LESSON_LINK,
                new JsonAdaptedTask("id", "Call", null, false, NAME, PHONE, "Math", null, null, null)::toModelType);
        assertThrows(IllegalValueException.class, JsonAdaptedTask.MESSAGE_INCOMPLETE_LESSON_LINK,
                new JsonAdaptedTask("id", "Call", null, false, NAME, PHONE, null, "2026-12-01", "16:30",
                        90)::toModelType);
        assertThrows(IllegalValueException.class, LessonDate.MESSAGE_CONSTRAINTS,
                new JsonAdaptedTask("id", "Call", null, false, NAME, PHONE, "Math", "2026-02-30", "16:30",
                        90)::toModelType);
    }
}
