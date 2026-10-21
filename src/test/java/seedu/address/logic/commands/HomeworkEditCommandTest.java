package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

public class HomeworkEditCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(),
            new UserPrefs());

    private static HomeworkEditCommand edit(Index index, String description, LocalDate due) {
        return new HomeworkEditCommand(index, description, due, TODAY);
    }

    private void show() throws CommandException {
        new HomeworkListCommand(null, false, false, TODAY).execute(model);
    }

    private String rejection(HomeworkEditCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    @Test
    public void execute_changedDueDate_reportsTheEditedLine() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).build());
        show();

        String message = edit(INDEX_FIRST_PERSON, null, TODAY.plusDays(3)).execute(model).getFeedbackToUser();

        assertEquals("Edited homework 1: Alice Pauline Algebra Q1-10 due 2030-01-13 assigned", message);
    }

    @Test
    public void execute_editedItem_keepsIdentityStatusAndFeedback() throws Exception {
        Homework original = new HomeworkBuilder(ALICE).withStatus(HomeworkStatus.SUBMITTED)
                .withFeedback("Check signs").build();
        model.addHomework(original);
        show();

        String message = edit(INDEX_FIRST_PERSON, "Geometry", null).execute(model).getFeedbackToUser();

        assertEquals("Edited homework 1: Alice Pauline Geometry due 2030-01-12 submitted", message);
        Homework edited = model.getAddressBook().getHomeworkList().getFirst();
        assertEquals(original.getId(), edited.getId());
        assertEquals(HomeworkStatus.SUBMITTED, edited.getStatus());
        assertEquals("Check signs", edited.getFeedback().orElseThrow());
    }

    @Test
    public void execute_overdueItem_isShownAsOverdue() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).withDueDate(TODAY.minusDays(2)).build());
        show();

        assertEquals("Edited homework 1: Alice Pauline Algebra due 2030-01-08 OVERDUE",
                edit(INDEX_FIRST_PERSON, "Algebra", null).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_unchangedEarlierDueDate_isKept() throws Exception {
        LocalDate old = TODAY.minusDays(30);
        model.addHomework(new HomeworkBuilder(ALICE).withDueDate(old).withStatus(HomeworkStatus.REVIEWED).build());
        show();

        edit(INDEX_FIRST_PERSON, "Corrected", old).execute(model);

        assertEquals(old, model.getAddressBook().getHomeworkList().getFirst().getDueDate());
    }

    @Test
    public void execute_changedDueDateOutOfBounds_rejected() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).build());
        show();

        assertEquals("Due dates must be today or later.",
                rejection(edit(INDEX_FIRST_PERSON, null, TODAY.minusDays(1))));
        assertEquals("That due date is more than a year away. Check the year.",
                rejection(edit(INDEX_FIRST_PERSON, null, TODAY.plusYears(1).plusDays(1))));
        assertEquals(HomeworkBuilder.DEFAULT_DUE_DATE, model.getAddressBook().getHomeworkList().getFirst()
                .getDueDate());
    }

    @Test
    public void execute_editWouldDuplicateAnotherItem_rejected() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).withDescription("First").build());
        model.addHomework(new HomeworkBuilder(ALICE).withDescription("Second").build());
        show();

        assertEquals("This student already has homework 'First' due 2030-01-12.",
                rejection(edit(INDEX_SECOND_PERSON, "FIRST", null)));
        edit(INDEX_SECOND_PERSON, "SECOND", null).execute(model);
        assertEquals(List.of("First", "SECOND"), model.getAddressBook().getHomeworkList().stream()
                .map(Homework::getDescription).toList());
    }

    @Test
    public void execute_indexOutsideTheLastListing_rejected() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).build());

        String expected = "Invalid homework index. Run 'hw list' to display homework first.";
        assertEquals(expected, rejection(edit(INDEX_FIRST_PERSON, "A", null)));
        show();
        assertEquals(expected, rejection(edit(INDEX_SECOND_PERSON, "A", null)));
    }

    @Test
    public void execute_itemDeletedSinceTheListing_neverSelectsTheNextItem() throws Exception {
        Homework first = new HomeworkBuilder(ALICE).withDescription("First").build();
        Homework second = new HomeworkBuilder(ALICE).withDescription("Second").build();
        model.addHomework(first);
        model.addHomework(second);
        show();
        model.deleteHomework(first);

        assertEquals("That homework was deleted. Run 'hw list' to refresh.",
                rejection(edit(INDEX_FIRST_PERSON, "Changed", null)));
        assertEquals("Second", model.getAddressBook().getHomeworkList().getFirst().getDescription());
        edit(INDEX_SECOND_PERSON, "Changed", null).execute(model);
        assertEquals("Changed", model.getAddressBook().getHomeworkList().getFirst().getDescription());
    }

    @Test
    public void execute_afterEditing_keepsTheDisplayedSelection() throws Exception {
        Homework original = new HomeworkBuilder(ALICE).build();
        model.addHomework(original);
        show();

        edit(INDEX_FIRST_PERSON, "Geometry", null).execute(model);

        assertEquals(List.of(original), model.getDisplayedHomework());
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(null, "A", null, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkEditCommand(INDEX_FIRST_PERSON, "A", null, null));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkEditCommand command = edit(INDEX_FIRST_PERSON, "Algebra", TODAY);

        assertEquals(command, command);
        assertEquals(command, edit(INDEX_FIRST_PERSON, "Algebra", TODAY));
        assertEquals(command.hashCode(), edit(INDEX_FIRST_PERSON, "Algebra", TODAY).hashCode());
        assertEquals(edit(INDEX_FIRST_PERSON, null, TODAY), edit(INDEX_FIRST_PERSON, null, TODAY));
        assertEquals(edit(INDEX_FIRST_PERSON, null, TODAY).hashCode(),
                edit(INDEX_FIRST_PERSON, null, TODAY).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, edit(INDEX_SECOND_PERSON, "Algebra", TODAY));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, "Geometry", TODAY));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, null, TODAY));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, "Algebra", TODAY.plusDays(1)));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, "Algebra", null));
        assertNotEquals(command, new HomeworkEditCommand(INDEX_FIRST_PERSON, "Algebra", TODAY, TODAY.plusDays(1)));
    }

    @Test
    public void toStringMethod() {
        assertEquals(HomeworkEditCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", description=Algebra, dueDate=" + TODAY + ", today=" + TODAY + "}",
                edit(INDEX_FIRST_PERSON, "Algebra", TODAY).toString());
    }
}
