package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkStatusCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(),
            new UserPrefs());

    private void showWith(Homework... items) throws CommandException {
        for (Homework item : items) {
            model.addHomework(item);
        }
        new HomeworkListCommand(null, false, false, TODAY).execute(model);
    }

    private static HomeworkStatusCommand status(int index, HomeworkStatus status) {
        return new HomeworkStatusCommand(Index.fromOneBased(index), status);
    }

    private String rejection(HomeworkStatusCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    private Homework stored() {
        return model.getAddressBook().getHomeworkList().getFirst();
    }

    @Test
    public void execute_forward_reportsTheNewStatus() throws Exception {
        showWith(new HomeworkBuilder(ALICE).build());

        assertEquals("Homework 1: Algebra Q1-10 is now: submitted",
                status(1, HomeworkStatus.SUBMITTED).execute(model).getFeedbackToUser());
        assertEquals(HomeworkStatus.SUBMITTED, stored().getStatus());
        assertEquals("Homework 1: Algebra Q1-10 is now: reviewed",
                status(1, HomeworkStatus.REVIEWED).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_assignedStraightToReviewed_hasNoBackwardNote() throws Exception {
        showWith(new HomeworkBuilder(ALICE).build());

        assertEquals("Homework 1: Algebra Q1-10 is now: reviewed",
                status(1, HomeworkStatus.REVIEWED).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_unchangedStatus_rejected() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).build());

        assertEquals("That homework is already marked submitted.", rejection(status(1, HomeworkStatus.SUBMITTED)));
    }

    @Test
    public void execute_backwardMove_isAllowedWithANoteAndKeepsFeedback() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.REVIEWED).withFeedback("Keep me").build());

        assertEquals("Homework 1: Algebra Q1-10 is now: assigned\n"
                + "Note: moved back from reviewed to assigned. Feedback already recorded is kept.",
                status(1, HomeworkStatus.ASSIGNED).execute(model).getFeedbackToUser());
        assertEquals("Keep me", stored().getFeedback().orElseThrow());
        assertEquals("Homework 1: Algebra Q1-10 is now: submitted",
                status(1, HomeworkStatus.SUBMITTED).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_reviewedBackToSubmitted_notesTheMove() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.REVIEWED).build());

        assertEquals("Homework 1: Algebra Q1-10 is now: submitted\n"
                + "Note: moved back from reviewed to submitted. Feedback already recorded is kept.",
                status(1, HomeworkStatus.SUBMITTED).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_overdueItemSubmitted_isNoLongerOverdue() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withDueDate(TODAY.minusDays(3)).build());
        assertTrue(stored().isOverdue(TODAY));

        status(1, HomeworkStatus.SUBMITTED).execute(model);

        assertFalse(stored().isOverdue(TODAY));
    }

    @Test
    public void execute_invalidOrDeletedIndex_rejected() throws Exception {
        Homework first = new HomeworkBuilder(ALICE).withDescription("First").build();
        showWith(first, new HomeworkBuilder(ALICE).withDescription("Second").build());

        assertEquals("Invalid homework index. Run 'hw list' to display homework first.",
                rejection(status(3, HomeworkStatus.SUBMITTED)));

        model.deleteHomework(first);
        assertEquals("That homework was deleted. Run 'hw list' to refresh.",
                rejection(status(1, HomeworkStatus.SUBMITTED)));
        assertEquals(HomeworkStatus.ASSIGNED, stored().getStatus());
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkStatusCommand(null, HomeworkStatus.ASSIGNED));
        assertThrows(NullPointerException.class, () -> new HomeworkStatusCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkStatusCommand command = new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED);

        assertEquals(command, command);
        assertEquals(command, new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED));
        assertEquals(command.hashCode(),
                new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new HomeworkStatusCommand(INDEX_SECOND_PERSON, HomeworkStatus.SUBMITTED));
        assertNotEquals(command, new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.REVIEWED));
    }

    @Test
    public void toStringMethod() {
        assertEquals(HomeworkStatusCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", status=submitted}", new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED)
                .toString());
    }
}
