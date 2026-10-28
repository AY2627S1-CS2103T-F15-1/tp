package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

public class TravelCommandTest {
    @Test
    public void constructor_invalidBuffer_rejectsBeforeChangingModel() {
        assertThrows(IllegalArgumentException.class, () -> new TravelCommand(-1));
        assertThrows(IllegalArgumentException.class, () -> new TravelCommand(181));
        assertThrows(NullPointerException.class, () -> new TravelCommand(30).execute(null));
    }

    @Test
    public void execute_emptyData_changesOnlyBuffer() {
        ModelManager model = new ModelManager();
        assertEquals("Travel buffer set to 0 minutes.", new TravelCommand(0).execute(model).getFeedbackToUser());
        assertEquals(0, model.getAddressBook().getTravelBuffer());
        assertTrue(model.getAddressBook().getLessonList().isEmpty());
        assertEquals("Travel buffer set to 1 minute.", new TravelCommand(1).execute(model).getFeedbackToUser());
    }

    @Test
    public void equalsAndHashCode_dependOnBuffer() {
        TravelCommand command = new TravelCommand(30);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new TravelCommand(30)));
        assertFalse(command.equals(new TravelCommand(31)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertEquals(command.hashCode(), new TravelCommand(30).hashCode());
        assertEquals(TravelCommand.class.getCanonicalName() + "{minutes=30}", command.toString());
    }
}
