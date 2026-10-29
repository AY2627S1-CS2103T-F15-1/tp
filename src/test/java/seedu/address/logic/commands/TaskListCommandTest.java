package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.task.Task;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TaskBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code TaskListCommand}.
 */
public class TaskListCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_noTasks_saysSo() throws Exception {
        assertEquals(TaskListCommand.MESSAGE_NO_TASKS,
                new TaskListCommand(false, null).execute(model).getFeedbackToUser());
        assertTrue(model.getShownTasks().isEmpty());
    }

    @Test
    public void execute_openTasks_sortedByDueDateWithUndatedLast() throws Exception {
        Task undated = new TaskBuilder().withId("a").withDescription("Undated").build();
        Task late = new TaskBuilder().withId("b").withDescription("Late").withDue(LocalDate.of(2026, 12, 9))
                .build();
        Task early = new TaskBuilder().withId("c").withDescription("Early").withDue(LocalDate.of(2026, 10, 20))
                .build();
        Task finished = new TaskBuilder().withId("d").withDescription("Finished").done().build();
        Task alsoUndated = new TaskBuilder().withId("e").withDescription("Also undated").build();
        for (Task task : List.of(undated, late, early, finished, alsoUndated)) {
            model.addTask(task);
        }

        String message = new TaskListCommand(false, null).execute(model).getFeedbackToUser();

        String student = ALICE.getName() + " (" + ALICE.getPhone() + ") | student";
        assertEquals("Tasks (4)\n"
                + "1. [open] Early due 2026-10-20 | " + student + "\n"
                + "2. [open] Late due 2026-12-09 | " + student + "\n"
                + "3. [open] Undated (no due date) | " + student + "\n"
                + "4. [open] Also undated (no due date) | " + student, message);
        assertEquals(List.of(early, late, undated, alsoUndated), model.getShownTasks());
    }

    @Test
    public void execute_all_includesCompletedTasks() throws Exception {
        model.addTask(new TaskBuilder().withId("a").build());
        model.addTask(new TaskBuilder().withId("b").withDescription("Finished").done().build());

        String message = new TaskListCommand(true, null).execute(model).getFeedbackToUser();

        assertTrue(message.startsWith("Tasks (2)"));
        assertTrue(message.contains("[done] Finished (no due date)"));
    }

    @Test
    public void execute_forStudent_limitsToThatStudentsTasks() throws Exception {
        model.addTask(new TaskBuilder().withId("a").withDescription("Alice task").build());
        model.addTask(new TaskBuilder().withId("b").withDescription("Benson task").withStudent(BENSON).build());

        String message = new TaskListCommand(false, INDEX_SECOND_PERSON).execute(model).getFeedbackToUser();

        assertTrue(message.startsWith("Tasks (1)"));
        assertTrue(message.contains("Benson task"));
        assertFalse(message.contains("Alice task"));
    }

    @Test
    public void execute_lessonTask_showsLessonAsItNowReads() throws Exception {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(3)).build();
        model.addLesson(lesson);
        model.addTask(new TaskBuilder().withLesson(lesson).withDescription("Prepare mock paper").build());
        model.setLesson(lesson, lesson.complete("Done"));

        String message = new TaskListCommand(false, null).execute(model).getFeedbackToUser();

        assertTrue(message.contains("| lesson Math " + lesson.getDate() + " " + lesson.getTimeRange()
                + " (completed)"), message);
    }

    @Test
    public void execute_paymentTask_showsPaymentAsItNowReads() throws Exception {
        LedgerEntry payment = LedgerEntry.payment(ALICE, new java.math.BigDecimal("25.00"),
                LocalDate.of(2026, 10, 18), "PayNow");
        model.addLedgerEntry(payment);
        model.addTask(new TaskBuilder().withPayment(payment, ALICE).withDescription("Remind about fees").build());

        String message = new TaskListCommand(false, null).execute(model).getFeedbackToUser();

        assertTrue(message.contains("| payment $25.00 on 2026-10-18 - PayNow"), message);
    }

    @Test
    public void execute_afterListing_numbersStayFixedUntilNextListing() throws Exception {
        Task first = new TaskBuilder().withId("a").withDescription("First").build();
        model.addTask(first);
        new TaskListCommand(false, null).execute(model);

        model.addTask(new TaskBuilder().withId("b").withDescription("Second").withDue(LocalDate.of(2020, 1, 1))
                .build());

        assertEquals(List.of(first), model.getShownTasks());
    }

    @Test
    public void execute_badStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new TaskListCommand(false, outOfBounds), model,
                Messages.invalidPersonIndex(outOfBounds, model.getFilteredPersonList().size()));
    }

    @Test
    public void equals() {
        TaskListCommand command = new TaskListCommand(false, null);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new TaskListCommand(false, null)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new TaskListCommand(true, null)));
        assertFalse(command.equals(new TaskListCommand(false, INDEX_FIRST_PERSON)));
    }

    @Test
    public void toStringMethod() {
        String expected = TaskListCommand.class.getCanonicalName() + "{isShowingAll=true, studentIndex="
                + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, new TaskListCommand(true, INDEX_FIRST_PERSON).toString());
    }
}
