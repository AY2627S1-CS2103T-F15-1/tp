package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;

public class AgendaCommandTest {

    private static final LocalDate MONDAY = LocalDate.of(2030, 1, 7);

    private final Lesson mondayMath = new LessonBuilder().withStudent(ALICE).withDate(MONDAY).withTime("16:30")
            .withDuration(90).build();
    private final Lesson mondayPhysics = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
            .withDate(MONDAY).withTime("17:00").withDuration(60).build();
    private final Lesson tuesdayEnglish = new LessonBuilder().withStudent(CARL).withSubject("English")
            .withDate(MONDAY.plusDays(1)).withTime("19:00").withDuration(60).build();
    private final Lesson cancelledWednesday = new LessonBuilder().withStudent(ALICE).withDate(MONDAY.plusDays(2))
            .build().cancel(null);
    private final Lesson nextMonday = new LessonBuilder().withStudent(ALICE).withDate(MONDAY.plusDays(7)).build();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AgendaCommand(null, false));
    }

    @Test
    public void execute_dayWithLessons_showsThemInOrderWithOverlapMarked() {
        addLessons(tuesdayEnglish, mondayPhysics, mondayMath);

        String message = new AgendaCommand(MONDAY, false).execute(model).getFeedbackToUser();

        String[] lines = message.split("\n");
        assertEquals("Agenda — Mon 07 Jan 2030 (2 lessons, 2h 30m)", lines[0]);
        assertTrue(lines[1].startsWith(" 1. 16:30-18:00  Alice Pauline"));
        assertTrue(lines[1].endsWith(AgendaCommand.MESSAGE_OVERLAPS));
        assertTrue(lines[2].startsWith(" 2. 17:00-18:00  Benson Meier"));
        assertTrue(lines[2].endsWith(AgendaCommand.MESSAGE_OVERLAPS));
        assertEquals(3, lines.length);
        assertEquals(List.of(mondayMath, mondayPhysics), model.getFilteredLessonList());
    }

    @Test
    public void execute_dayWithoutLessons_reportsNoLessons() {
        addLessons(mondayMath);

        String message = new AgendaCommand(MONDAY.plusDays(1), false).execute(model).getFeedbackToUser();

        assertEquals(String.format(AgendaCommand.MESSAGE_NO_LESSONS_ON_DAY, "Tue 08 Jan 2030"), message);
        assertTrue(model.getFilteredLessonList().isEmpty());
    }

    @Test
    public void execute_cancelledLesson_isShownButNotCounted() {
        addLessons(cancelledWednesday);

        String message = new AgendaCommand(MONDAY.plusDays(2), false).execute(model).getFeedbackToUser();

        assertTrue(message.startsWith("Agenda — Wed 09 Jan 2030 (0 lessons, 0m)"));
        assertTrue(message.endsWith("(cancelled)"));
    }

    @Test
    public void execute_week_showsSevenDaysOfTheWeekContainingTheDate() {
        addLessons(mondayMath, tuesdayEnglish, cancelledWednesday, nextMonday);

        String message = new AgendaCommand(MONDAY.plusDays(3), true).execute(model).getFeedbackToUser();

        String[] lines = message.split("\n");
        assertEquals("Week of Mon 07 Jan 2030 to Sun 13 Jan 2030 (2 lessons, 2h 30m)", lines[0]);
        assertEquals("Mon 07 Jan 2030 (1 lesson, 1h 30m)", lines[1]);
        assertTrue(lines[2].startsWith(" 1. 16:30-18:00"));
        assertEquals("Tue 08 Jan 2030 (1 lesson, 1h)", lines[3]);
        assertTrue(lines[4].startsWith(" 2. 19:00-20:00"));
        assertTrue(lines[6].startsWith(" 3. "));
        assertEquals("Thu 10 Jan 2030 (0 lessons, 0m)", lines[7]);
        assertEquals(AgendaCommand.MESSAGE_EMPTY_DAY, lines[8]);
        assertEquals(15, lines.length);
        assertEquals(List.of(mondayMath, tuesdayEnglish, cancelledWednesday), model.getFilteredLessonList());
    }

    @Test
    public void equals() {
        AgendaCommand command = new AgendaCommand(MONDAY, false);

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new AgendaCommand(MONDAY, false)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new AgendaCommand(MONDAY.plusDays(1), false)));
        assertFalse(command.equals(new AgendaCommand(MONDAY, true)));
    }

    @Test
    public void toStringMethod() {
        String expected = AgendaCommand.class.getCanonicalName() + "{date=" + MONDAY + ", isWeek=true}";
        assertEquals(expected, new AgendaCommand(MONDAY, true).toString());
    }

    private void addLessons(Lesson... lessons) {
        for (Lesson lesson : lessons) {
            model.addLesson(lesson);
        }
    }
}
