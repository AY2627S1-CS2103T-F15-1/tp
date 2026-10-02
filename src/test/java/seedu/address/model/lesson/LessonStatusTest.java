package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LessonStatusTest {

    @Test
    public void isValidStatus() {
        // invalid statuses
        assertFalse(LessonStatus.isValidStatus(null));
        assertFalse(LessonStatus.isValidStatus("")); // empty string
        assertFalse(LessonStatus.isValidStatus("pending")); // unknown status
        assertFalse(LessonStatus.isValidStatus(" done")); // surrounding spaces

        // valid statuses
        assertTrue(LessonStatus.isValidStatus("scheduled"));
        assertTrue(LessonStatus.isValidStatus("completed"));
        assertTrue(LessonStatus.isValidStatus("cancelled"));
        assertTrue(LessonStatus.isValidStatus("missed"));
        assertTrue(LessonStatus.isValidStatus("MISSED")); // upper case
    }

    @Test
    public void fromString_validStatus_returnsStatus() {
        assertEquals(LessonStatus.SCHEDULED, LessonStatus.fromString("scheduled"));
        assertEquals(LessonStatus.COMPLETED, LessonStatus.fromString("Completed"));
        assertEquals(LessonStatus.CANCELLED, LessonStatus.fromString("CANCELLED"));
        assertEquals(LessonStatus.MISSED, LessonStatus.fromString("missed"));
    }

    @Test
    public void fromString_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, LessonStatus.MESSAGE_CONSTRAINTS, ()
                -> LessonStatus.fromString("pending"));
        assertThrows(IllegalArgumentException.class, LessonStatus.MESSAGE_CONSTRAINTS, ()
                -> LessonStatus.fromString(null));
    }

    @Test
    public void toStringMethod_returnsLowerCaseName() {
        assertEquals("scheduled", LessonStatus.SCHEDULED.toString());
        assertEquals("missed", LessonStatus.MISSED.toString());
    }
}
