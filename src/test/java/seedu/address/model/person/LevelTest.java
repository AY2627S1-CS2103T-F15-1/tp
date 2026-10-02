package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LevelTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Level(null));
    }

    @Test
    public void constructor_invalidLevel_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Level(""));
        assertThrows(IllegalArgumentException.class, () -> new Level("Sec 3"));
    }

    @Test
    public void constructor_lowercaseLevel_storedInUppercase() {
        assertEquals("S3", new Level("s3").value);
    }

    @Test
    public void isValidLevel() {
        // null level
        assertThrows(NullPointerException.class, () -> Level.isValidLevel(null));

        // invalid levels
        assertFalse(Level.isValidLevel("")); // empty string
        assertFalse(Level.isValidLevel(" ")); // spaces only
        assertFalse(Level.isValidLevel("P7")); // primary only goes up to 6
        assertFalse(Level.isValidLevel("S6")); // secondary only goes up to 5
        assertFalse(Level.isValidLevel("J3")); // junior college only goes up to 2
        assertFalse(Level.isValidLevel("P0")); // levels start from 1
        assertFalse(Level.isValidLevel("X1")); // unknown stage
        assertFalse(Level.isValidLevel("S")); // missing year
        assertFalse(Level.isValidLevel("S33")); // extra digit
        assertFalse(Level.isValidLevel("Sec 3")); // free text
        assertFalse(Level.isValidLevel(" S3")); // leading space

        // valid levels
        assertTrue(Level.isValidLevel("P1"));
        assertTrue(Level.isValidLevel("P6"));
        assertTrue(Level.isValidLevel("S1"));
        assertTrue(Level.isValidLevel("S5"));
        assertTrue(Level.isValidLevel("J1"));
        assertTrue(Level.isValidLevel("J2"));
        assertTrue(Level.isValidLevel("s3")); // lowercase
    }

    @Test
    public void toStringMethod() {
        assertEquals("S4", new Level("s4").toString());
    }

    @Test
    public void equals() {
        Level level = new Level("S3");

        // same values -> returns true
        assertTrue(level.equals(new Level("S3")));

        // same values with different case -> returns true
        assertTrue(level.equals(new Level("s3")));

        // same object -> returns true
        assertTrue(level.equals(level));

        // null -> returns false
        assertFalse(level.equals(null));

        // different types -> returns false
        assertFalse(level.equals(5.0f));

        // different values -> returns false
        assertFalse(level.equals(new Level("S4")));
    }

    @Test
    public void hashCode_equalLevels_sameHashCode() {
        assertEquals(new Level("S3").hashCode(), new Level("s3").hashCode());
    }
}
