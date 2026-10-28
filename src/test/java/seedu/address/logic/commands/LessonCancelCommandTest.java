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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LessonCancelCommand}.
 */
public class LessonCancelCommandTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

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
                Messages.invalidLessonIndex(outOfBoundIndex, model.getFilteredLessonList().size()));
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
    public void execute_chargedCompletedLesson_reportsCompletedNotBilled() {
        Lesson completed = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build().complete(null);
        model.addLesson(completed);
        model.addLedgerEntry(LedgerEntry.charge(ALICE, completed));

        assertCommandFailure(new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell"), model,
                LessonCancelCommand.MESSAGE_COMPLETED_LESSON);
    }

    @Test
    public void execute_chargedMissedLesson_throwsCommandException() {
        Lesson missed = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build().miss();
        model.addLesson(missed);
        model.addLedgerEntry(LedgerEntry.charge(ALICE, missed));

        assertCommandFailure(new LessonCancelCommand(INDEX_FIRST_PERSON, "Student unwell"), model,
                LessonCommandUtil.MESSAGE_CHARGED_LESSON);
    }

    @Test
    public void execute_unchargedMissedLesson_cancelsAndKeepsTheRecordOfTheBooking() throws Exception {
        Lesson missed = missedLesson();
        Model isolatedModel = modelWith(missed);

        new LessonCancelCommand(INDEX_FIRST_PERSON, "Marked missed by mistake").execute(isolatedModel);

        assertCancelledKeepingBooking(missed, "Marked missed by mistake", isolatedModel);
    }

    @Test
    public void execute_missedLessonWithActiveCharge_refusedAndStaysMissed() throws Exception {
        Model isolatedModel = modelWith(missedLesson());
        new ChargeCommand(INDEX_FIRST_PERSON, new BigDecimal("50"), false, CLOCK).execute(isolatedModel);

        assertCommandFailure(new LessonCancelCommand(INDEX_FIRST_PERSON, null), isolatedModel,
                LessonCommandUtil.MESSAGE_CHARGED_LESSON);
        assertEquals(LessonStatus.MISSED, isolatedModel.getFilteredLessonList().get(0).getStatus());
    }

    @Test
    public void execute_missedLessonAfterChargeReversal_cancelsAndKeepsTheLedger() throws Exception {
        Lesson missed = missedLesson();
        Model isolatedModel = modelWith(missed);
        new ChargeCommand(INDEX_FIRST_PERSON, new BigDecimal("50"), false, CLOCK).execute(isolatedModel);
        new ChargeCommand(INDEX_FIRST_PERSON, null, true, CLOCK).execute(isolatedModel);

        new LessonCancelCommand(INDEX_FIRST_PERSON, null).execute(isolatedModel);

        assertCancelledKeepingBooking(missed, null, isolatedModel);
        // The charge and its reversal both stay in the append-only ledger
        assertEquals(2, isolatedModel.getAddressBook().getLedgerEntries().size());
    }

    @Test
    public void execute_anotherLessonIsCharged_stillCancelsUnchargedLesson() throws Exception {
        Model isolatedModel = new ModelManager();
        isolatedModel.addPerson(ALICE);
        Lesson billedLesson = new LessonBuilder().withStudent(ALICE)
                .withDate(LocalDate.now().minusDays(5)).build().complete(null);
        Lesson unchargedLesson = new LessonBuilder().withStudent(ALICE)
                .withDate(LocalDate.now().plusDays(5)).withTime("15:00").build();
        isolatedModel.addLesson(billedLesson);
        isolatedModel.addLesson(unchargedLesson);
        isolatedModel.addLedgerEntry(LedgerEntry.charge(ALICE, billedLesson));
        int targetPosition = isolatedModel.getFilteredLessonList().indexOf(unchargedLesson);

        new LessonCancelCommand(Index.fromZeroBased(targetPosition), null).execute(isolatedModel);

        assertEquals(LessonStatus.CANCELLED, isolatedModel.getFilteredLessonList().get(targetPosition).getStatus());
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

    /** Returns a past lesson of {@code ALICE} that was marked missed after preparation and notes were recorded. */
    private static Lesson missedLesson() {
        return new LessonBuilder().withStudent(ALICE).withDate("2026-10-18").withVenue("Blk 512 Bishan St 13")
                .withNotes("Bring the algebra worksheet").build().prepare("Revise factorising").miss();
    }

    private static Model modelWith(Lesson lesson) {
        Model isolatedModel = new ModelManager();
        isolatedModel.addPerson(ALICE);
        isolatedModel.addLesson(lesson);
        return isolatedModel;
    }

    /**
     * Asserts that the only lesson in {@code model} is {@code missed}, cancelled for {@code reason}, with the same
     * identity, booking details, preparation and notes.
     */
    private static void assertCancelledKeepingBooking(Lesson missed, String reason, Model model) {
        Lesson cancelled = model.getFilteredLessonList().get(0);
        assertEquals(LessonStatus.CANCELLED, cancelled.getStatus());
        assertEquals(Optional.ofNullable(reason), cancelled.getCancelReason());
        assertEquals(missed.getId(), cancelled.getId());
        assertEquals(missed.getStudentId(), cancelled.getStudentId());
        assertEquals(missed.getSubject(), cancelled.getSubject());
        assertEquals(missed.getDate(), cancelled.getDate());
        assertEquals(missed.getTime(), cancelled.getTime());
        assertEquals(missed.getDuration(), cancelled.getDuration());
        assertEquals(missed.getVenue(), cancelled.getVenue());
        assertEquals(Optional.of("Revise factorising"), cancelled.getPreparation());
        assertEquals(Optional.of("Bring the algebra worksheet"), cancelled.getNotes());
    }

    private Model getExpectedModel(Lesson cancelledLesson) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setLesson(lesson, cancelledLesson);
        return expectedModel;
    }
}
