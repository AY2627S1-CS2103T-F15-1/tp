package seedu.address.model;

import seedu.address.commons.core.GuiSettings;

/**
 * Unmodifiable view of user prefs.
 */
public interface ReadOnlyUserPrefs {

    GuiSettings getGuiSettings();

    /** Returns whether the user has finished or left the guided tour, so that it is not offered at every start. */
    boolean isTourSeen();

}
