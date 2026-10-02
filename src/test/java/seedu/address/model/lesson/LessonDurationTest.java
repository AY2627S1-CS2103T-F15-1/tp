package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LessonDurationTest {

    @Test
    public void constructor_invalidDuration_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, LessonDuration.MESSAGE_CONSTRAINTS, () -> new LessonDuration(0));
        assertThrows(IllegalArgumentException.class, LessonDuration.MESSAGE_CONSTRAINTS, () -> new LessonDuration(50));
    }

    @Test
    public void constructor_validDuration_storesMinutes() {
        assertEquals(90, new LessonDuration(90).value);
    }

    @Test
    public void isValidDuration() {
        // invalid durations
        assertFalse(LessonDuration.isValidDuration(Integer.MIN_VALUE));
        assertFalse(LessonDuration.isValidDuration(-15)); // negative
        assertFalse(LessonDuration.isValidDuration(0));
        assertFalse(LessonDuration.isValidDuration(14)); // below the minimum
        assertFalse(LessonDuration.isValidDuration(50)); // not a multiple of 15
        assertFalse(LessonDuration.isValidDuration(495)); // above the maximum
        assertFalse(LessonDuration.isValidDuration(Integer.MAX_VALUE));

        // valid durations
        assertTrue(LessonDuration.isValidDuration(LessonDuration.MIN_DURATION));
        assertTrue(LessonDuration.isValidDuration(60));
        assertTrue(LessonDuration.isValidDuration(90));
        assertTrue(LessonDuration.isValidDuration(LessonDuration.MAX_DURATION));
    }

    @Test
    public void toStringMethod() {
        assertEquals("45m", new LessonDuration(45).toString()); // minutes only
        assertEquals("2h", new LessonDuration(120).toString()); // whole hours
        assertEquals("1h 30m", new LessonDuration(90).toString()); // hours and minutes
    }

    @Test
    public void equals() {
        LessonDuration duration = new LessonDuration(90);

        // same values -> returns true
        assertTrue(duration.equals(new LessonDuration(90)));

        // same object -> returns true
        assertTrue(duration.equals(duration));

        // null -> returns false
        assertFalse(duration.equals(null));

        // different types -> returns false
        assertFalse(duration.equals(5.0f));

        // different values -> returns false
        assertFalse(duration.equals(new LessonDuration(60)));
    }

    @Test
    public void hashCode_equalDurations_sameHashCode() {
        assertEquals(new LessonDuration(90).hashCode(), new LessonDuration(90).hashCode());
    }
}
