package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalLessons.getTypicalAddressBookWithLessons;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_studentsAndLessons_clearsBoth() {
        Model model = new ModelManager(getTypicalAddressBookWithLessons(), new UserPrefs());
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_notConfirmed_offersTheConfirmingCommand() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertEquals(Optional.of("clear confirm"), new ClearCommand().execute(model).getConfirmationCommand());
        assertTrue(new ClearCommand(true).execute(model).getConfirmationCommand().isEmpty());
    }

    @Test
    public void execute_notConfirmed_asksForConfirmationAndKeepsData() {
        Model model = new ModelManager(getTypicalAddressBookWithLessons(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBookWithLessons(), new UserPrefs());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_CONFIRMATION, expectedModel);
    }

    @Test
    public void equals() {
        ClearCommand confirmed = new ClearCommand(true);

        assertTrue(confirmed.equals(confirmed)); // same object
        assertTrue(confirmed.equals(new ClearCommand(true)));
        assertTrue(new ClearCommand().equals(new ClearCommand(false))); // not confirmed is the default
        assertFalse(confirmed.equals(null));
        assertFalse(confirmed.equals(new ListCommand()));
        assertFalse(confirmed.equals(new ClearCommand(false)));
    }

    @Test
    public void toStringMethod() {
        String expected = ClearCommand.class.getCanonicalName() + "{isConfirmed=true}";
        assertEquals(expected, new ClearCommand(true).toString());
    }

}
