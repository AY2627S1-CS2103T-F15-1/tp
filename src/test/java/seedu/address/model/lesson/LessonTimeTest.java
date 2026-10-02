package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class LessonTimeTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonTime(null));
    }

    @Test
    public void constructor_invalidTime_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, LessonTime.MESSAGE_CONSTRAINTS, () -> new LessonTime(""));
        assertThrows(IllegalArgumentException.class, LessonTime.MESSAGE_CONSTRAINTS, () -> new LessonTime("24:00"));
    }

    @Test
    public void constructor_validTime_parsesTime() {
        assertEquals(LocalTime.of(16, 30), new LessonTime("16:30").value);
        assertEquals(LocalTime.of(9, 5), new LessonTime("9:05").value);
        assertEquals(LocalTime.of(16, 30), new LessonTime("1630").value);
        assertEquals(LocalTime.of(16, 30), new LessonTime("4:30pm").value);
        assertEquals(LocalTime.of(16, 30), new LessonTime("4:30 PM".replace(" ", "")).value);
        assertEquals(LocalTime.of(0, 15), new LessonTime("12:15am").value);
        assertEquals(LocalTime.of(12, 0), new LessonTime("12:00pm").value);
    }

    @Test
    public void isValidTime() {
        // null time
        assertThrows(NullPointerException.class, () -> LessonTime.isValidTime(null));

        // invalid times
        assertFalse(LessonTime.isValidTime("")); // empty string
        assertFalse(LessonTime.isValidTime(" ")); // spaces only
        assertFalse(LessonTime.isValidTime("noon")); // not a time
        assertFalse(LessonTime.isValidTime("24:00")); // hour out of range
        assertFalse(LessonTime.isValidTime("12:60")); // minute out of range
        assertFalse(LessonTime.isValidTime("16.30")); // wrong separator
        assertFalse(LessonTime.isValidTime("2400")); // hour out of range in the other format
        assertFalse(LessonTime.isValidTime("13:00pm")); // 12-hour format with an hour above 12
        assertFalse(LessonTime.isValidTime("0:30am")); // 12-hour format has no hour 0

        // valid times
        assertTrue(LessonTime.isValidTime("00:00"));
        assertTrue(LessonTime.isValidTime("23:59"));
        assertTrue(LessonTime.isValidTime("0000"));
        assertTrue(LessonTime.isValidTime("2359"));
        assertTrue(LessonTime.isValidTime(" 16:30 ")); // surrounding spaces
        assertTrue(LessonTime.isValidTime("4:30PM")); // upper case
    }

    @Test
    public void toStringMethod_returns24HourTime() {
        assertEquals("09:05", new LessonTime("9:05").toString());
        assertEquals("16:30", new LessonTime("4:30pm").toString());
    }

    @Test
    public void equals() {
        LessonTime time = new LessonTime("16:30");

        // same values -> returns true
        assertTrue(time.equals(new LessonTime("16:30")));

        // same time in another format -> returns true
        assertTrue(time.equals(new LessonTime("4:30pm")));

        // same object -> returns true
        assertTrue(time.equals(time));

        // null -> returns false
        assertFalse(time.equals(null));

        // different types -> returns false
        assertFalse(time.equals(5.0f));

        // different values -> returns false
        assertFalse(time.equals(new LessonTime("16:31")));
    }

    @Test
    public void hashCode_equalTimes_sameHashCode() {
        assertEquals(new LessonTime("16:30").hashCode(), new LessonTime("1630").hashCode());
    }
}
