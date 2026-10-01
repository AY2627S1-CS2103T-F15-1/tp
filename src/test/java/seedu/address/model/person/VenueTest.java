package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class VenueTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Venue(null));
    }

    @Test
    public void constructor_invalidVenue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Venue(""));
        assertThrows(IllegalArgumentException.class, () -> new Venue("   "));
    }

    @Test
    public void constructor_extraWhitespace_whitespaceNormalized() {
        assertEquals("Blk 30 Geylang Street 29", new Venue("  Blk   30  Geylang Street 29 ").value);
    }

    @Test
    public void isValidVenue() {
        // null venue
        assertThrows(NullPointerException.class, () -> Venue.isValidVenue(null));

        // invalid venues
        assertFalse(Venue.isValidVenue("")); // empty string
        assertFalse(Venue.isValidVenue(" ")); // spaces only
        assertFalse(Venue.isValidVenue("a".repeat(101))); // too long

        // valid venues
        assertTrue(Venue.isValidVenue("Online"));
        assertTrue(Venue.isValidVenue("-")); // one character
        assertTrue(Venue.isValidVenue("Blk 456, Den Road, #01-355"));
        assertTrue(Venue.isValidVenue("a".repeat(100))); // longest allowed
        assertTrue(Venue.isValidVenue("  Starbucks Bishan  ")); // surrounding spaces
    }

    @Test
    public void toStringMethod() {
        assertEquals("Online", new Venue("Online").toString());
    }

    @Test
    public void equals() {
        Venue venue = new Venue("Online");

        // same values -> returns true
        assertTrue(venue.equals(new Venue("Online")));

        // same values with extra whitespace -> returns true
        assertTrue(venue.equals(new Venue("  Online ")));

        // same object -> returns true
        assertTrue(venue.equals(venue));

        // null -> returns false
        assertFalse(venue.equals(null));

        // different types -> returns false
        assertFalse(venue.equals(5.0f));

        // different values -> returns false
        assertFalse(venue.equals(new Venue("Starbucks Bishan")));
    }

    @Test
    public void hashCode_equalVenues_sameHashCode() {
        assertEquals(new Venue("Online").hashCode(), new Venue(" Online ").hashCode());
    }
}
