package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;

public class LessonSeriesCancelCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    @Test
    public void constructorAndEquals_validateInputsAndCompareIndexAndClock() {
        Index first = Index.fromOneBased(1);
        LessonSeriesCancelCommand command = new LessonSeriesCancelCommand(first, CLOCK);
        assertThrows(NullPointerException.class, () -> new LessonSeriesCancelCommand(null, CLOCK));
        assertThrows(NullPointerException.class, () -> new LessonSeriesCancelCommand(first, null));
        assertThrows(NullPointerException.class, () -> command.execute(null));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new LessonSeriesCancelCommand(first, CLOCK)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonSeriesCancelCommand(Index.fromOneBased(2), CLOCK)));
        assertFalse(command.equals(new LessonSeriesCancelCommand(first, Clock.systemUTC())));
        assertEquals(command.hashCode(), new LessonSeriesCancelCommand(first, CLOCK).hashCode());
    }

    @Test
    public void execute_selectPastOccurrence_cancelsOnlyFutureScheduledOccurrencesIncludingMoved() throws Exception {
        ModelManager model = model();
        Lesson past = occurrence(-1, "12:00");
        Lesson startedToday = occurrence(0, "11:00");
        Lesson exactlyNow = occurrence(0, "12:00");
        Lesson moved = occurrence(7, "14:00").reschedule(occurrence(3, "16:00").getDate(),
                occurrence(3, "16:00").getTime(), exactlyNow.getDuration(), new Venue("Cafe"));
        Lesson completed = occurrence(8, "12:00").complete("History");
        Lesson missed = occurrence(9, "12:00").miss();
        Lesson cancelled = occurrence(10, "12:00").cancel("Already cancelled");
        Lesson unrelated = occurrence(11, "12:00").withSeriesId("another-series");
        List<Lesson> originals = List.of(past, startedToday, exactlyNow, moved, completed, missed, cancelled,
                unrelated);
        originals.forEach(model::addLesson);

        String result = new LessonSeriesCancelCommand(Index.fromOneBased(1), CLOCK).execute(model).getFeedbackToUser();
        assertEquals("Cancelled 2 series occurrences.", result);
        for (Lesson original : originals) {
            Lesson actual = model.getAddressBook().getLessonList().stream()
                    .filter(lesson -> lesson.getId().equals(original.getId())).findFirst().orElseThrow();
            assertEquals(original == exactlyNow || original == moved ? original.cancel("Series cancelled") : original,
                    actual);
            assertEquals(original.getSeriesId(), actual.getSeriesId());
        }
    }

    @Test
    public void execute_noFutureOccurrencesOrNoSeries_rejectsWithoutChangingAnything() {
        ModelManager model = model();
        Lesson past = occurrence(-1, "12:00");
        model.addLesson(past);
        CommandException error = assertThrows(CommandException.class, () ->
                new LessonSeriesCancelCommand(Index.fromOneBased(1), CLOCK).execute(model));
        assertEquals(LessonSeriesCancelCommand.MESSAGE_NONE, error.getMessage());
        assertEquals(List.of(past), model.getAddressBook().getLessonList());
        model.deleteLesson(past);
        Lesson single = new LessonBuilder().withStudent(ALICE).withDate(TODAY).build();
        model.addLesson(single);
        error = assertThrows(CommandException.class, () ->
                new LessonSeriesCancelCommand(Index.fromOneBased(1), CLOCK).execute(model));
        assertEquals(LessonSeriesCancelCommand.MESSAGE_NOT_SERIES, error.getMessage());
        assertEquals(List.of(single), model.getAddressBook().getLessonList());
    }

    @Test
    public void execute_nothingChangesWhenBuildingReplacementFails() {
        ModelManager model = new ModelManager() {
            @Override
            public void setAddressBook(ReadOnlyAddressBook data) {
                throw new IllegalStateException("Synthetic model failure");
            }
        };
        model.addPerson(ALICE);
        List<Lesson> originals = List.of(occurrence(1, "12:00"), occurrence(8, "12:00"));
        originals.forEach(model::addLesson);
        assertThrows(IllegalStateException.class, () ->
                new LessonSeriesCancelCommand(Index.fromOneBased(1), CLOCK).execute(model));
        assertEquals(originals, model.getAddressBook().getLessonList());
    }

    @Test
    public void execute_futureOnlyList_refreshesNumbersAndRetainsCancelledHistory() throws Exception {
        ModelManager model = model();
        model.addLesson(occurrence(1, "12:00"));
        model.addLesson(occurrence(8, "12:00"));
        model.updateFilteredLessonList(Lesson::isScheduled);
        String result = new LessonSeriesCancelCommand(Index.fromOneBased(1), CLOCK).execute(model).getFeedbackToUser();
        assertTrue(result.contains("Lesson numbers have changed."));
        assertTrue(result.contains("No lessons in the current list."));
        assertTrue(model.getFilteredLessonList().isEmpty());
        assertTrue(model.getAddressBook().getLessonList().stream()
                .allMatch(lesson -> lesson.getStatus() == LessonStatus.CANCELLED));
    }

    @Test
    public void parse_requiresExactConfirmationAndValidIndex() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertEquals(new LessonSeriesCancelCommand(Index.fromOneBased(2)),
                parser.parseCommand("lesson series cancel 2 confirm"));
        for (String suffix : List.of("cancel 1", "cancel 1 Confirm", "cancel 0 confirm", "cancel -1 confirm",
                "cancel 1 confirm now", "cancel confirm 1", "cancel abc confirm", "1 confirm", "", "end 1 confirm")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("lesson series " + suffix));
        }
    }

    private static ModelManager model() {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        return model;
    }

    private static Lesson occurrence(int days, String time) {
        return new LessonBuilder().withStudent(ALICE).withDate(TODAY.plusDays(days)).withTime(time).build()
                .snapshotStudent(ALICE).withSeriesId("weekly-series");
    }
}
