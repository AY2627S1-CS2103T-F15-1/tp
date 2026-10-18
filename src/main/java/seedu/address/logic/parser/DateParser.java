package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonDate;

/** Shared local-calendar aliases, with explicit formats delegated to the existing date parser. */
public final class DateParser {
    private DateParser() {
    }

    /** Resolves today and tmr against an injectable clock. */
    public static LocalDate parse(String text, Clock clock) throws ParseException {
        return switch (text.strip().toLowerCase(Locale.ENGLISH)) {
            case "today" -> LocalDate.now(clock);
            case "tmr" -> LocalDate.now(clock).plusDays(1);
            default -> ParserUtil.parseDate(text).value;
        };
    }

    /** Resolves a lesson date, accepting today and tmr in addition to the explicit formats. */
    public static LessonDate parseLessonDate(String text, Clock clock) throws ParseException {
        return new LessonDate(parse(text, clock).toString());
    }
}
