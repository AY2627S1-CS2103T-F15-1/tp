package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedLesson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;

public class JsonAdaptedLessonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "91a4";
    private static final String INVALID_SUBJECT = "#math";
    private static final String INVALID_DATE = "2026-02-30";
    private static final String INVALID_TIME = "24:00";
    private static final int INVALID_DURATION = 50;
    private static final String INVALID_VENUE = " ";
    private static final String INVALID_STATUS = "pending";

    private static final String VALID_NAME = ALICE_MATH.getStudentName().toString();
    private static final String VALID_PHONE = ALICE_MATH.getStudentPhone().toString();
    private static final String VALID_SUBJECT = ALICE_MATH.getSubject().toString();
    private static final String VALID_DATE = ALICE_MATH.getDate().toStorageString();
    private static final String VALID_TIME = ALICE_MATH.getTime().toString();
    private static final int VALID_DURATION = ALICE_MATH.getDuration().value;
    private static final String VALID_VENUE = ALICE_MATH.getVenue().get().toString();
    private static final String VALID_STATUS = ALICE_MATH.getStatus().toString();

    @Test
    public void toModelType_validLessonDetails_returnsLesson() throws Exception {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(ALICE_MATH);
        assertEquals(ALICE_MATH, lesson.toModelType());
    }

    @Test
    public void toModelType_lessonWithAllFields_returnsLesson() throws Exception {
        Lesson completedLesson = new LessonBuilder(ALICE_MATH).withStatus(LessonStatus.COMPLETED)
                .withNotes("Covered algebra").withCancelReason("None").build();
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(completedLesson);
        assertEquals(completedLesson, lesson.toModelType());
    }

    @Test
    public void toModelType_lessonWithoutOptionalFields_returnsLesson() throws Exception {
        Lesson lessonWithoutOptionalFields = new LessonBuilder().build();
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(lessonWithoutOptionalFields);
        assertEquals(lessonWithoutOptionalFields, lesson.toModelType());
    }

    @Test
    public void toModelType_nullOptionalFields_returnsLessonWithoutOptionalFields() throws Exception {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, null, VALID_STATUS, null, null);
        Lesson modelLesson = lesson.toModelType();
        assertTrue(modelLesson.getVenue().isEmpty());
        assertTrue(modelLesson.getNotes().isEmpty());
        assertTrue(modelLesson.getCancelReason().isEmpty());
    }

    @Test
    public void toModelType_nullStatus_returnsScheduledLesson() throws Exception {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, null, null, null);
        assertEquals(LessonStatus.SCHEDULED, lesson.toModelType().getStatus());
    }

    @Test
    public void toModelType_statusInUpperCase_returnsLesson() throws Exception {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, "MISSED", null, null);
        assertEquals(LessonStatus.MISSED, lesson.toModelType().getStatus());
    }

    @Test
    public void toModelType_invalidStudentName_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(INVALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullStudentName_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(null, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentName");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidStudentPhone_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, INVALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, Phone.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullStudentPhone_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, null, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "studentPhone");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, INVALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullSubject_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, null, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "subject");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, INVALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, LessonDate.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullDate_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, null,
                VALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "date");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidTime_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                INVALID_TIME, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, LessonTime.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullTime_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                null, VALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "time");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidDuration_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, INVALID_DURATION, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, LessonDuration.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullDuration_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, null, VALID_VENUE, VALID_STATUS, null, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "duration");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }

    @Test
    public void toModelType_lessonRunsPastMidnight_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                "23:00", 120, VALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, Lesson.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidVenue_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, INVALID_VENUE, VALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, Venue.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_NAME, VALID_PHONE, VALID_SUBJECT, VALID_DATE,
                VALID_TIME, VALID_DURATION, VALID_VENUE, INVALID_STATUS, null, null);
        assertThrows(IllegalValueException.class, LessonStatus.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

}
