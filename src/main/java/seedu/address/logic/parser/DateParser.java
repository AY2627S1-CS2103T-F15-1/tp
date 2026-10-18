package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import seedu.address.logic.parser.exceptions.ParseException;

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
}
