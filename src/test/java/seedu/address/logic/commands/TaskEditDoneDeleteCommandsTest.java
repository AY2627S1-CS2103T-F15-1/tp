package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.task.Task;
import seedu.address.testutil.TaskBuilder;

/**
 * Tests {@code TaskEditCommand}, {@code TaskDoneCommand} and {@code TaskDeleteCommand}, which all take their index
 * from the tasks shown by the last {@code task list}.
 */
public class TaskEditDoneDeleteCommandsTest {

    private static final LocalDate DUE = LocalDate.of(2026, 12, 1);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @BeforeEach
    public void addAndShowTasks() throws Exception {
        model.addTask(new TaskBuilder().withId("a").withDescription("Call the parent").withDue(DUE).build());
        model.addTask(new TaskBuilder().withId("b").withDescription("Second").withDue(DUE.plusDays(1)).build());
        new TaskListCommand(false, null).execute(model);
    }

    // ---- task edit

    @Test
    public void edit_descriptionOnly_keepsDueDate() throws Exception {
        String message = new TaskEditCommand(INDEX_FIRST_PERSON, "Call again", null, false)
                .execute(model).getFeedbackToUser();

        assertEquals("Updated task 1: Call again. Shown task indices stay unchanged; run 'task list' to refresh.",
                message);
        Task edited = model.findTask("a").get();
        assertEquals("Call again", edited.getDescription());
        assertEquals(DUE, edited.getDue().get());
    }

    @Test
    public void edit_newDueDate_changesIt() throws Exception {
        new TaskEditCommand(INDEX_FIRST_PERSON, "Call the parent", DUE.plusMonths(1), false).execute(model);

        assertEquals(DUE.plusMonths(1), model.findTask("a").get().getDue().get());
    }

    @Test
    public void edit_clearDue_removesIt() throws Exception {
        new TaskEditCommand(INDEX_FIRST_PERSON, "Call the parent", null, true).execute(model);

        assertTrue(model.findTask("a").get().getDue().isEmpty());
    }

    @Test
    public void edit_keepsIdStatusAndLink() throws Exception {
        model.setTask(model.findTask("a").get(), model.findTask("a").get().markDone());

        new TaskEditCommand(INDEX_FIRST_PERSON, "Renamed", null, false).execute(model);

        Task edited = model.findTask("a").get();
        assertTrue(edited.isDone());
        assertEquals(1, model.getAddressBook().getTaskList().stream().filter(t -> t.getId().equals("a")).count());
    }

    @Test
    public void edit_indexNotShown_throwsCommandException() {
        assertCommandFailure(new TaskEditCommand(Index.fromOneBased(3), "x", null,
                false), model, "Invalid task index. Run 'task list' first.");
    }

    @Test
    public void edit_noTaskListShownYet_throwsCommandException() {
        Model fresh = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertCommandFailure(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, false), fresh,
                "Invalid task index. Run 'task list' first.");
    }

    @Test
    public void edit_taskDeletedMeanwhile_throwsCommandException() {
        model.deleteTask(model.findTask("a").get());

        assertCommandFailure(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, false), model,
                "That task was deleted. Run 'task list' to refresh.");
    }

    // ---- task done

    @Test
    public void done_marksTaskDoneAndKeepsIndicesStable() throws Exception {
        String message = new TaskDoneCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser();

        assertEquals("Completed task 1: Call the parent. Shown task indices stay unchanged; "
                + "run 'task list' to refresh.", message);
        assertTrue(model.findTask("a").get().isDone());
        // The second task is still number 2 until the next listing
        new TaskDoneCommand(INDEX_SECOND_PERSON).execute(model);
        assertTrue(model.findTask("b").get().isDone());
    }

    @Test
    public void done_alreadyDone_throwsCommandException() throws Exception {
        new TaskDoneCommand(INDEX_FIRST_PERSON).execute(model);

        assertCommandFailure(new TaskDoneCommand(INDEX_FIRST_PERSON), model, "That task is already done.");
    }

    @Test
    public void done_indexNotShown_throwsCommandException() {
        assertCommandFailure(new TaskDoneCommand(Index.fromOneBased(9)), model,
                "Invalid task index. Run 'task list' first.");
    }

    @Test
    public void done_completedTaskStaysInAllListing() throws Exception {
        new TaskDoneCommand(INDEX_FIRST_PERSON).execute(model);

        String message = new TaskListCommand(true, null).execute(model).getFeedbackToUser();

        assertTrue(message.contains("[done] Call the parent"));
        assertFalse(new TaskListCommand(false, null).execute(model).getFeedbackToUser()
                .contains("Call the parent"));
    }

    // ---- task delete

    @Test
    public void delete_withoutConfirm_onlyAsksAndDeletesNothing() throws Exception {
        String message = new TaskDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals("Delete task: Call the parent? Type 'task delete 1 confirm' to proceed.", message);
        assertEquals(2, model.getAddressBook().getTaskList().size());
    }

    @Test
    public void delete_confirmed_deletesAndKeepsOtherIndices() throws Exception {
        String message = new TaskDeleteCommand(INDEX_FIRST_PERSON, true).execute(model).getFeedbackToUser();

        assertEquals("Deleted task 1: Call the parent. Remaining shown indices are unchanged; "
                + "run 'task list' to refresh.", message);
        assertTrue(model.findTask("a").isEmpty());
        // Task 2 is still task 2, and deleting task 1 again says that it is gone
        assertEquals("Delete task: Second? Type 'task delete 2 confirm' to proceed.",
                new TaskDeleteCommand(INDEX_SECOND_PERSON, false).execute(model).getFeedbackToUser());
        assertCommandFailure(new TaskDeleteCommand(INDEX_FIRST_PERSON, true), model,
                "That task was deleted. Run 'task list' to refresh.");
    }

    @Test
    public void delete_indexNotShown_throwsCommandException() {
        assertCommandFailure(new TaskDeleteCommand(Index.fromOneBased(3), true),
                model, "Invalid task index. Run 'task list' first.");
    }

    // ---- equals and toString

    @Test
    public void equalsAndToString() {
        assertTrue(new TaskEditCommand(INDEX_FIRST_PERSON, "x", DUE, false)
                .equals(new TaskEditCommand(INDEX_FIRST_PERSON, "x", DUE, false)));
        assertFalse(new TaskEditCommand(INDEX_FIRST_PERSON, "x", DUE, false)
                .equals(new TaskEditCommand(INDEX_SECOND_PERSON, "x", DUE, false)));
        assertFalse(new TaskEditCommand(INDEX_FIRST_PERSON, "x", DUE, false)
                .equals(new TaskEditCommand(INDEX_FIRST_PERSON, "y", DUE, false)));
        assertFalse(new TaskEditCommand(INDEX_FIRST_PERSON, "x", DUE, false)
                .equals(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, false)));
        assertFalse(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, false)
                .equals(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, true)));
        assertFalse(new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, false).equals(null));

        assertTrue(new TaskDoneCommand(INDEX_FIRST_PERSON).equals(new TaskDoneCommand(INDEX_FIRST_PERSON)));
        assertFalse(new TaskDoneCommand(INDEX_FIRST_PERSON).equals(new TaskDoneCommand(INDEX_SECOND_PERSON)));
        assertFalse(new TaskDoneCommand(INDEX_FIRST_PERSON).equals(null));

        assertTrue(new TaskDeleteCommand(INDEX_FIRST_PERSON, true)
                .equals(new TaskDeleteCommand(INDEX_FIRST_PERSON, true)));
        assertFalse(new TaskDeleteCommand(INDEX_FIRST_PERSON, true)
                .equals(new TaskDeleteCommand(INDEX_FIRST_PERSON, false)));
        assertFalse(new TaskDeleteCommand(INDEX_FIRST_PERSON, true)
                .equals(new TaskDeleteCommand(INDEX_SECOND_PERSON, true)));
        assertFalse(new TaskDeleteCommand(INDEX_FIRST_PERSON, true).equals(null));

        assertEquals(TaskDoneCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON + "}",
                new TaskDoneCommand(INDEX_FIRST_PERSON).toString());
        assertEquals(TaskDeleteCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", isConfirmed=true}", new TaskDeleteCommand(INDEX_FIRST_PERSON, true).toString());
        assertEquals(TaskEditCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", description=x, newDue=null, isClearingDue=true}",
                new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, true).toString());
    }
}
