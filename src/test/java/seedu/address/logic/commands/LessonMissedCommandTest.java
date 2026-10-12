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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

public class LessonMissedCommandTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-12T12:00:00Z"), ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private final Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(TODAY)
            .withVenue("Online").withNotes("Prepared algebra").build();
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonMissedCommand(null));
        assertThrows(NullPointerException.class, () -> new LessonMissedCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command().execute(null));
    }

    @Test
    public void execute_today_marksMissedAndRetainsDetails() {
        assertMarkMissedSuccess(lesson, command());
    }

    @Test
    public void execute_past_marksMissed() {
        assertMarkMissedSuccess(new LessonBuilder(lesson).withDate(TODAY.minusDays(1)).build(), command());
    }

    @Test
    public void execute_noNotes_marksMissed() {
        assertMarkMissedSuccess(new LessonBuilder().withStudent(ALICE).withDate(TODAY).build(), command());
    }

    @Test
    public void execute_futureLesson_rejectsWithoutChangingData() {
        Lesson future = new LessonBuilder(lesson).withDate(TODAY.plusDays(1)).build();
        model.addLesson(future);
        assertCommandFailure(command(), model, String.format(LessonMissedCommand.MESSAGE_FUTURE_LESSON,
                future.getDate()));
    }

    @Test
    public void execute_nearUtcMidnight_usesLocalDate() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T17:00:00Z"), ZoneId.of("Asia/Singapore"));
        assertMarkMissedSuccess(lesson, new LessonMissedCommand(INDEX_FIRST_PERSON, clock));
    }

    @Test
    public void execute_cancelledLesson_rejectsWithoutChangingData() {
        model.addLesson(lesson.cancel("Student unwell"));
        assertCommandFailure(command(), model, LessonMissedCommand.MESSAGE_CANCELLED_LESSON);
    }

    @Test
    public void execute_completedLesson_rejectsWithoutChangingData() {
        model.addLesson(lesson.complete(null));
        assertCommandFailure(command(), model, LessonMissedCommand.MESSAGE_COMPLETED_LESSON);
    }

    @Test
    public void execute_alreadyMissed_keepsOriginalRecord() {
        model.addLesson(lesson.miss());
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(command(), model, LessonMissedCommand.MESSAGE_ALREADY_MISSED, expected);
        assertEquals(1, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void execute_invalidIndex_rejectsWithoutChangingData() {
        assertCommandFailure(command(), model, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        model.addLesson(lesson);
        assertCommandFailure(new LessonMissedCommand(Index.fromOneBased(2), CLOCK), model,
                Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_marksDisplayedLessonOnly() throws Exception {
        Lesson other = new LessonBuilder(lesson).withStudent(BENSON).withTime("15:00").build();
        model.addLesson(other);
        model.addLesson(lesson);
        model.updateFilteredLessonList(value -> value.isWith(ALICE));
        command().execute(model);
        assertEquals(lesson.miss(), model.getFilteredLessonList().get(0));
        assertTrue(model.getAddressBook().getLessonList().contains(other));
        assertEquals(1, model.getFilteredLessonList().size());
    }

    @Test
    public void execute_upcomingFilter_removesMissedLessonFromUpcomingList() throws Exception {
        model.addLesson(lesson);
        model.updateFilteredLessonList(value -> value.isUpcoming(TODAY));
        command().execute(model);
        assertTrue(model.getFilteredLessonList().isEmpty());
        assertEquals(lesson.miss(), model.getAddressBook().getLessonList().get(0));
    }

    @Test
    public void equals() {
        LessonMissedCommand command = command();
        assertTrue(command.equals(command));
        assertTrue(command.equals(command()));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonMissedCommand(INDEX_SECOND_PERSON, CLOCK)));
        assertFalse(command.equals(new LessonMissedCommand(INDEX_FIRST_PERSON, Clock.systemUTC())));
    }

    @Test
    public void toStringMethod() {
        assertEquals(LessonMissedCommand.class.getCanonicalName() + "{lessonIndex=" + INDEX_FIRST_PERSON + "}",
                command().toString());
    }

    private LessonMissedCommand command() {
        return new LessonMissedCommand(INDEX_FIRST_PERSON, CLOCK);
    }

    private void assertMarkMissedSuccess(Lesson original, LessonMissedCommand command) {
        model.addLesson(original);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setLesson(original, original.miss());
        assertCommandSuccess(command, model, String.format(LessonMissedCommand.MESSAGE_SUCCESS,
                Messages.format(original)), expected);
        assertEquals(LessonStatus.SCHEDULED, original.getStatus());
    }
}
