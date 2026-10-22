package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
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
    private final Homework hw = Homework.assign(BENSON.getId(), "hw test", LocalDate.of(3000, 10, 20));
    private final Model model;
    private final Model expectedModel;

    public UndoCommandTest() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addHomework(hw);
        expectedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
    }

    @Test
    public void execute_nothing_failure() {
        assertCommandFailure(new UndoCommand(), model, MESSAGE_NOTHING);
    }

    @Test
    public void execute_add_failure() {
        // GEORGE is the last person, so this does not affect the order of persons.
        model.deletePerson(GEORGE);
        model.addPerson(GEORGE);
        assertCommandFailure(new UndoCommand(), model, MESSAGE_NOTHING);
    }

    @Test
    public void execute_deleteAndUndo_successAndFailure() {
        model.deleteHomework(hw);
        CommandResult expected = new CommandResult(
                String.format(MESSAGE_SUCCESS, "homework deletion"),
                false, false);
        assertCommandSuccess(new UndoCommand(), model, expected, expectedModel);
        assertCommandFailure(new UndoCommand(), model, MESSAGE_NOTHING);
    }
}
