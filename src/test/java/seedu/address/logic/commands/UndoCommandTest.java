package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.UndoCommand.MESSAGE_NOTHING;
import static seedu.address.logic.commands.UndoCommand.MESSAGE_SUCCESS;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code UndoCommand}.
 */
public class UndoCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_nothing_failure() {
        CommandResult expected = new CommandResult(MESSAGE_NOTHING, false, false);
        assertCommandSuccess(new UndoCommand(), model, expected, expectedModel);
    }

    @Test
    public void execute_add_failure() {
        // GEORGE is the last person, so this does not affect the order of persons.
        model.deletePerson(GEORGE);
        model.addPerson(GEORGE);
        CommandResult expected = new CommandResult(MESSAGE_NOTHING, false, false);
        assertCommandSuccess(new UndoCommand(), model, expected, expectedModel);
    }

    @Test
    public void execute_delete_success() {
        Homework hw = Homework.assign(BENSON.getId(), "hw test", LocalDate.of(3000, 10, 20));
        model.addHomework(hw);
        model.deleteHomework(hw);
        CommandResult expected = new CommandResult(
                String.format(MESSAGE_SUCCESS, "homework deletion"),
                false, false);
        assertCommandSuccess(new UndoCommand(), model, expected, expectedModel);
    }
}
