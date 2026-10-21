package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.testutil.LessonBuilder;

public class LessonCommandUtilTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-12T12:00:00Z"), ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final String MESSAGE = "Rejected.";
    private static final String MESSAGE_FORMAT = "Dated %1$s.";

    private final Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(TODAY).build();

    @Test
    public void getTravelWarnings_exactSpecificationTextIncludesBothStudents() {
        ModelManager model = new ModelManager();
        Lesson previous = new LessonBuilder().withStudent("Tan Wei Ming", "91234567")
                .withDate("2026-10-24").withTime("10:00").withDuration(60)
                .withVenue("Blk 512 Bishan St 13").build();
        Lesson next = new LessonBuilder().withStudent("Priya Kumar", "92345678")
                .withDate("2026-10-24").withTime("11:10").withDuration(60)
                .withVenue("Blk 12 Clementi Ave 3").build();
        model.addLesson(previous);
        model.addLesson(next);
        String warning = "Travel warning: Tan Wei Ming at Blk 512 Bishan St 13 -> Priya Kumar at "
                + "Blk 12 Clementi Ave 3 (Sat 24 Oct 2026 11:10): 10 min available; 30 min required.";
        assertEquals("\n" + warning, LessonCommandUtil.getTravelWarnings(model, destination -> true));
        assertEquals("\n  " + warning, LessonCommandUtil.getTravelWarnings(model, destination -> true, "  "));
        assertEquals("", LessonCommandUtil.getTravelWarnings(model, destination -> false));
    }

    @Test
    public void requireStatusNot_otherStatus_doesNotThrow() throws Exception {
        LessonCommandUtil.requireStatusNot(lesson, LessonStatus.CANCELLED, MESSAGE);
    }

    @Test
    public void requireStatusNot_sameStatus_throwsCommandExceptionWithMessage() {
        assertThrows(CommandException.class, MESSAGE, () -> LessonCommandUtil.requireStatusNot(
                lesson.cancel(null), LessonStatus.CANCELLED, MESSAGE));
    }

    @Test
    public void requireNotInFuture_todayOrEarlier_doesNotThrow() throws Exception {
        LessonCommandUtil.requireNotInFuture(lesson, CLOCK, MESSAGE_FORMAT);
        LessonCommandUtil.requireNotInFuture(new LessonBuilder(lesson).withDate(TODAY.minusDays(1)).build(), CLOCK,
                MESSAGE_FORMAT);
    }

    @Test
    public void requireNotInFuture_tomorrow_throwsCommandExceptionNamingTheDate() {
        Lesson tomorrow = new LessonBuilder(lesson).withDate(TODAY.plusDays(1)).build();

        assertThrows(CommandException.class, String.format(MESSAGE_FORMAT, tomorrow.getDate()), ()
                -> LessonCommandUtil.requireNotInFuture(tomorrow, CLOCK, MESSAGE_FORMAT));
    }
}
