package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.task.Task;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code TaskAddCommand}.
 */
public class TaskAddCommandTest {

    private static final LocalDate DUE = LocalDate.of(2026, 12, 1);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullDescription_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null,
                null, null));
    }

    @Test
    public void execute_studentLink_addsOpenTaskAboutStudent() throws Exception {
        String message = new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call parent", DUE)
                .execute(model).getFeedbackToUser();

        assertEquals("Added task: Call parent. Run 'task list' to display its index.", message);
        Task task = model.getAddressBook().getTaskList().get(0);
        assertEquals("Call parent", task.getDescription());
        assertEquals(DUE, task.getDue().get());
        assertFalse(task.isDone());
        assertTrue(task.isAbout(ALICE));
        assertTrue(task.getLessonLink().isEmpty());
    }

    @Test
    public void execute_lessonLink_addsTaskAboutLessonAndItsStudent() throws Exception {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(3)).build();
        model.addLesson(lesson);

        new TaskAddCommand(null, INDEX_FIRST_PERSON, null, null, "Prepare mock paper", null).execute(model);

        Task task = model.getAddressBook().getTaskList().get(0);
        assertTrue(task.isAbout(lesson));
        assertTrue(task.isAbout(ALICE));
        assertTrue(task.getDue().isEmpty());
    }

    @Test
    public void execute_sameTextTwice_bothAllowed() throws Exception {
        TaskAddCommand command = new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call parent", null);

        command.execute(model);
        command.execute(model);

        assertEquals(2, model.getAddressBook().getTaskList().size());
        assertFalse(model.getAddressBook().getTaskList().get(0).getId()
                .equals(model.getAddressBook().getTaskList().get(1).getId()));
    }

    @Test
    public void execute_studentIndexOutOfRange_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new TaskAddCommand(outOfBounds, null, null, null, "x", null), model,
                Messages.invalidPersonIndex(outOfBounds, model.getFilteredPersonList().size()));
        assertTrue(model.getAddressBook().getTaskList().isEmpty());
    }

    @Test
    public void execute_lessonIndexOutOfRange_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(1); // no lessons are displayed

        assertCommandFailure(new TaskAddCommand(null, outOfBounds, null, null, "x", null), model,
                Messages.invalidLessonIndex(outOfBounds, 0));
        assertTrue(model.getAddressBook().getTaskList().isEmpty());
    }

    @Test
    public void execute_homeworkOrPaymentLink_explainsToListFirst() {
        assertCommandFailure(new TaskAddCommand(null, null, INDEX_FIRST_PERSON, null, "x", null), model,
                "Invalid homework index. Run 'hw list' to display homework first.");
        assertCommandFailure(new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "x", null), model,
                "Invalid payment index. Run 'pay list' first.");
        assertTrue(model.getAddressBook().getTaskList().isEmpty());
    }

    @Test
    public void equals() {
        TaskAddCommand command = new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call", DUE);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call", DUE)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new TaskAddCommand(INDEX_SECOND_PERSON, null, null, null, "Call", DUE)));
        assertFalse(command.equals(new TaskAddCommand(null, INDEX_FIRST_PERSON, null, null, "Call", DUE)));
        assertFalse(command.equals(new TaskAddCommand(null, null, INDEX_FIRST_PERSON, null, "Call", DUE)));
        assertFalse(command.equals(new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "Call", DUE)));
        assertFalse(command.equals(new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Other", DUE)));
        assertFalse(command.equals(new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call", null)));
    }

    @Test
    public void toStringMethod() {
        TaskAddCommand command = new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call", DUE);
        String expected = TaskAddCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", lessonIndex=null, homeworkIndex=null, paymentIndex=null, description=Call, due=" + DUE + "}";
        assertEquals(expected, command.toString());
    }
}
