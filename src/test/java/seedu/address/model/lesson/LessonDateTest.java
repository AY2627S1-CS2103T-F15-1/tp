package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class LessonDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonDate(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, LessonDate.MESSAGE_CONSTRAINTS, () -> new LessonDate(""));
        assertThrows(IllegalArgumentException.class, LessonDate.MESSAGE_CONSTRAINTS, ()
                -> new LessonDate("2026-02-30"));
    }

    @Test
    public void constructor_validDate_parsesDate() {
        assertEquals(LocalDate.of(2026, 9, 22), new LessonDate("2026-09-22").value);
        assertEquals(LocalDate.of(2026, 9, 22), new LessonDate("22/9/2026").value);
        assertEquals(LocalDate.of(2026, 9, 2), new LessonDate("02/09/2026").value);
    }

    @Test
    public void isValidDate() {
        // null date
        assertThrows(NullPointerException.class, () -> LessonDate.isValidDate(null));

        // invalid dates
        assertFalse(LessonDate.isValidDate("")); // empty string
        assertFalse(LessonDate.isValidDate(" ")); // spaces only
        assertFalse(LessonDate.isValidDate("tomorrow")); // not a date
        assertFalse(LessonDate.isValidDate("2026-02-30")); // impossible date
        assertFalse(LessonDate.isValidDate("2027-02-29")); // not a leap year
        assertFalse(LessonDate.isValidDate("2026-13-01")); // impossible month
        assertFalse(LessonDate.isValidDate("30/02/2026")); // impossible date in the other format
        assertFalse(LessonDate.isValidDate("9/10")); // no year
        assertFalse(LessonDate.isValidDate("2026/09/22")); // wrong separator
        assertFalse(LessonDate.isValidDate("22-09-2026")); // wrong order

        // valid dates
        assertTrue(LessonDate.isValidDate("2026-09-22"));
        assertTrue(LessonDate.isValidDate("2028-02-29")); // leap day
        assertTrue(LessonDate.isValidDate("22/09/2026"));
        assertTrue(LessonDate.isValidDate(" 2026-09-22 ")); // surrounding spaces
    }

    @Test
    public void toStorageString_returnsIsoDate() {
        assertEquals("2026-09-02", new LessonDate("2/9/2026").toStorageString());
    }

    @Test
    public void toStringMethod_returnsDisplayDate() {
        assertEquals("Tue 22 Sep 2026", new LessonDate("2026-09-22").toString());
    }

    @Test
    public void equals() {
        LessonDate date = new LessonDate("2026-09-22");

        // same values -> returns true
        assertTrue(date.equals(new LessonDate("2026-09-22")));

        // same date in the other format -> returns true
        assertTrue(date.equals(new LessonDate("22/09/2026")));

        // same object -> returns true
        assertTrue(date.equals(date));

        // null -> returns false
        assertFalse(date.equals(null));

        // different types -> returns false
        assertFalse(date.equals(5.0f));

        // different values -> returns false
        assertFalse(date.equals(new LessonDate("2026-09-23")));
    }

    @Test
    public void hashCode_equalDates_sameHashCode() {
        assertEquals(new LessonDate("2026-09-22").hashCode(), new LessonDate("22/09/2026").hashCode());
    }
}
