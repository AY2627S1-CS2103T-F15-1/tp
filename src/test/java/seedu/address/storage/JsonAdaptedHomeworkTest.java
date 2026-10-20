package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.lesson.Lesson;

public class JsonAdaptedHomeworkTest {

    private static final String ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String STUDENT = "223e4567-e89b-12d3-a456-426614174000";

    private static JsonAdaptedHomework item(String id, String student, String description, String dueDate,
            String status, String feedback) {
        return new JsonAdaptedHomework(id, student, description, dueDate, status, feedback);
    }

    private static String messageOf(JsonAdaptedHomework item) {
        return assertThrows(IllegalValueException.class, item::toModelType).getMessage();
    }

    @Test
    public void toModelType_validFields_restoresEveryField() throws Exception {
        Homework restored = item(ID, STUDENT, "Algebra", "2026-10-20", "SUBMITTED", "Good").toModelType();

        assertEquals(UUID.fromString(ID), restored.getId());
        assertEquals(UUID.fromString(STUDENT), restored.getStudentId());
        assertEquals("Algebra", restored.getDescription());
        assertEquals(LocalDate.of(2026, 10, 20), restored.getDueDate());
        assertEquals(HomeworkStatus.SUBMITTED, restored.getStatus());
        assertEquals("Good", restored.getFeedback().orElseThrow());
    }

    @Test
    public void toModelType_statusInAnyLetterCase_isAccepted() throws Exception {
        for (String status : new String[] {"assigned", "Assigned", "ASSIGNED", " assigned "}) {
            assertEquals(HomeworkStatus.ASSIGNED,
                    item(ID, STUDENT, "Algebra", "2026-10-20", status, null).toModelType().getStatus());
        }
    }

    @Test
    public void toModelType_oldDueDateAndFeedbackWhileAssigned_areRestored() throws Exception {
        Homework restored = item(ID, STUDENT, "Algebra", "2001-01-01", "ASSIGNED", "Kept").toModelType();

        assertEquals(LocalDate.of(2001, 1, 1), restored.getDueDate());
        assertEquals("Kept", restored.getFeedback().orElseThrow());
    }

    @Test
    public void toModelType_missingField_namesTheField() {
        String format = JsonAdaptedHomework.MISSING_FIELD_MESSAGE_FORMAT;

        assertEquals(String.format(format, "id"), messageOf(item(null, STUDENT, "A", "2026-10-20", "ASSIGNED", null)));
        assertEquals(String.format(format, "studentId"),
                messageOf(item(ID, null, "A", "2026-10-20", "ASSIGNED", null)));
        assertEquals(String.format(format, "description"),
                messageOf(item(ID, STUDENT, null, "2026-10-20", "ASSIGNED", null)));
        assertEquals(String.format(format, "dueDate"), messageOf(item(ID, STUDENT, "A", null, "ASSIGNED", null)));
        assertEquals(String.format(format, "status"), messageOf(item(ID, STUDENT, "A", "2026-10-20", null, null)));
    }

    @Test
    public void toModelType_invalidField_namesTheField() {
        String format = JsonAdaptedHomework.INVALID_FIELD_MESSAGE_FORMAT;

        assertTrue(messageOf(item("bad", STUDENT, "A", "2026-10-20", "ASSIGNED", null))
                .startsWith(String.format(format, "id", "")));
        assertTrue(messageOf(item(ID, "bad", "A", "2026-10-20", "ASSIGNED", null))
                .startsWith(String.format(format, "studentId", "")));
        assertTrue(messageOf(item(ID, STUDENT, "A", "2026-02-30", "ASSIGNED", null))
                .startsWith(String.format(format, "dueDate", "")));
        assertTrue(messageOf(item(ID, STUDENT, "A", "20/10/2026", "ASSIGNED", null))
                .startsWith(String.format(format, "dueDate", "")));
        assertEquals(String.format(format, "status", HomeworkStatus.MESSAGE_CONSTRAINTS),
                messageOf(item(ID, STUDENT, "A", "2026-10-20", "OVERDUE", null)));
        assertEquals(String.format(format, "status", HomeworkStatus.MESSAGE_CONSTRAINTS),
                messageOf(item(ID, STUDENT, "A", "2026-10-20", "", null)));
    }

    @Test
    public void toModelType_invalidDescriptionOrFeedback_usesTheMessageOfTheRule() {
        assertEquals(Homework.DESCRIPTION_CONSTRAINTS,
                messageOf(item(ID, STUDENT, "", "2026-10-20", "ASSIGNED", null)));
        assertEquals(Homework.DESCRIPTION_CONSTRAINTS,
                messageOf(item(ID, STUDENT, "x".repeat(201), "2026-10-20", "ASSIGNED", null)));
        assertEquals(Lesson.MESSAGE_NOTES_CONSTRAINTS,
                messageOf(item(ID, STUDENT, "A", "2026-10-20", "SUBMITTED", "two\nlines")));
        assertEquals(Lesson.MESSAGE_NOTES_CONSTRAINTS,
                messageOf(item(ID, STUDENT, "A", "2026-10-20", "SUBMITTED", "")));
    }

    @Test
    public void serialize_homework_writesTheStatusNameAndEveryKey() throws Exception {
        Homework homework = new Homework(UUID.fromString(ID), UUID.fromString(STUDENT), "Algebra",
                LocalDate.of(2026, 10, 20), HomeworkStatus.REVIEWED, "Good");

        String json = JsonUtil.toJsonString(new JsonAdaptedHomework(homework));

        assertTrue(json.contains("\"status\" : \"REVIEWED\""), json);
        assertTrue(json.contains("\"dueDate\" : \"2026-10-20\""), json);
        assertTrue(json.contains("\"feedback\" : \"Good\""), json);
        assertTrue(json.contains("\"studentId\" : \"" + STUDENT + "\""), json);
    }
}
