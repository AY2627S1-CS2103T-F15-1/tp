package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;

public class DateAliasParserTest {
    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T04:00:00Z"), ZONE);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private static LessonAddCommand lessonOn(String date) {
        return new LessonAddCommand(Index.fromOneBased(1), null, new LessonDate(date), new LessonTime("16:30"),
                new LessonDuration(LessonDuration.DEFAULT_MINUTES), null);
    }

    @Test
    public void agenda_aliasesAndDefault_resolveAgainstInjectedClock() {
        AgendaCommandParser parser = new AgendaCommandParser(CLOCK);
        assertParseSuccess(parser, "", new AgendaCommand(TODAY, false));
        assertParseSuccess(parser, " d/today", new AgendaCommand(TODAY, false));
        assertParseSuccess(parser, " d/TMR week/", new AgendaCommand(TODAY.plusDays(1), true));
        assertParseSuccess(parser, " week/", new AgendaCommand(TODAY, true));
    }

    @Test
    public void agenda_existingFormats_stillAccepted() {
        AgendaCommandParser parser = new AgendaCommandParser(CLOCK);
        assertParseSuccess(parser, " d/2026-10-22", new AgendaCommand(LocalDate.of(2026, 10, 22), false));
        assertParseSuccess(parser, " d/22/10/2026", new AgendaCommand(LocalDate.of(2026, 10, 22), false));
    }

    @Test
    public void lessonAdd_aliases_resolveAgainstInjectedClock() {
        LessonAddCommandParser parser = new LessonAddCommandParser(CLOCK);
        assertParseSuccess(parser, " st/1 d/today t/16:30", lessonOn("2026-10-20"));
        assertParseSuccess(parser, " st/1 d/Tmr t/16:30", lessonOn("2026-10-21"));
        assertParseSuccess(parser, " st/1 d/2026-12-22 t/16:30", lessonOn("2026-12-22"));
        assertParseSuccess(parser, " st/1 d/22/12/2026 t/16:30", lessonOn("2026-12-22"));
    }

    @Test
    public void lessonMove_aliases_resolveAgainstInjectedClock() {
        LessonMoveCommandParser parser = new LessonMoveCommandParser(CLOCK);
        assertParseSuccess(parser, " 1 d/today",
                new LessonMoveCommand(Index.fromOneBased(1), new LessonDate("2026-10-20"), null, null, null));
        assertParseSuccess(parser, " 1 d/tmr t/17:00", new LessonMoveCommand(Index.fromOneBased(1),
                new LessonDate("2026-10-21"), new LessonTime("17:00"), null, null));
        assertParseSuccess(parser, " 1 d/2026-12-24", new LessonMoveCommand(Index.fromOneBased(1),
                new LessonDate("2026-12-24"), null, null, null));
    }

    @Test
    public void aliases_unknownWord_keepExistingDateMessage() {
        assertParseFailure(new AgendaCommandParser(CLOCK), " d/tomorrow", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(new LessonAddCommandParser(CLOCK), " st/1 d/yesterday t/16:30",
                LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(new LessonMoveCommandParser(CLOCK), " 1 d/next", LessonDate.MESSAGE_CONSTRAINTS);
    }
}
