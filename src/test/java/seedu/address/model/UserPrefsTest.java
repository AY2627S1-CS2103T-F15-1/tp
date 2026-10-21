package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class UserPrefsTest {

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void tourSeen_isFalseUntilSet() {
        UserPrefs userPrefs = new UserPrefs();
        assertFalse(userPrefs.isTourSeen());

        userPrefs.setTourSeen(true);
        assertTrue(userPrefs.isTourSeen());
    }

    @Test
    public void copyConstructor_keepsWhetherTheTourWasSeen() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setTourSeen(true);

        assertTrue(new UserPrefs(userPrefs).isTourSeen());
        assertFalse(new UserPrefs(new UserPrefs()).isTourSeen());
    }

    @Test
    public void equalsAndHashCode_dependOnWhetherTheTourWasSeen() {
        UserPrefs seen = new UserPrefs();
        seen.setTourSeen(true);

        assertNotEquals(new UserPrefs(), seen);
        assertNotEquals(new UserPrefs().hashCode(), seen.hashCode());
        assertEquals(seen, new UserPrefs(seen));
        assertEquals(seen.hashCode(), new UserPrefs(seen).hashCode());
        assertTrue(seen.toString().contains("Tour seen : true"));
    }

}
