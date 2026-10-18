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
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.finance.Money;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

public class LessonDoneCommandTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final String NOTES = "Covered quadratic roots.";

    private final Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(TODAY).withVenue("Online").build();
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullRequiredArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonDoneCommand(null, NOTES));
        assertThrows(NullPointerException.class, () -> new LessonDoneCommand(INDEX_FIRST_PERSON, NOTES, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(NOTES).execute(null));
    }

    @Test
    public void execute_todayWithNotes_completesAndRetainsBookingDetails() throws Exception {
        model.addLesson(lesson);
        assertChargedSuccess(lesson, NOTES, CLOCK);
    }

    @Test
    public void execute_pastWithoutNotes_completesLesson() throws Exception {
        Lesson past = new LessonBuilder(lesson).withDate(TODAY.minusDays(1)).build();
        model.addLesson(past);
        assertChargedSuccess(past, null, CLOCK);
    }

    @Test
    public void execute_futureLesson_rejectsWithoutChangingData() {
        Lesson future = new LessonBuilder(lesson).withDate(TODAY.plusDays(1)).build();
        model.addLesson(future);
        assertCommandFailure(command(NOTES), model,
                String.format(LessonDoneCommand.MESSAGE_FUTURE_LESSON, future.getDate()));
    }

    @Test
    public void execute_nearUtcMidnight_usesLocalDate() throws Exception {
        Clock singaporeClock = Clock.fixed(Instant.parse("2026-10-07T17:00:00Z"), ZoneId.of("Asia/Singapore"));
        model.addLesson(lesson);
        assertChargedSuccess(lesson, null, singaporeClock);
    }

    @Test
    public void execute_cancelledLesson_rejectsWithoutChangingData() {
        model.addLesson(lesson.cancel("Student unwell"));
        assertCommandFailure(command(NOTES), model, LessonDoneCommand.MESSAGE_CANCELLED_LESSON);
    }

    @Test
    public void execute_completedLessonWithNotes_replacesNotesWithoutDuplicatingLesson() {
        Lesson completed = lesson.complete("Old notes");
        model.addLesson(completed);
        assertCommandSuccess(command(NOTES), model, String.format(LessonDoneCommand.MESSAGE_UPDATED_NOTES,
                Messages.format(completed)), expectedModel(completed, completed.complete(NOTES)));
        assertEquals(1, model.getAddressBook().getLessonList().size());
    }

    @Test
    public void execute_completedLessonWithoutNotes_retainsNotes() {
        model.addLesson(lesson.complete(NOTES));
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(command(null), model, LessonDoneCommand.MESSAGE_ALREADY_COMPLETED, expected);
    }

    @Test
    public void execute_missedLesson_changesStatusToCompleted() throws Exception {
        Lesson missed = new LessonBuilder(lesson).withStatus(LessonStatus.MISSED).build();
        model.addLesson(missed);
        assertChargedSuccess(missed, NOTES, CLOCK);
    }

    @Test
    public void execute_invalidIndex_rejectsWithoutChangingData() {
        assertCommandFailure(command(NOTES), model, Messages.invalidLessonIndex(Index.fromOneBased(1), 0));
        model.addLesson(lesson);
        assertCommandFailure(new LessonDoneCommand(Index.fromOneBased(2), NOTES, CLOCK), model,
                Messages.invalidLessonIndex(Index.fromOneBased(2), 1));
    }

    @Test
    public void execute_filteredList_completesDisplayedLessonOnly() throws Exception {
        Lesson other = new LessonBuilder(lesson).withStudent(BENSON).withTime("15:00").build();
        model.addLesson(lesson);
        model.addLesson(other);
        model.updateFilteredLessonList(value -> value.isWith(ALICE));

        command(NOTES).execute(model);

        assertEquals(lesson.complete(NOTES), model.getFilteredLessonList().get(0));
        assertTrue(model.getAddressBook().getLessonList().contains(other));
        assertEquals(1, model.getFilteredLessonList().size());
    }

    @Test
    public void execute_upcomingFilter_removesCompletedLessonFromUpcomingList() throws Exception {
        model.addLesson(lesson);
        model.updateFilteredLessonList(value -> value.isUpcoming(TODAY));
        command(null).execute(model);
        assertTrue(model.getFilteredLessonList().isEmpty());
        assertEquals(lesson.complete(null), model.getAddressBook().getLessonList().get(0));
    }

    @Test
    public void equals() {
        LessonDoneCommand command = command(NOTES);
        assertTrue(command.equals(command));
        assertTrue(command.equals(command(NOTES)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(command(null)));
        assertFalse(command.equals(new LessonDoneCommand(INDEX_SECOND_PERSON, NOTES, CLOCK)));
        assertFalse(command.equals(new LessonDoneCommand(INDEX_FIRST_PERSON, NOTES, Clock.systemUTC())));
    }

    @Test
    public void toStringMethod() {
        assertEquals(LessonDoneCommand.class.getCanonicalName() + "{lessonIndex=" + INDEX_FIRST_PERSON
                + ", notes=" + NOTES + "}", command(NOTES).toString());
    }

    private LessonDoneCommand command(String notes) {
        return new LessonDoneCommand(INDEX_FIRST_PERSON, notes, CLOCK);
    }

    private void assertChargedSuccess(Lesson original, String notes, Clock clock) throws Exception {
        CommandResult result = new LessonDoneCommand(INDEX_FIRST_PERSON, notes, clock).execute(model);
        assertEquals(original.complete(notes), model.getFilteredLessonList().getFirst());
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
        LedgerEntry charge = model.getAddressBook().getLedgerEntries().getFirst();
        assertEquals(original.getId(), charge.lessonId());
        assertEquals(ALICE.getId(), charge.studentId());
        assertEquals(Money.charge(ALICE.getRate().value, original.getDuration().value), charge.amount());
        assertEquals(String.format(LessonDoneCommand.MESSAGE_SUCCESS, Messages.format(original))
                + "\nCharge of " + Money.format(charge.amount()) + " recorded.", result.getFeedbackToUser());
    }

    private Model expectedModel(Lesson original, Lesson completed) {
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.setLesson(original, completed);
        return expected;
    }
}
