package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class ViewCommandTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null, TODAY));
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_studentWithoutLessons_showsDetailsAndEmptyStates() throws Exception {
        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals("Viewing: Alice Pauline", lines[0]);
        assertEquals("Level S3 \u00b7 Math \u00b7 $50.00/lesson", lines[1]);
        assertEquals("Phone: 94351253 \u00b7 Email: alice@example.com \u00b7 Venue: 123, Jurong West Ave 6, #08-111",
                lines[2]);
        assertEquals("", lines[3]);
        assertEquals(ViewCommand.HEADING_NEXT_LESSON, lines[4]);
        assertEquals(ViewCommand.MESSAGE_NO_UPCOMING_LESSON, lines[5]);
        assertEquals(ViewCommand.HEADING_RECENT_NOTES, lines[7]);
        assertEquals(ViewCommand.MESSAGE_NO_NOTES, lines[8]);
        assertEquals(ViewCommand.HEADING_LESSON_HISTORY, lines[10]);
        assertEquals("  0 completed \u00b7 0 cancelled \u00b7 0 missed", lines[11]);
        assertEquals(12, lines.length);
    }

    @Test
    public void execute_studentWithSeveralSubjects_listsSubjectsInAlphabeticalOrder() throws Exception {
        String[] lines = view(INDEX_SECOND_PERSON).split("\n");

        assertEquals("Viewing: Benson Meier", lines[0]);
        assertEquals("Level J1 \u00b7 Math, Physics \u00b7 $62.50/lesson", lines[1]);
    }

    @Test
    public void execute_studentWithUpcomingLessons_showsTheEarliestScheduledOne() throws Exception {
        addLessons(
                lesson(TODAY.plusDays(7), "16:30"),
                lesson(TODAY.plusDays(2), "18:00"),
                lesson(TODAY.plusDays(2), "09:00"),
                // a cancelled lesson is not a next lesson, even though it is the earliest
                new LessonBuilder().withStudent(ALICE).withDate(TODAY.plusDays(1)).withTime("10:00")
                        .withStatus(LessonStatus.CANCELLED).build(),
                // a lesson of another student is ignored
                new LessonBuilder().withStudent(BENSON).withDate(TODAY.plusDays(1)).withTime("11:00").build());

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals(ViewCommand.HEADING_NEXT_LESSON, lines[4]);
        String expectedDate = TODAY.plusDays(2).format(DateTimeFormatter.ofPattern("EEE dd MMM uuuu", Locale.ENGLISH));
        assertEquals("  " + expectedDate + ", 09:00-10:30 \u00b7 Math \u00b7 \u2014", lines[5]);
        assertEquals(ViewCommand.HEADING_RECENT_NOTES, lines[7]);
    }

    @Test
    public void execute_lessonToday_countsAsNextLesson() throws Exception {
        addLessons(lesson(TODAY, "16:30"));

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertTrue(lines[5].endsWith("16:30-18:00 \u00b7 Math \u00b7 \u2014"));
    }

    @Test
    public void execute_pastScheduledLesson_isNotANextLesson() throws Exception {
        addLessons(lesson(TODAY.minusDays(1), "16:30"));

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals(ViewCommand.MESSAGE_NO_UPCOMING_LESSON, lines[5]);
    }

    @Test
    public void execute_completedLessonsWithNotes_showsTheThreeMostRecentNewestFirst() throws Exception {
        addLessons(
                completed(TODAY.minusDays(30), "Oldest notes"),
                completed(TODAY.minusDays(21), "Third notes"),
                completed(TODAY.minusDays(14), "Second notes"),
                completed(TODAY.minusDays(7), "Newest notes"),
                // a completed lesson without notes shows nothing
                new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(1))
                        .withStatus(LessonStatus.COMPLETED).build());

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals(ViewCommand.HEADING_RECENT_NOTES, lines[7]);
        assertTrue(lines[8].endsWith("  Newest notes"));
        assertTrue(lines[9].endsWith("  Second notes"));
        assertTrue(lines[10].endsWith("  Third notes"));
        assertEquals("", lines[11]);
        assertEquals(ViewCommand.HEADING_LESSON_HISTORY, lines[12]);
    }

    @Test
    public void execute_lessonsOfEveryStatus_countsThemInTheHistory() throws Exception {
        addLessons(
                completed(TODAY.minusDays(14), "Notes"),
                completed(TODAY.minusDays(7), "More notes"),
                new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(3))
                        .withStatus(LessonStatus.CANCELLED).build(),
                new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(2))
                        .withStatus(LessonStatus.MISSED).build(),
                new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(1))
                        .withStatus(LessonStatus.MISSED).build(),
                lesson(TODAY.plusDays(3), "16:30"));

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals("  2 completed \u00b7 1 cancelled \u00b7 2 missed", lines[lines.length - 1]);
    }

    @Test
    public void execute_studentWithRemark_showsTheRemark() throws Exception {
        model.setPerson(ALICE, new PersonBuilder(ALICE).withRemark("Prefers mornings").build());

        String[] lines = view(INDEX_FIRST_PERSON).split("\n");

        assertEquals("Remark: Prefers mornings", lines[3]);
        assertEquals("", lines[4]);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(outOfBoundIndex, TODAY), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_anyStudent_leavesTheModelUnchanged() throws Exception {
        addLessons(lesson(TODAY.plusDays(1), "16:30"));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        List<Lesson> displayedLessons = List.copyOf(model.getFilteredLessonList());

        view(INDEX_FIRST_PERSON);

        assertEquals(expectedModel, model);
        assertEquals(displayedLessons, model.getFilteredLessonList());
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON, TODAY);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PERSON, TODAY);

        // same object -> returns true
        assertTrue(viewFirstCommand.equals(viewFirstCommand));

        // same values -> returns true
        assertTrue(viewFirstCommand.equals(new ViewCommand(INDEX_FIRST_PERSON, TODAY)));

        // different types -> returns false
        assertFalse(viewFirstCommand.equals(1));

        // null -> returns false
        assertFalse(viewFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(viewFirstCommand.equals(viewSecondCommand));

        // different day -> returns false
        assertFalse(viewFirstCommand.equals(new ViewCommand(INDEX_FIRST_PERSON, TODAY.plusDays(1))));
    }

    @Test
    public void toStringMethod() {
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON, TODAY);
        String expected = ViewCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", today=" + TODAY + "}";
        assertEquals(expected, viewCommand.toString());
    }

    private String view(Index index) throws Exception {
        return new ViewCommand(index, TODAY).execute(model).getFeedbackToUser();
    }

    private static Lesson lesson(LocalDate date, String time) {
        return new LessonBuilder().withStudent(ALICE).withDate(date).withTime(time).build();
    }

    private static Lesson completed(LocalDate date, String notes) {
        return new LessonBuilder().withStudent(ALICE).withDate(date).withStatus(LessonStatus.COMPLETED)
                .withNotes(notes).build();
    }

    private void addLessons(Lesson... lessons) {
        for (Lesson lesson : lessons) {
            model.addLesson(lesson);
        }
    }
}
