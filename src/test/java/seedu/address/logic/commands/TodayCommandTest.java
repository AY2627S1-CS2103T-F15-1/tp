package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

public class TodayCommandTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final Lesson todayMath = new LessonBuilder().withStudent(ALICE).withDate(TODAY).withTime("16:30")
            .withDuration(90).withVenue("Blk 512 Bishan St 13").build();
    private final Lesson todayPhysics = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
            .withDate(TODAY).withTime("17:00").withDuration(60).build();
    private final Lesson todayCancelled = new LessonBuilder().withStudent(CARL).withSubject("English")
            .withDate(TODAY).withTime("09:00").build().cancel(null);
    private final Lesson yesterdayScheduled = new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(1))
            .build();
    private final Lesson lastWeekScheduled = new LessonBuilder().withStudent(CARL).withSubject("English")
            .withDate(TODAY.minusDays(7)).build();
    private final Lesson yesterdayCompleted = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
            .withDate(TODAY.minusDays(1)).withStatus(LessonStatus.COMPLETED).build();
    private final Lesson tomorrowScheduled = new LessonBuilder().withStudent(ALICE).withDate(TODAY.plusDays(1))
            .build();

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TodayCommand(null));
    }

    @Test
    public void execute_nothingToShow_showsEveryEmptyState() {
        String message = new TodayCommand(TODAY).execute(model).getFeedbackToUser();

        assertEquals("Thursday, 10 January 2030\n\n"
                + "TODAY'S LESSONS (0, 0m)\n" + TodayCommand.MESSAGE_NO_LESSONS_TODAY + "\n\n"
                + "LESSONS AWAITING NOTES (0)\n" + TodayCommand.MESSAGE_ALL_RECORDED
                + "\n\nOVERDUE HOMEWORK (0)\n  No overdue homework."
                + "\n\nDUES\nNo outstanding balances. Everyone is up to date.", message);
    }

    @Test
    public void execute_somethingInEverySection_showsEverySection() {
        Lesson aliceCharged = new LessonBuilder().withStudent(ALICE).withDate(TODAY.minusDays(2)).withTime("10:00")
                .withDuration(30).withStatus(LessonStatus.COMPLETED).build();
        Lesson carlCharged = new LessonBuilder().withStudent(CARL).withSubject("English")
                .withDate(TODAY.minusDays(5)).withTime("10:00").withDuration(60)
                .withStatus(LessonStatus.COMPLETED).build();
        addLessons(todayMath, todayPhysics, yesterdayScheduled, aliceCharged, carlCharged);
        // half an hour at $50.00/hour, and an hour at $35.00/hour
        model.addLedgerEntry(LedgerEntry.charge(ALICE, aliceCharged));
        model.addLedgerEntry(LedgerEntry.charge(CARL, carlCharged));
        // a student in credit owes nothing
        model.addLedgerEntry(LedgerEntry.payment(BENSON, BigDecimal.TEN, TODAY));
        model.addHomework(Homework.assign(CARL.getId(), "Comprehension worksheet", TODAY.minusDays(3)));

        assertEquals("Thursday, 10 January 2030\n\n"
                + "TODAY'S LESSONS (2, 2h 30m)\n"
                + "  16:30-18:00  Alice Pauline      Math       Blk 512 Bishan St 13  \u26a0 overlaps\n"
                + "  17:00-18:00  Benson Meier       Physics    \u2014  \u26a0 overlaps\n\n"
                + "LESSONS AWAITING NOTES (1)\n"
                + "  Wed 9 Jan   Alice Pauline      Math       not yet marked completed\n\n"
                + "OVERDUE HOMEWORK (1)\n"
                + "  Carl Kurz  Comprehension worksheet  due 2030-01-07  OVERDUE\n\n"
                + "DUES\n"
                + "2 student(s) with outstanding balances \u2014 $60.00 total\n"
                + "  Carl Kurz $35.00 oldest unpaid 5 days\n"
                + "  Alice Pauline $25.00 oldest unpaid 2 days",
                new TodayCommand(TODAY).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_chargeFromYesterday_isOneDayOld() {
        Lesson charged = new LessonBuilder(yesterdayScheduled).withStatus(LessonStatus.COMPLETED).build();
        addLessons(charged);
        // an hour and a half at $50.00/hour
        model.addLedgerEntry(LedgerEntry.charge(ALICE, charged));

        assertEquals("DUES\n"
                + "1 student(s) with outstanding balances \u2014 $75.00 total\n"
                + "  Alice Pauline $75.00 oldest unpaid 1 day", section("DUES"));
    }

    @Test
    public void execute_archivedStudentOwesMoney_marksThemArchivedLikeTheirHomework() {
        Lesson charged = new LessonBuilder(lastWeekScheduled).withStatus(LessonStatus.COMPLETED).build();
        addLessons(charged);
        // an hour and a half at $35.00/hour
        model.addLedgerEntry(LedgerEntry.charge(CARL, charged));
        model.addHomework(Homework.assign(CARL.getId(), "Comprehension worksheet", TODAY.minusDays(3)));
        model.setPerson(CARL, CARL.withArchived(true));

        assertEquals("OVERDUE HOMEWORK (1)\n"
                + "  Carl Kurz (archived)  Comprehension worksheet  due 2030-01-07  OVERDUE",
                section("OVERDUE HOMEWORK"));
        assertEquals("DUES\n"
                + "1 student(s) with outstanding balances \u2014 $52.50 total\n"
                + "  Carl Kurz (archived) $52.50 oldest unpaid 7 days", section("DUES"));
    }

    @Test
    public void execute_lessonsToday_showsThemInOrderWithOverlapMarked() {
        addLessons(todayPhysics, tomorrowScheduled, todayMath, todayCancelled);

        String[] lines = new TodayCommand(TODAY).execute(model).getFeedbackToUser().split("\n");

        assertEquals("TODAY'S LESSONS (2, 2h 30m)", lines[2]);
        assertTrue(lines[3].startsWith("  16:30-18:00  Alice Pauline"));
        assertTrue(lines[3].contains("Blk 512 Bishan St 13"));
        assertTrue(lines[3].endsWith(AgendaCommand.MESSAGE_OVERLAPS));
        assertTrue(lines[4].startsWith("  17:00-18:00  Benson Meier"));
        assertTrue(lines[4].endsWith(AgendaCommand.MESSAGE_OVERLAPS));
        assertEquals("", lines[5]);
        assertEquals("LESSONS AWAITING NOTES (0)", lines[6]);
    }

    @Test
    public void execute_completedLessonToday_showsItsStatus() {
        addLessons(new LessonBuilder(todayMath).withStatus(LessonStatus.COMPLETED).build());

        String[] lines = new TodayCommand(TODAY).execute(model).getFeedbackToUser().split("\n");

        assertEquals("TODAY'S LESSONS (1, 1h 30m)", lines[2]);
        assertTrue(lines[3].endsWith("Blk 512 Bishan St 13  (completed)"));
    }

    @Test
    public void execute_pastScheduledLessons_showsThemMostRecentFirst() {
        addLessons(lastWeekScheduled, yesterdayScheduled, yesterdayCompleted, tomorrowScheduled);

        String[] lines = new TodayCommand(TODAY).execute(model).getFeedbackToUser().split("\n");

        assertEquals(TodayCommand.MESSAGE_NO_LESSONS_TODAY, lines[3]);
        assertEquals("LESSONS AWAITING NOTES (2)", lines[5]);
        assertTrue(lines[6].startsWith("  Wed 9 Jan   Alice Pauline"));
        assertTrue(lines[6].endsWith(TodayCommand.MESSAGE_NOT_COMPLETED));
        assertTrue(lines[7].startsWith("  Thu 3 Jan   Carl Kurz"));
        assertEquals(14, lines.length);
    }

    @Test
    public void execute_lessons_displayedLessonListUnchanged() {
        addLessons(todayMath, yesterdayScheduled, tomorrowScheduled);
        List<Lesson> displayedLessons = List.copyOf(model.getFilteredLessonList());

        new TodayCommand(TODAY).execute(model);

        assertEquals(displayedLessons, model.getFilteredLessonList());
    }

    @Test
    public void execute_studentsWhoOweMoney_leavesTheModelUnchanged() {
        Lesson charged = new LessonBuilder(yesterdayScheduled).withStatus(LessonStatus.COMPLETED).build();
        addLessons(charged);
        model.addLedgerEntry(LedgerEntry.charge(ALICE, charged));
        model.addLedgerEntry(LedgerEntry.payment(ALICE, BigDecimal.TEN, TODAY));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        new TodayCommand(TODAY).execute(model);

        assertEquals(expectedModel, model);
    }

    @Test
    public void equals() {
        TodayCommand command = new TodayCommand(TODAY);

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new TodayCommand(TODAY)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new TodayCommand(TODAY.plusDays(1))));
    }

    @Test
    public void toStringMethod() {
        String expected = TodayCommand.class.getCanonicalName() + "{today=" + TODAY + "}";
        assertEquals(expected, new TodayCommand(TODAY).toString());
    }

    /** Returns the section of the dashboard of {@code TODAY} that starts with {@code heading}. */
    private String section(String heading) {
        return Stream.of(new TodayCommand(TODAY).execute(model).getFeedbackToUser().split("\n\n"))
                .filter(block -> block.startsWith(heading))
                .findFirst().orElseThrow();
    }

    private void addLessons(Lesson... lessons) {
        for (Lesson lesson : lessons) {
            model.addLesson(lesson);
        }
    }
}
