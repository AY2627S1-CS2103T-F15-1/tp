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
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LessonMoveCommand}.
 */
public class LessonMoveCommandTest {

    private static final LocalDate LESSON_DATE = LocalDate.now().plusDays(10);
    private static final LessonDate NEW_DATE = new LessonDate(LESSON_DATE.plusDays(1).toString());

    private final Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LESSON_DATE)
            .withVenue("Blk 512").build();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonMoveCommand(null, NEW_DATE, null, null, null));
    }

    @Test
    public void execute_dateGiven_movesLessonAndRetainsOtherDetails() {
        model.addLesson(lesson);
        Lesson moved = lesson.reschedule(NEW_DATE, lesson.getTime(), lesson.getDuration(),
                lesson.getVenue().get());
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null);

        assertCommandSuccess(command, model, getSuccessMessage(moved), getExpectedModel(moved));
    }

    @Test
    public void execute_allFieldsGiven_movesLesson() {
        model.addLesson(lesson);
        Venue venue = new Venue("Online");
        Lesson moved = lesson.reschedule(NEW_DATE, new LessonTime("09:00"), new LessonDuration(45), venue);
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, new LessonTime("09:00"),
                new LessonDuration(45), venue);

        assertCommandSuccess(command, model, getSuccessMessage(moved), getExpectedModel(moved));
    }

    @Test
    public void execute_timeOnly_keepsDateEvenIfInPast() {
        Lesson pastLesson = new LessonBuilder(lesson).withDate(LocalDate.now().minusDays(3)).build();
        model.addLesson(pastLesson);
        Lesson moved = pastLesson.reschedule(pastLesson.getDate(), new LessonTime("19:00"),
                pastLesson.getDuration(), pastLesson.getVenue().get());
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, null, new LessonTime("19:00"), null,
                null);

        assertCommandSuccess(command, model, getSuccessMessage(pastLesson, moved), getExpectedModel(moved));
    }

    @Test
    public void execute_newSlotOverlapsAnotherLesson_movesLessonWithWarning() {
        Lesson other = new LessonBuilder().withStudent(BENSON).withSubject("Physics").withDate(NEW_DATE.value)
                .withTime("17:00").withDuration(60).build();
        model.addLesson(lesson);
        model.addLesson(other);
        Lesson moved = lesson.reschedule(NEW_DATE, lesson.getTime(), lesson.getDuration(), lesson.getVenue().get());
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null);

        String expectedMessage = getSuccessMessage(moved) + String.format(
                LessonCommandUtil.MESSAGE_OVERLAP_WARNING, Messages.format(other) + ".");
        assertCommandSuccess(command, model, expectedMessage, getExpectedModel(moved));
    }

    @Test
    public void execute_onlyOverlapsItsOldSlot_movesWithoutWarning() {
        model.addLesson(lesson);
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, null, new LessonTime("17:00"), null,
                null);

        assertFalse(executeSuccessfully(command).contains("Warning"));
    }

    @Test
    public void execute_invalidLessonIndex_throwsCommandException() {
        model.addLesson(lesson);
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredLessonList().size() + 1);
        LessonMoveCommand command = new LessonMoveCommand(outOfBoundIndex, NEW_DATE, null, null, null);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_completedLesson_throwsCommandException() {
        model.addLesson(new LessonBuilder(lesson).withStatus(LessonStatus.COMPLETED).build());

        assertCommandFailure(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null), model,
                LessonMoveCommand.MESSAGE_COMPLETED_LESSON);
    }

    @Test
    public void execute_cancelledLesson_throwsCommandException() {
        model.addLesson(lesson.cancel(null));

        assertCommandFailure(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null), model,
                LessonMoveCommand.MESSAGE_CANCELLED_LESSON);
    }

    @Test
    public void execute_newDateInPast_throwsCommandException() {
        model.addLesson(lesson);
        LessonDate yesterday = new LessonDate(LocalDate.now().minusDays(1).toString());

        assertCommandFailure(new LessonMoveCommand(INDEX_FIRST_PERSON, yesterday, null, null, null), model,
                LessonMoveCommand.MESSAGE_DATE_IN_PAST);
    }

    @Test
    public void execute_newDateMoreThanTwoYearsAhead_throwsCommandException() {
        model.addLesson(lesson);
        LessonDate farAway = new LessonDate(LocalDate.now().plusYears(2).plusDays(1).toString());

        assertCommandFailure(new LessonMoveCommand(INDEX_FIRST_PERSON, farAway, null, null, null), model,
                LessonCommandUtil.MESSAGE_DATE_TOO_FAR);
    }

    @Test
    public void execute_newSlotRunsPastMidnight_throwsCommandException() {
        model.addLesson(lesson);
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, null, new LessonTime("23:00"),
                new LessonDuration(120), null);

        assertCommandFailure(command, model, Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_moveMakesLessonDuplicate_throwsCommandException() {
        Lesson duplicateTarget = new LessonBuilder(lesson).withDate(NEW_DATE.value).build();
        model.addLesson(lesson);
        model.addLesson(duplicateTarget);

        assertCommandFailure(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null), model,
                LessonCommandUtil.MESSAGE_DUPLICATE_LESSON);
    }

    @Test
    public void equals() {
        LessonMoveCommand command = new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null);

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonMoveCommand(INDEX_SECOND_PERSON, NEW_DATE, null, null, null)));
        assertFalse(command.equals(new LessonMoveCommand(INDEX_FIRST_PERSON, null, null, null, null)));
        assertFalse(command.equals(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, new LessonTime("09:00"),
                null, null)));
        assertFalse(command.equals(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null,
                new LessonDuration(60), null)));
        assertFalse(command.equals(new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null,
                new Venue("Online"))));
    }

    @Test
    public void toStringMethod() {
        String expected = LessonMoveCommand.class.getCanonicalName() + "{lessonIndex=" + INDEX_FIRST_PERSON
                + ", date=" + NEW_DATE + ", time=null, duration=null, venue=null}";
        assertEquals(expected, new LessonMoveCommand(INDEX_FIRST_PERSON, NEW_DATE, null, null, null).toString());
    }

    private String executeSuccessfully(LessonMoveCommand command) {
        try {
            return command.execute(model).getFeedbackToUser();
        } catch (Exception e) {
            throw new AssertionError("The command should have succeeded", e);
        }
    }

    private String getSuccessMessage(Lesson moved) {
        return getSuccessMessage(lesson, moved);
    }

    private String getSuccessMessage(Lesson original, Lesson moved) {
        return String.format(LessonMoveCommand.MESSAGE_SUCCESS, original.getStudentName(), original.getSubject(),
                Messages.formatSlot(original), Messages.formatSlot(moved));
    }

    private Model getExpectedModel(Lesson moved) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setLesson(model.getFilteredLessonList().get(0), moved);
        return expectedModel;
    }
}
