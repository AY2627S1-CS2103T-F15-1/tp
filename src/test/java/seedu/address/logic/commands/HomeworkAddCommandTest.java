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
import seedu.address.testutil.LessonBuilder;

public class HomeworkAddCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Model model = newModel();

    private static Model newModel() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        return new ModelManager(book, new UserPrefs());
    }

    private static HomeworkAddCommand add(Index student, String description, LocalDate due) {
        return new HomeworkAddCommand(student, description, due, TODAY);
    }

    private String rejection(HomeworkAddCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    @Test
    public void execute_noUpcomingLesson_assignsAndAddsTheNote() throws Exception {
        String message = add(INDEX_FIRST_PERSON, "Algebra Q1-10", TODAY.plusDays(2)).execute(model)
                .getFeedbackToUser();

        assertEquals("Homework assigned to Alice Pauline: Algebra Q1-10, due 2030-01-12\n"
                + "Run 'hw list' to display the new assignment's index.\n"
                + "Note: Alice Pauline has no upcoming lessons. The homework is still recorded.", message);
        Homework stored = model.getAddressBook().getHomeworkList().getFirst();
        assertEquals(ALICE.getId(), stored.getStudentId());
        assertEquals("Algebra Q1-10", stored.getDescription());
        assertEquals(TODAY.plusDays(2), stored.getDueDate());
        assertEquals(HomeworkStatus.ASSIGNED, stored.getStatus());
        assertTrue(stored.getFeedback().isEmpty());
    }

    @Test
    public void execute_upcomingLesson_leavesOutTheNote() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY).build());

        assertEquals("Homework assigned to Alice Pauline: Algebra Q1-10, due 2030-01-12\n"
                + "Run 'hw list' to display the new assignment's index.",
                add(INDEX_FIRST_PERSON, "Algebra Q1-10", TODAY.plusDays(2)).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_onlyPastOrOtherStudentsLessons_addsTheNote() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(1)).build());
        model.addLesson(new LessonBuilder().withStudent(BENSON).withDate(TODAY.plusDays(1)).build());

        String message = add(INDEX_FIRST_PERSON, "Algebra", TODAY).execute(model).getFeedbackToUser();

        assertTrue(message.endsWith("Note: Alice Pauline has no upcoming lessons. The homework is still recorded."));
    }

    @Test
    public void execute_dueDateBoundaries_acceptedOrRejected() throws Exception {
        add(INDEX_FIRST_PERSON, "Today", TODAY).execute(model);
        add(INDEX_FIRST_PERSON, "A year ahead", TODAY.plusYears(1)).execute(model);

        assertEquals("Due dates must be today or later.",
                rejection(add(INDEX_FIRST_PERSON, "Yesterday", TODAY.minusDays(1))));
        assertEquals("That due date is more than a year away. Check the year.",
                rejection(add(INDEX_FIRST_PERSON, "Too far", TODAY.plusYears(1).plusDays(1))));
        assertEquals(2, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void execute_archivedStudent_rejected() {
        model.setPerson(ALICE, ALICE.withArchived(true));
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertEquals("Restore this student before assigning new homework.",
                rejection(add(INDEX_FIRST_PERSON, "Algebra", TODAY)));
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
    }

    @Test
    public void execute_studentIndexOutOfRange_rejectedWithTheStudentMessage() {
        Index outOfRange = Index.fromOneBased(3);

        assertEquals(Messages.invalidPersonIndex(outOfRange, 2), rejection(add(outOfRange, "Algebra", TODAY)));
    }

    @Test
    public void execute_duplicateOfAnyStatus_rejectedAndNothingAdded() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).withDescription("Algebra Q1-10").withDueDate(TODAY.plusDays(2))
                .withStatus(HomeworkStatus.REVIEWED).withFeedback("Done").build());

        String expected = "This student already has homework 'Algebra Q1-10' due 2030-01-12.";
        assertEquals(expected, rejection(add(INDEX_FIRST_PERSON, "Algebra Q1-10", TODAY.plusDays(2))));
        assertEquals(expected, rejection(add(INDEX_FIRST_PERSON, "ALGEBRA q1-10", TODAY.plusDays(2))));
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void execute_sameTextForAnotherStudentOrAnotherDay_accepted() throws Exception {
        model.addHomework(new HomeworkBuilder(ALICE).withDueDate(TODAY.plusDays(2)).build());

        add(INDEX_SECOND_PERSON, "Algebra Q1-10", TODAY.plusDays(2)).execute(model);
        add(INDEX_FIRST_PERSON, "Algebra Q1-10", TODAY.plusDays(3)).execute(model);
        add(INDEX_FIRST_PERSON, "Algebra Q11-20", TODAY.plusDays(2)).execute(model);

        assertEquals(4, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void execute_afterAListing_doesNotRenumberTheDisplayedHomework() throws Exception {
        Homework existing = new HomeworkBuilder(ALICE).withDescription("First").build();
        model.addHomework(existing);
        new HomeworkListCommand(null, false, false, TODAY).execute(model);

        add(INDEX_FIRST_PERSON, "Second", TODAY.plusDays(1)).execute(model);

        assertEquals(List.of(existing), model.getDisplayedHomework());
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(null, "A", TODAY, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(INDEX_FIRST_PERSON, null, TODAY, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(INDEX_FIRST_PERSON, "A", null, TODAY));
        assertThrows(NullPointerException.class, () -> new HomeworkAddCommand(INDEX_FIRST_PERSON, "A", TODAY, null));
    }

    @Test
    public void equalsAndHashCode() {
        HomeworkAddCommand command = add(INDEX_FIRST_PERSON, "Algebra", TODAY);

        assertEquals(command, command);
        assertEquals(command, add(INDEX_FIRST_PERSON, "Algebra", TODAY));
        assertEquals(command.hashCode(), add(INDEX_FIRST_PERSON, "Algebra", TODAY).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, add(INDEX_SECOND_PERSON, "Algebra", TODAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, "Geometry", TODAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, "Algebra", TODAY.plusDays(1)));
        assertNotEquals(command, new HomeworkAddCommand(INDEX_FIRST_PERSON, "Algebra", TODAY, TODAY.plusDays(1)));
    }

    @Test
    public void toStringMethod() {
        HomeworkAddCommand command = add(INDEX_FIRST_PERSON, "Algebra", TODAY);

        assertEquals(HomeworkAddCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", description=Algebra, dueDate=" + TODAY + ", today=" + TODAY + "}", command.toString());
    }
}
