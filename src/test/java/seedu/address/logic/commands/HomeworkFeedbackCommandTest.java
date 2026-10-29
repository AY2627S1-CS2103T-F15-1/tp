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

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkFeedbackCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(),
            new UserPrefs());

    private void showWith(Homework... items) throws CommandException {
        for (Homework item : items) {
            model.addHomework(item);
        }
        new HomeworkListCommand(null, false, false, TODAY).execute(model);
    }

    private String rejection(HomeworkFeedbackCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    private Homework stored() {
        return model.getAddressBook().getHomeworkList().getFirst();
    }

    @Test
    public void execute_submittedHomework_recordsTheFeedback() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).build());

        String message = new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Sign errors in Q8-12.").execute(model)
                .getFeedbackToUser();

        assertEquals("Feedback recorded for homework 1: Algebra Q1-10", message);
        assertEquals("Sign errors in Q8-12.", stored().getFeedback().orElseThrow());
        assertEquals(HomeworkStatus.SUBMITTED, stored().getStatus());
    }

    @Test
    public void execute_feedbackAgain_replacesInsteadOfAppending() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.REVIEWED).withFeedback("Old").build());

        new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "New").execute(model);

        assertEquals("New", stored().getFeedback().orElseThrow());
    }

    @Test
    public void execute_clearFeedback_submittedAndReviewedHomeworkKeepStatusAndIdentity() throws Exception {
        for (HomeworkStatus status : List.of(HomeworkStatus.SUBMITTED, HomeworkStatus.REVIEWED)) {
            Homework original = new HomeworkBuilder(ALICE).withStatus(status).withFeedback("Mistaken").build();
            model.addHomework(original);
            new HomeworkListCommand(null, false, false, TODAY).execute(model);
            new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "").execute(model);
            assertEquals(original.withoutFeedback(), stored());
            assertTrue(stored().getFeedback().isEmpty());
            model.deleteHomework(stored());
        }
    }

    @Test
    public void execute_assignedHomework_rejected() throws Exception {
        showWith(new HomeworkBuilder(ALICE).build());

        assertEquals("Cannot record feedback on homework that has not been submitted.",
                rejection(new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Too early")));
        assertTrue(stored().getFeedback().isEmpty());
    }

    @Test
    public void execute_overdueButStillAssigned_rejected() throws Exception {
        showWith(new HomeworkBuilder(ALICE).withDueDate(TODAY.minusDays(1)).build());

        assertEquals("Cannot record feedback on homework that has not been submitted.",
                rejection(new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Too early")));
    }

    @Test
    public void execute_invalidOrDeletedIndex_rejected() throws Exception {
        Homework first = new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED).build();
        showWith(first);

        assertEquals("Invalid homework index. Run 'hw list' to display homework first.",
                rejection(new HomeworkFeedbackCommand(INDEX_SECOND_PERSON, "x")));

        model.deleteHomework(first);
        assertEquals("That homework was deleted. Run 'hw list' to refresh.",
                rejection(new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "x")));
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkFeedbackCommand(null, "x"));
        assertThrows(NullPointerException.class, () -> new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkFeedbackCommand command = new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Good");

        assertEquals(command, command);
        assertEquals(command, new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Good"));
        assertEquals(command.hashCode(), new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Good").hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new HomeworkFeedbackCommand(Index.fromOneBased(2), "Good"));
        assertNotEquals(command, new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Better"));
    }

    @Test
    public void toStringMethod() {
        assertEquals(HomeworkFeedbackCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", feedback=Good}", new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Good").toString());
    }
}
