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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
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
        assertEquals(ALICE.getId(), task.getStudentId());
        assertTrue(task.getLessonId().isEmpty());
        assertTrue(task.getHomeworkId().isEmpty());
    }

    @Test
    public void execute_lessonLink_addsTaskAboutLessonAndItsStudent() throws Exception {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(3)).build();
        model.addLesson(lesson);

        new TaskAddCommand(null, INDEX_FIRST_PERSON, null, null, "Prepare mock paper", null).execute(model);

        Task task = model.getAddressBook().getTaskList().get(0);
        assertTrue(task.isAbout(lesson));
        assertTrue(task.isAbout(ALICE));
        assertEquals(lesson.getId(), task.getLessonId().get());
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
    public void execute_homeworkLink_addsTaskAboutThatHomeworkAndItsStudent() throws Exception {
        Homework homework = Homework.assign(ALICE.getId(), "Practice set", LocalDate.now().plusDays(2));
        model.addHomework(homework);
        model.showHomework(List.of(homework));

        new TaskAddCommand(null, null, INDEX_FIRST_PERSON, null, "Mark the set", null).execute(model);

        Task task = model.getAddressBook().getTaskList().get(0);
        assertTrue(task.isAbout(homework));
        assertTrue(task.isAbout(ALICE));
        assertEquals(homework.getId(), task.getHomeworkId().get());
        assertTrue(task.getLessonId().isEmpty());
    }

    @Test
    public void execute_homeworkLinkWithoutListOrOutOfRange_explainsToListFirst() {
        assertCommandFailure(new TaskAddCommand(null, null, INDEX_FIRST_PERSON, null, "x", null), model,
                HomeworkCommandSupport.MESSAGE_INVALID_INDEX);
        Homework homework = Homework.assign(ALICE.getId(), "Practice set", LocalDate.now().plusDays(2));
        model.addHomework(homework);
        model.showHomework(List.of(homework));
        assertCommandFailure(new TaskAddCommand(null, null, INDEX_SECOND_PERSON, null, "x", null), model,
                HomeworkCommandSupport.MESSAGE_INVALID_INDEX);
        assertTrue(model.getAddressBook().getTaskList().isEmpty());
    }

    @Test
    public void execute_homeworkDeletedSinceTheList_reportsItAsDeleted() {
        Homework homework = Homework.assign(ALICE.getId(), "Practice set", LocalDate.now().plusDays(2));
        model.addHomework(homework);
        model.showHomework(List.of(homework));
        model.deleteHomework(homework);

        assertCommandFailure(new TaskAddCommand(null, null, INDEX_FIRST_PERSON, null, "x", null), model,
                HomeworkCommandSupport.MESSAGE_DELETED);
        assertTrue(model.getAddressBook().getTaskList().isEmpty());
    }

    @Test
    public void execute_paymentLink_addsTaskAboutThatPaymentAndItsStudent() throws Exception {
        LedgerEntry payment = LedgerEntry.payment(ALICE, new BigDecimal("25.00"),
                LocalDate.now().minusDays(1), "PayNow");
        model.addLedgerEntry(payment);
        model.setShownPayments(List.of(payment));

        new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "Remind about fees", null).execute(model);

        Task task = model.getAddressBook().getTaskList().get(0);
        assertEquals(payment.id(), task.getPaymentId().orElseThrow());
        assertEquals(ALICE.getId(), task.getStudentId());
        assertTrue(task.getLessonId().isEmpty());
        assertTrue(task.getHomeworkId().isEmpty());
    }

    @Test
    public void execute_paymentLinkWithoutAListOrOutOfRange_explainsToListFirst() {
        assertCommandFailure(new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "x", null), model,
                TaskAddCommand.MESSAGE_INVALID_PAYMENT_INDEX);
        LedgerEntry payment = LedgerEntry.payment(ALICE, BigDecimal.TEN, LocalDate.now());
        model.addLedgerEntry(payment);
        model.setShownPayments(List.of(payment));
        assertCommandFailure(new TaskAddCommand(null, null, null, INDEX_SECOND_PERSON, "x", null), model,
                TaskAddCommand.MESSAGE_INVALID_PAYMENT_INDEX);
    }

    @Test
    public void execute_paymentDeletedSinceTheList_reportsItAsDeleted() {
        LedgerEntry payment = LedgerEntry.payment(ALICE, BigDecimal.TEN, LocalDate.now());
        model.addLedgerEntry(payment);
        model.setShownPayments(List.of(payment));
        model.deletePerson(ALICE);
        model.addPerson(ALICE);

        assertCommandFailure(new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "x", null), model,
                TaskAddCommand.MESSAGE_PAYMENT_DELETED);
    }

    @Test
    public void execute_shownEntryIsNotAPayment_reportsAnInvalidPaymentIndex() {
        LedgerEntry charge = LedgerEntry.charge(ALICE, new seedu.address.testutil.LessonBuilder()
                .withStudent(ALICE).withDate(LocalDate.now()).build(), BigDecimal.TEN);
        model.setShownPayments(List.of(charge));

        assertCommandFailure(new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "x", null), model,
                TaskAddCommand.MESSAGE_INVALID_PAYMENT_INDEX);
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
