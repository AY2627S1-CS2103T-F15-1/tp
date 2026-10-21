package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void withConfirmationCommand_keepsTheRestOfTheResult() {
        CommandResult plain = new CommandResult("feedback", true, false);
        assertTrue(plain.getConfirmationCommand().isEmpty());

        CommandResult preview = plain.withConfirmationCommand("delete 1 confirm");

        assertEquals(Optional.of("delete 1 confirm"), preview.getConfirmationCommand());
        assertEquals("feedback", preview.getFeedbackToUser());
        assertTrue(preview.isShowHelp());
        assertEquals(Optional.of("delete 1 confirm"), preview.withFeedback("other").getConfirmationCommand());
        assertEquals(Optional.of("delete 1 confirm"), preview.withAppendedText(" more").getConfirmationCommand());
        assertThrows(NullPointerException.class, () -> plain.withConfirmationCommand(null));
    }

    @Test
    public void withTourRequested_marksTheResultAndKeepsTheRest() {
        CommandResult plain = new CommandResult("feedback").withConfirmationCommand("delete 1 confirm");
        assertFalse(plain.isTourRequested());

        CommandResult tour = plain.withTourRequested();

        assertTrue(tour.isTourRequested());
        assertEquals("feedback", tour.getFeedbackToUser());
        assertEquals(Optional.of("delete 1 confirm"), tour.getConfirmationCommand());
        assertTrue(tour.withFeedback("other").isTourRequested());
        assertTrue(tour.withAppendedText(" more").isTourRequested());
        assertTrue(tour.withDisplayedLessons(List.of()).isTourRequested());
        assertTrue(tour.withDisplayedHomework(List.of()).isTourRequested());
        assertTrue(tour.withAddedStudents(List.of()).isTourRequested());
        assertTrue(tour.withConfirmationCommand("x").isTourRequested());
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + "}";
        assertEquals(expected, commandResult.toString());
    }
}
