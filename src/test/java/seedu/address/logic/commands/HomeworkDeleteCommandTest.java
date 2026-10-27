package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.task.Task;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkDeleteCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(),
            new UserPrefs());

    private void showWith(Homework... items) throws CommandException {
        for (Homework item : items) {
            model.addHomework(item);
        }
        new HomeworkListCommand(null, false, false, TODAY).execute(model);
    }

    private String rejection(HomeworkDeleteCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    @Test
    public void execute_noFeedback_deletesAtOnce() throws Exception {
        showWith(new HomeworkBuilder(ALICE).build());

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals("Deleted homework 1: Algebra Q1-10. Remaining shown indices are unchanged; "
                + "run 'hw list' to refresh.", message);
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
    }

    @Test
    public void execute_withFeedbackUnconfirmed_asksAndDeletesNothing() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).withFeedback("Keep").build());

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals("This homework has feedback recorded: Algebra Q1-10. "
                + "Type 'hw delete 1 confirm' to delete it anyway. Nothing is deleted.", message);
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void execute_withFeedbackConfirmed_deletes() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).withFeedback("Keep").build());

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true).execute(model).getFeedbackToUser();

        assertEquals("Deleted homework 1: Algebra Q1-10. Remaining shown indices are unchanged; "
                + "run 'hw list' to refresh.", message);
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
    }

    @Test
    public void execute_withLinkedTaskUnconfirmed_asksAndDeletesNothing() throws Exception {
        Homework item = new HomeworkBuilder(ALICE).build();
        showWith(item);
        model.addTask(Task.create("Mark it", null, ALICE, item));

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals("This deletes homework 'Algebra Q1-10', any feedback, and 1 linked task(s). "
                + "Type 'hw delete 1 confirm' to proceed.", message);
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
        assertEquals(1, model.getAddressBook().getTaskList().size());
    }

    @Test
    public void execute_withFeedbackAndTasksUnconfirmed_usesTheTaskMessage() throws Exception {
        Homework item = new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).withFeedback("Keep").build();
        showWith(item);
        model.addTask(Task.create("Mark it", null, ALICE, item));
        model.addTask(Task.create("Call parent", null, ALICE, item));

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals("This deletes homework 'Algebra Q1-10', any feedback, and 2 linked task(s). "
                + "Type 'hw delete 1 confirm' to proceed.", message);
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
        assertEquals(2, model.getAddressBook().getTaskList().size());
    }

    @Test
    public void execute_withLinkedTaskConfirmed_deletesHomeworkAndItsTasksOnly() throws Exception {
        Homework item = new HomeworkBuilder(ALICE).build();
        showWith(item);
        Task linked = Task.create("Mark it", null, ALICE, item);
        Task other = Task.create("Call parent", null, ALICE);
        model.addTask(linked);
        model.addTask(other);

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true).execute(model).getFeedbackToUser();

        assertEquals("Deleted homework 1: Algebra Q1-10. Remaining shown indices are unchanged; "
                + "run 'hw list' to refresh.", message);
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
        assertEquals(List.of(other), List.copyOf(model.getAddressBook().getTaskList()));
    }

    @Test
    public void execute_taskAboutAnotherItem_doesNotNeedConfirmation() throws Exception {
        Homework first = new HomeworkBuilder(ALICE).withDescription("First").build();
        Homework second = new HomeworkBuilder(ALICE).withDescription("Second").build();
        showWith(first, second);
        model.addTask(Task.create("Mark second", null, ALICE, second));

        String message = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertTrue(message.startsWith("Deleted homework 1: First."), message);
        assertEquals(1, model.getAddressBook().getTaskList().size());
    }

    @Test
    public void execute_confirmationPromptNamesTheShownNumber() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withDescription("First").build(),
                new HomeworkBuilder(ALICE).withDescription("Second").withStatus(HomeworkStatus.REVIEWED)
                        .withFeedback("Keep").build());

        assertEquals("This homework has feedback recorded: Second. "
                + "Type 'hw delete 2 confirm' to delete it anyway. Nothing is deleted.",
                new HomeworkDeleteCommand(INDEX_SECOND_PERSON, false).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_deletedNumber_isReportedAndNeverSelectsTheNextItem() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withDescription("First").build(),
                new HomeworkBuilder(ALICE).withDescription("Second").build());
        new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model);

        assertEquals("That homework was deleted. Run 'hw list' to refresh.",
                rejection(new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false)));
        assertEquals("Deleted homework 2: Second. Remaining shown indices are unchanged; "
                + "run 'hw list' to refresh.",
                new HomeworkDeleteCommand(INDEX_SECOND_PERSON, false).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_indexOutsideTheLastListing_rejected() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).build());

        String expected = "Invalid homework index. Run 'hw list' to display homework first.";
        assertEquals(expected, rejection(new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false)));
        showWith();
        assertEquals(expected, rejection(new HomeworkDeleteCommand(INDEX_SECOND_PERSON, false)));
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkDeleteCommand(null, false));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkDeleteCommand command = new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true);

        assertEquals(command, command);
        assertEquals(command, new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true));
        assertEquals(command.hashCode(), new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new HomeworkDeleteCommand(INDEX_SECOND_PERSON, true));
        assertNotEquals(command, new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false));
    }

    @Test
    public void toStringMethod() {
        assertEquals(HomeworkDeleteCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", isConfirmed=true}", new HomeworkDeleteCommand(INDEX_FIRST_PERSON, true).toString());
    }

    @Test
    public void execute_otherItemsKeepTheirRecords() throws Exception {
        Homework first = new HomeworkBuilder(ALICE).withDescription("First").build();
        Homework second = new HomeworkBuilder(ALICE).withDescription("Second").build();
        showWith(first, second);

        new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false).execute(model);

        assertEquals(List.of(second), List.copyOf(model.getAddressBook().getHomeworkList()));
    }
}
