package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.HomeworkBuilder;

public class HomeworkListCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = newModel();

    private static Model newModel() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        return new ModelManager(book, new UserPrefs());
    }

    private Homework add(Homework item) {
        model.addHomework(item);
        return item;
    }

    /** Fills the book with one overdue item, two tied items, a later item and a reviewed item with feedback. */
    private void fillBook() {
        add(new HomeworkBuilder(ALICE).withDescription("Later").withDueDate(TODAY.plusDays(5)).build());
        add(new HomeworkBuilder(BENSON).withDescription("Tie one").withDueDate(TODAY.plusDays(1))
                .withStatus(HomeworkStatus.SUBMITTED).build());
        add(new HomeworkBuilder(ALICE).withDescription("Overdue").withDueDate(TODAY.minusDays(2)).build());
        add(new HomeworkBuilder(ALICE).withDescription("Tie two").withDueDate(TODAY.plusDays(1)).build());
        add(new HomeworkBuilder(ALICE).withDescription("Done").withDueDate(TODAY.minusDays(10))
                .withStatus(HomeworkStatus.REVIEWED).withFeedback("Great work").build());
    }

    private String list(Index student, boolean isOverdueOnly, boolean isOutstandingOnly) throws CommandException {
        return new HomeworkListCommand(student, isOverdueOnly, isOutstandingOnly, TODAY).execute(model)
                .getFeedbackToUser();
    }

    @Test
    public void execute_noFilter_listsTheWholeHistoryOverdueFirstThenByDueDate() throws Exception {
        fillBook();

        assertEquals("5 homework item(s).\n"
                + "1. Alice Pauline Overdue due 2030-01-08 OVERDUE\n"
                + "2. Alice Pauline Done due 2029-12-31 reviewed\n"
                + "    Feedback: Great work\n"
                + "3. Benson Meier Tie one due 2030-01-11 submitted\n"
                + "4. Alice Pauline Tie two due 2030-01-11 assigned\n"
                + "5. Alice Pauline Later due 2030-01-15 assigned", list(null, false, false));
    }

    @Test
    public void execute_overdueFlag_keepsOnlyUnsubmittedWorkPastItsDate() throws Exception {
        fillBook();

        assertEquals("1 homework item(s).\n1. Alice Pauline Overdue due 2030-01-08 OVERDUE",
                list(null, true, false));
    }

    @Test
    public void execute_outstandingFlag_dropsReviewedWork() throws Exception {
        fillBook();

        assertEquals("4 homework item(s).\n"
                + "1. Alice Pauline Overdue due 2030-01-08 OVERDUE\n"
                + "2. Benson Meier Tie one due 2030-01-11 submitted\n"
                + "3. Alice Pauline Tie two due 2030-01-11 assigned\n"
                + "4. Alice Pauline Later due 2030-01-15 assigned", list(null, false, true));
    }

    @Test
    public void execute_studentFilter_keepsOneStudentsHistory() throws Exception {
        fillBook();

        assertEquals("1 homework item(s).\n1. Benson Meier Tie one due 2030-01-11 submitted",
                list(INDEX_SECOND_PERSON, false, false));
        assertEquals("3 homework item(s).\n"
                + "1. Alice Pauline Overdue due 2030-01-08 OVERDUE\n"
                + "2. Alice Pauline Tie two due 2030-01-11 assigned\n"
                + "3. Alice Pauline Later due 2030-01-15 assigned", list(INDEX_FIRST_PERSON, false, true));
    }

    @Test
    public void execute_archivedStudent_isLabelled() throws Exception {
        add(new HomeworkBuilder(BENSON).withDescription("Old").build());
        model.setPerson(BENSON, BENSON.withArchived(true));

        assertEquals("1 homework item(s).\n1. Benson Meier (archived) Old due 2030-01-12 assigned",
                list(null, false, false));
    }

    @Test
    public void execute_nothingRecorded_saysSo() throws Exception {
        assertEquals("No homework recorded yet.", list(null, false, false));
        assertEquals("No homework recorded yet.", list(INDEX_FIRST_PERSON, false, true));
        assertTrue(model.getDisplayedHomework().isEmpty());
    }

    @Test
    public void execute_filtersMatchNothing_saysSo() throws Exception {
        add(new HomeworkBuilder(ALICE).build());

        assertEquals("0 homework item(s).\nNo homework matches those filters.", list(null, true, false));
        assertEquals("0 homework item(s).\nNo homework matches those filters.",
                list(INDEX_SECOND_PERSON, false, false));
        assertTrue(model.getDisplayedHomework().isEmpty());
    }

    @Test
    public void execute_studentIndexOutOfRange_rejected() {
        Index outOfRange = Index.fromOneBased(3);

        HomeworkListCommand command = new HomeworkListCommand(outOfRange, false, false, TODAY);

        assertEquals(Messages.invalidPersonIndex(outOfRange, 2),
                assertThrows(CommandException.class, () -> command.execute(model)).getMessage());
    }

    @Test
    public void execute_listing_numbersTheSelectionUntilTheNextListing() throws Exception {
        fillBook();
        list(null, false, false);
        List<Homework> shown = model.getDisplayedHomework();

        assertEquals(List.of("Overdue", "Done", "Tie one", "Tie two", "Later"),
                shown.stream().map(Homework::getDescription).toList());
        list(null, true, false);
        assertEquals(1, model.getDisplayedHomework().size());
    }

    @Test
    public void execute_listing_doesNotChangeTheData() throws Exception {
        fillBook();
        List<Homework> before = List.copyOf(model.getAddressBook().getHomeworkList());

        list(null, false, false);

        assertEquals(before, List.copyOf(model.getAddressBook().getHomeworkList()));
    }

    @Test
    public void constructor_nullToday_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkListCommand(null, false, false, null));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkListCommand command = new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY);

        assertEquals(command, command);
        assertEquals(command, new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY));
        assertEquals(command.hashCode(), new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY).hashCode());
        assertEquals(new HomeworkListCommand(null, false, false, TODAY),
                new HomeworkListCommand(null, false, false, TODAY));
        assertEquals(new HomeworkListCommand(null, false, false, TODAY).hashCode(),
                new HomeworkListCommand(null, false, false, TODAY).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new HomeworkListCommand(null, true, false, TODAY));
        assertNotEquals(command, new HomeworkListCommand(INDEX_SECOND_PERSON, true, false, TODAY));
        assertNotEquals(command, new HomeworkListCommand(INDEX_FIRST_PERSON, false, true, TODAY));
        assertNotEquals(command, new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY.plusDays(1)));
    }

    @Test
    public void toStringMethod() {
        assertEquals(HomeworkListCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", isOverdueOnly=true, isOutstandingOnly=false, today=" + TODAY + "}",
                new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY).toString());
    }
}
