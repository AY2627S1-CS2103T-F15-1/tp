package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;

public class LessonListCommandTest {

    private final Lesson upcoming = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(7))
            .build();
    private final Lesson past = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().minusDays(7))
            .build();
    private final Lesson cancelled = new LessonBuilder().withStudent(ALICE).withSubject("Science")
            .withDate(LocalDate.now().plusDays(8)).build().cancel("Student unwell");

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonListCommand(null, false));
    }

    @Test
    public void execute_upcomingOnly_listsOnlyUpcomingLessons() throws Exception {
        addLessons(past, upcoming, cancelled);

        String message = new LessonListCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals(List.of(upcoming), model.getFilteredLessonList());
        assertEquals(String.format(LessonListCommand.MESSAGE_HEADER_UPCOMING, ALICE.getName(), 1) + "\n1. "
                + upcoming.getDate() + "  " + upcoming.getTimeRange() + "  Math  "
                + Messages.formatVenue(upcoming) + "  scheduled", message);
    }

    @Test
    public void execute_all_listsPastAndCancelledLessonsInDateOrder() throws Exception {
        addLessons(upcoming, cancelled, past);

        String message = new LessonListCommand(INDEX_FIRST_PERSON, true).execute(model).getFeedbackToUser();

        assertEquals(List.of(past, upcoming, cancelled), model.getFilteredLessonList());
        assertTrue(message.startsWith(String.format(LessonListCommand.MESSAGE_HEADER_ALL, ALICE.getName(), 3)));
        assertTrue(message.contains("\n3. " + cancelled.getDate()));
        assertTrue(message.endsWith("cancelled"));
    }

    @Test
    public void execute_studentWithoutLessons_reportsNoLessons() throws Exception {
        addLessons(upcoming);

        String message = new LessonListCommand(INDEX_SECOND_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals(String.format(LessonListCommand.MESSAGE_NO_LESSONS, "Benson Meier"), message);
        assertTrue(model.getFilteredLessonList().isEmpty());
    }

    @Test
    public void execute_onlyPastLessons_suggestsAll() throws Exception {
        addLessons(past);

        String message = new LessonListCommand(INDEX_FIRST_PERSON, false).execute(model).getFeedbackToUser();

        assertEquals(String.format(LessonListCommand.MESSAGE_NO_UPCOMING_LESSONS, ALICE.getName()), message);
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new LessonListCommand(outOfBoundIndex, false), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        LessonListCommand command = new LessonListCommand(INDEX_FIRST_PERSON, false);

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new LessonListCommand(INDEX_FIRST_PERSON, false)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonListCommand(INDEX_SECOND_PERSON, false)));
        assertFalse(command.equals(new LessonListCommand(INDEX_FIRST_PERSON, true)));
    }

    @Test
    public void toStringMethod() {
        String expected = LessonListCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", isShowingAll=true}";
        assertEquals(expected, new LessonListCommand(INDEX_FIRST_PERSON, true).toString());
    }

    private void addLessons(Lesson... lessons) {
        for (Lesson lesson : lessons) {
            model.addLesson(lesson);
        }
    }
}
