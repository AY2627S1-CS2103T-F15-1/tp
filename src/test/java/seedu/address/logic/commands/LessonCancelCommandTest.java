package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LessonCancelCommand}.
 */
public class LessonCancelCommandTest {

    private final Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(10))
            .build();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonCancelCommand(null, "Student unwell"));
    }

    @Test
    public void execute_withReason_cancelsLessonAndShowsReason() {
        model.addLesson(lesson);
        LessonCancelCommand command = new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell");

        String expectedMessage = String.format(LessonCancelCommand.MESSAGE_SUCCESS, Messages.format(lesson))
                + String.format(LessonCancelCommand.MESSAGE_REASON, "Student unwell");
        assertCommandSuccess(command, model, expectedMessage, getExpectedModel(lesson.cancel("Student unwell")));
    }

    @Test
    public void execute_withoutReason_cancelsLessonWithoutReasonLine() {
        model.addLesson(lesson);
        LessonCancelCommand command = new LessonCancelCommand(INDEX_FIRST_PERSON, null);

        String expectedMessage = String.format(LessonCancelCommand.MESSAGE_SUCCESS, Messages.format(lesson));
        assertCommandSuccess(command, model, expectedMessage, getExpectedModel(lesson.cancel(null)));
    }

    @Test
    public void execute_cancelledLesson_noLongerOverlapsOtherLessons() throws Exception {
        Lesson other = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
                .withDate(lesson.getDate().value).withTime("17:00").withDuration(60).build();
        model.addLesson(lesson);
        model.addLesson(other);
        assertFalse(model.getConflictingLessons(other).isEmpty());

        new LessonCancelCommand(INDEX_FIRST_PERSON, null).execute(model);

        assertTrue(model.getConflictingLessons(other).isEmpty());
        assertEquals(LessonStatus.CANCELLED, model.getFilteredLessonList().get(0).getStatus());
    }

    @Test
    public void execute_invalidLessonIndex_throwsCommandException() {
        model.addLesson(lesson);
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredLessonList().size() + 1);

        assertCommandFailure(new LessonCancelCommand(outOfBoundIndex, null), model,
                Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyCancelledLesson_throwsCommandException() {
        model.addLesson(lesson.cancel(null));

        assertCommandFailure(new LessonCancelCommand(INDEX_FIRST_PERSON, null), model,
                LessonCancelCommand.MESSAGE_ALREADY_CANCELLED);
    }

    @Test
    public void execute_completedLesson_throwsCommandException() {
        model.addLesson(new LessonBuilder(lesson).withStatus(LessonStatus.COMPLETED).build());

        assertCommandFailure(new LessonCancelCommand(INDEX_FIRST_PERSON, null), model,
                LessonCancelCommand.MESSAGE_COMPLETED_LESSON);
    }

    @Test
    public void equals() {
        LessonCancelCommand command = new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell");

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonCancelCommand(INDEX_SECOND_PERSON, "Student unwell")));
        assertFalse(command.equals(new LessonCancelCommand(INDEX_FIRST_PERSON, null)));
    }

    @Test
    public void toStringMethod() {
        String expected = LessonCancelCommand.class.getCanonicalName() + "{lessonIndex=" + INDEX_FIRST_PERSON
                + ", reason=Student unwell}";
        assertEquals(expected, new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell").toString());
    }

    private Model getExpectedModel(Lesson cancelledLesson) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setLesson(lesson, cancelledLesson);
        return expectedModel;
    }
}
