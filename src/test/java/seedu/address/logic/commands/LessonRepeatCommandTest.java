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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.testutil.LessonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LessonRepeatCommand}.
 */
public class LessonRepeatCommandTest {

    private static final LocalDate START = LocalDate.now().plusDays(10);
    private static final LessonDate START_DATE = new LessonDate(START.toString());
    private static final LessonTime TIME = new LessonTime("16:30");
    private static final LessonDuration DURATION = new LessonDuration(90);
    private static final Subject MATH = new Subject("Math");

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonRepeatCommand(null, MATH, START_DATE, TIME,
                DURATION, null, 4, 1));
        assertThrows(NullPointerException.class, () -> new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, null,
                TIME, DURATION, null, 4, 1));
        assertThrows(NullPointerException.class, () -> new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH,
                START_DATE, null, DURATION, null, 4, 1));
        assertThrows(NullPointerException.class, () -> new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH,
                START_DATE, TIME, null, null, 4, 1));
    }

    @Test
    public void isValidCountAndEvery_boundaries() {
        assertFalse(LessonRepeatCommand.isValidCount(1));
        assertTrue(LessonRepeatCommand.isValidCount(2));
        assertTrue(LessonRepeatCommand.isValidCount(52));
        assertFalse(LessonRepeatCommand.isValidCount(53));
        assertFalse(LessonRepeatCommand.isValidEvery(0));
        assertTrue(LessonRepeatCommand.isValidEvery(1));
        assertTrue(LessonRepeatCommand.isValidEvery(12));
        assertFalse(LessonRepeatCommand.isValidEvery(13));
    }

    @Test
    public void execute_weeklySeries_addsEveryOccurrenceWithSharedSeriesId() throws Exception {
        model.updateFilteredLessonList(lesson -> false); // the new lessons will not be displayed
        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 1));

        assertEquals("Scheduled 4 weekly occurrences.\nThe new lessons are not in the displayed list. "
                + "Run 'agenda' or 'lesson list st/1' to number them.", message);
        List<Lesson> lessons = allLessons();
        assertEquals(4, lessons.size());
        for (int i = 0; i < 4; i++) {
            Lesson lesson = lessons.get(i);
            assertEquals(START.plusWeeks(i), lesson.getDate().value);
            assertEquals(TIME, lesson.getTime());
            assertEquals(DURATION, lesson.getDuration());
            assertEquals(ALICE.getLevel().value, lesson.getAcademicLevel().get().value);
            assertEquals(ALICE.getVenue(), lesson.getVenue());
            assertTrue(lesson.isScheduled());
        }
        assertEquals(1, lessons.stream().map(lesson -> lesson.getSeriesId().get()).distinct().count());
    }

    @Test
    public void execute_everyTwoWeeks_spacesOccurrencesAndSaysSo() throws Exception {
        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                new Venue("Online"), 3, 2));

        assertTrue(message.startsWith("Scheduled 3 occurrences, every 2 weeks."));
        List<Lesson> lessons = allLessons();
        assertEquals(List.of(START, START.plusWeeks(2), START.plusWeeks(4)),
                lessons.stream().map(lesson -> lesson.getDate().value).collect(Collectors.toList()));
        assertTrue(lessons.stream().allMatch(lesson -> lesson.getVenue().get().equals(new Venue("Online"))));
    }

    @Test
    public void execute_startsToday_success() throws Exception {
        execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate(LocalDate.now().toString()),
                TIME, DURATION, null, 2, 1));

        assertEquals(2, allLessons().size());
    }

    @Test
    public void execute_subjectOmitted_usesOnlySubject() throws Exception {
        execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, null, START_DATE, TIME, DURATION, null, 2, 1));

        assertEquals(MATH, allLessons().get(0).getSubject());
    }

    @Test
    public void execute_subjectErrors_addNothing() {
        assertCommandFailure(new LessonRepeatCommand(INDEX_SECOND_PERSON, null, START_DATE, TIME, DURATION, null,
                3, 1), model, String.format(LessonAddCommand.MESSAGE_SUBJECT_REQUIRED, BENSON.getName(),
                "Math, Physics"));
        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, new Subject("Physics"), START_DATE, TIME,
                DURATION, null, 3, 1), model, String.format(LessonAddCommand.MESSAGE_SUBJECT_NOT_TAKEN,
                ALICE.getName(), "Physics"));
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_archivedStudent_addsNothing() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(student, student.withArchived(true));
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION, null,
                3, 1), model, LessonRepeatCommand.MESSAGE_STUDENT_ARCHIVED);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new LessonRepeatCommand(outOfBounds, MATH, START_DATE, TIME, DURATION, null, 2, 1),
                model, Messages.invalidPersonIndex(outOfBounds, model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_startInPast_addsNothing() {
        LessonDate yesterday = new LessonDate(LocalDate.now().minusDays(1).toString());

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, yesterday, TIME, DURATION, null,
                3, 1), model, LessonRepeatCommand.MESSAGE_DATE_IN_PAST);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_lastOccurrenceBeyondTwoYears_addsNothing() {
        // 52 weekly occurrences from 3 months ahead end beyond the two year limit only if the gap is large
        LessonDate start = new LessonDate(LocalDate.now().plusYears(2).minusDays(7).toString());

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, start, TIME, DURATION, null, 3, 1),
                model, LessonCommandUtil.MESSAGE_DATE_TOO_FAR);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_lastOccurrenceOnTwoYearLimit_success() throws Exception {
        LessonDate start = new LessonDate(LocalDate.now().plusYears(2).minusWeeks(2).toString());

        execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, start, TIME, DURATION, null, 3, 1));

        assertEquals(3, allLessons().size());
    }

    @Test
    public void execute_lessonRunsPastMidnight_addsNothing() {
        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, new LessonTime("23:00"),
                new LessonDuration(120), null, 3, 1), model, Lesson.MESSAGE_CONSTRAINTS);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_anyOccurrenceDuplicate_addsNothing() {
        // The third occurrence is identical to a lesson that already exists
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(START.plusWeeks(2)).withTime("16:30")
                .withDuration(90).build());
        int before = allLessons().size();

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION, null,
                4, 1), model, "This exact lesson already exists. No occurrences were added.");
        assertEquals(before, allLessons().size());
    }

    @Test
    public void execute_cancelledLessonInSlot_doesNotCountAsDuplicate() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(START).withTime("16:30").withDuration(90)
                .build().cancel("Student unwell"));

        execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION, null, 2, 1));

        assertEquals(3, allLessons().size());
    }

    @Test
    public void execute_overlapsFewExistingLessons_listsEachWarning() throws Exception {
        Lesson existing = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
                .withDate(START.plusWeeks(1)).withTime("17:00").withDuration(60).build();
        model.addLesson(existing);

        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 3, 1));

        assertTrue(message.contains(String.format(LessonRepeatCommand.MESSAGE_OVERLAP_LINE,
                new LessonDate(START.plusWeeks(1).toString()), Messages.format(existing) + ".")));
        assertEquals(1, message.split("Warning", -1).length - 1);
    }

    @Test
    public void execute_overlapsManyExistingLessons_summarisesWarnings() throws Exception {
        for (int i = 0; i < 5; i++) {
            model.addLesson(new LessonBuilder().withStudent(BENSON).withSubject("Physics")
                    .withDate(START.plusWeeks(i)).withTime("17:00").withDuration(60).build());
        }

        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 5, 1));

        assertTrue(message.contains(String.format(LessonRepeatCommand.MESSAGE_OVERLAP_SUMMARY, 5, 5,
                START_DATE)));
        assertEquals(1, message.split("Warning", -1).length - 1);
        assertEquals(10, allLessons().size()); // overlaps warn, they never block
    }

    @Test
    public void execute_seriesInsertedAmongDisplayedLessons_warnsNumbersChanged() throws Exception {
        model.addLesson(new LessonBuilder().withDate(START.plusWeeks(1).plusDays(1)).build());
        model.updateFilteredLessonList(Model.PREDICATE_SHOW_ALL_LESSONS);

        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 2, 1));

        assertTrue(message.startsWith("Scheduled 2 weekly occurrences.\n\nLesson numbers have changed. "
                + "Current lesson list:\n1. "), message);
        assertEquals(1, message.split("Lesson numbers have changed", -1).length - 1);
        assertFalse(message.contains("to refresh them"));
    }

    @Test
    public void execute_seriesAfterEveryDisplayedLesson_leavesNumbersUnchanged() throws Exception {
        model.addLesson(new LessonBuilder().withDate(START.minusDays(1)).build());
        model.updateFilteredLessonList(Model.PREDICATE_SHOW_ALL_LESSONS);

        String message = execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 2, 1));

        assertEquals("Scheduled 2 weekly occurrences.\nThe new lessons are not in the displayed list. "
                + "Run 'agenda' or 'lesson list st/1' to number them.", message);
    }

    @Test
    public void execute_startDateAtTodayOfClock_success() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"), ZoneOffset.UTC);

        assertTrue(execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2026-10-21"), TIME,
                DURATION, null, 2, 1, clock)).startsWith("Scheduled 2 weekly occurrences."));
    }

    @Test
    public void execute_startDateBeforeTodayOfClock_addsNothing() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"), ZoneOffset.UTC);

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2026-10-20"), TIME,
                DURATION, null, 2, 1, clock), model, LessonRepeatCommand.MESSAGE_DATE_IN_PAST);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_lastOccurrenceOnTwoYearLimitOfClock_success() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"), ZoneOffset.UTC);

        // 2 occurrences, a week apart, the second exactly two years after today
        assertTrue(execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2028-10-14"), TIME,
                DURATION, null, 2, 1, clock)).startsWith("Scheduled 2 weekly occurrences."));
    }

    @Test
    public void execute_lastOccurrenceAfterTwoYearLimitOfClock_addsNothing() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-21T04:00:00Z"), ZoneOffset.UTC);

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2028-10-15"), TIME,
                DURATION, null, 2, 1, clock), model, LessonCommandUtil.MESSAGE_DATE_TOO_FAR);
        assertTrue(allLessons().isEmpty());
    }

    @Test
    public void execute_eachOccurrenceIsIndependent_canBeCancelledAlone() throws Exception {
        execute(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION, null, 3, 1));
        List<Lesson> lessons = allLessons();

        model.setLesson(lessons.get(1), lessons.get(1).cancel("Student unwell"));

        assertEquals(1, allLessons().stream().filter(Lesson::isCancelled).count());
        assertEquals(2, allLessons().stream().filter(Lesson::isScheduled).count());
    }

    @Test
    public void equals() {
        LessonRepeatCommand command = new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 1);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 1)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_SECOND_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, null, START_DATE, TIME, DURATION,
                null, 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, new LessonDate("2030-01-01"),
                TIME, DURATION, null, 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE,
                new LessonTime("09:00"), DURATION, null, 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME,
                new LessonDuration(60), null, 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                new Venue("Online"), 4, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 5, 1)));
        assertFalse(command.equals(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 2)));
    }

    @Test
    public void toStringMethod() {
        LessonRepeatCommand command = new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION,
                null, 4, 2);
        String expected = LessonRepeatCommand.class.getCanonicalName() + "{studentIndex=" + INDEX_FIRST_PERSON
                + ", subject=" + MATH + ", startDate=" + START_DATE + ", time=" + TIME + ", duration=" + DURATION
                + ", venue=null, count=4, everyWeeks=2}";
        assertEquals(expected, command.toString());
    }

    private String execute(LessonRepeatCommand command) throws CommandException {
        return command.execute(model).getFeedbackToUser();
    }

    /** Returns every lesson in the model, in the order of the address book, ignoring the displayed filter. */
    private List<Lesson> allLessons() {
        return model.getAddressBook().getLessonList().stream()
                .sorted(Lesson.CHRONOLOGICAL).collect(Collectors.toList());
    }

    @Test
    public void execute_addingFailsHalfway_rollsBackEveryAddedOccurrence() {
        Model failingModel = new ModelManager(getTypicalAddressBook(), new UserPrefs()) {
            private int addCount = 0;

            @Override
            public void addLesson(Lesson lesson) {
                addCount++;
                if (addCount == 3) {
                    throw new IllegalStateException("Simulated failure");
                }
                super.addLesson(lesson);
            }
        };

        assertCommandFailure(new LessonRepeatCommand(INDEX_FIRST_PERSON, MATH, START_DATE, TIME, DURATION, null,
                5, 1), failingModel, LessonRepeatCommand.MESSAGE_ROLLED_BACK);
        assertTrue(failingModel.getAddressBook().getLessonList().isEmpty());
    }
}
