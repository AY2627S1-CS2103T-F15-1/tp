package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class TourCommandTest {

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TourCommand().execute(null));
    }

    @Test
    public void execute_asksTheWindowToStartTheTourAndChangesNothing() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        CommandResult result = new TourCommand().execute(model);

        assertTrue(result.isTourRequested());
        assertEquals(TourCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
        assertFalse(model.isTourSeen(), "only finishing or leaving the tour counts as seeing it");
    }

    @Test
    public void usage_namesTheCommandAndSaysItChangesNoData() {
        assertTrue(TourCommand.MESSAGE_USAGE.startsWith("tour: "));
        assertTrue(TourCommand.MESSAGE_USAGE.contains("changes none of your data"));
        assertTrue(TourCommand.MESSAGE_USAGE.endsWith("Example: tour"));
    }
}
