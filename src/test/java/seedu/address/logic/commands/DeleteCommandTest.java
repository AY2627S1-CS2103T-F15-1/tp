package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_studentWithLessonsNotConfirmed_asksForConfirmationAndDeletesNothing() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.addLesson(new LessonBuilder().withStudent(student).withDate(LocalDate.now().plusDays(3)).build());
        model.addLesson(new LessonBuilder().withStudent(student).withDate(LocalDate.now().plusDays(10)).build());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        String expectedMessage = String.format(DeleteCommand.MESSAGE_CONFIRMATION, student.getName(),
                student.getPhone(), 2, String.format(DeleteCommand.MESSAGE_UPCOMING_WARNING, 2),
                INDEX_FIRST_PERSON.getOneBased());

        assertCommandSuccess(new DeleteCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
        assertTrue(model.getFilteredPersonList().contains(student));
        assertEquals(2, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void execute_studentWithOnlyPastLessonsNotConfirmed_asksWithoutUpcomingWarning() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.addLesson(new LessonBuilder().withStudent(student).withDate("2020-01-06").build());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        String expectedMessage = String.format(DeleteCommand.MESSAGE_CONFIRMATION, student.getName(),
                student.getPhone(), 1, "", INDEX_FIRST_PERSON.getOneBased());

        assertCommandSuccess(new DeleteCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithLessonsConfirmed_deletesStudentAndLessons() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Lesson lessonOfStudent = new LessonBuilder().withStudent(student).build();
        model.addLesson(lessonOfStudent);
        Person otherStudent = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Lesson lessonOfOtherStudent = new LessonBuilder().withStudent(otherStudent).build();
        model.addLesson(lessonOfOtherStudent);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(student);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_WITH_LESSONS_SUCCESS,
                Messages.format(student), 1);

        assertCommandSuccess(new DeleteCommand(INDEX_FIRST_PERSON, true), model, expectedMessage, expectedModel);
        // only the deleted student's lessons are removed
        assertEquals(1, model.getAddressBook().getLessonList().size());
        assertTrue(model.getAddressBook().getLessonList().contains(lessonOfOtherStudent));
    }

    @Test
    public void execute_studentWithoutLessonsConfirmed_deletesStudent() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON, true);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));

        // different confirmation -> returns false
        assertFalse(deleteFirstCommand.equals(new DeleteCommand(INDEX_FIRST_PERSON, true)));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName()
                + "{targetIndex=" + targetIndex + ", isConfirmed=false}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
