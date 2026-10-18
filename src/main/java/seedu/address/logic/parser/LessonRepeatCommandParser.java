package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.time.Clock;
import java.util.Objects;

import seedu.address.logic.commands.LessonRepeatCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonDuration;

/** Parses finite recurrence options without accepting repeated or empty values. */
public class LessonRepeatCommandParser implements Parser<LessonRepeatCommand> {
    private static final Prefix COUNT = new Prefix("count/");
    private static final Prefix EVERY = new Prefix("every/");
    private final Clock clock;

    /** Uses the application's local time zone to resolve today and tmr. */
    public LessonRepeatCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses {@code clock} to resolve today and tmr. */
    public LessonRepeatCommandParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public LessonRepeatCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE,
                PREFIX_TIME, PREFIX_DURATION, PREFIX_VENUE, COUNT, EVERY);
        ParserUtil.requirePrefixesPresent(values, LessonRepeatCommand.MESSAGE_USAGE,
                PREFIX_STUDENT, PREFIX_DATE, PREFIX_TIME, COUNT);
        values.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_DURATION, PREFIX_VENUE, COUNT, EVERY);
        values.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_DURATION, PREFIX_VENUE, COUNT, EVERY);
        int count = parseBounded(values.getValue(COUNT).orElseThrow(), 2, 52);
        int weeks = parseBounded(values.getValue(EVERY).orElse("1"), 1, 12);
        return new LessonRepeatCommand(ParserUtil.parseIndex(values.getValue(PREFIX_STUDENT).orElseThrow()),
                ParserUtil.parseOptional(values, PREFIX_SUBJECT, ParserUtil::parseSubject),
                DateParser.parseLessonDate(values.getValue(PREFIX_DATE).orElseThrow(), clock),
                ParserUtil.parseTime(values.getValue(PREFIX_TIME).orElseThrow()),
                Objects.requireNonNullElse(ParserUtil.parseOptional(values, PREFIX_DURATION, ParserUtil::parseDuration),
                        new LessonDuration(LessonDuration.DEFAULT_MINUTES)),
                ParserUtil.parseOptional(values, PREFIX_VENUE, ParserUtil::parseVenue), count, weeks);
    }

    /** Parses decimal integers within the recurrence bounds. */
    private static int parseBounded(String text, int minimum, int maximum) throws ParseException {
        try {
            int number = Integer.parseInt(text);
            if (!text.matches("[0-9]+") || number < minimum || number > maximum) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ParseException(LessonRepeatCommand.MESSAGE_USAGE);
        }
    }
}
