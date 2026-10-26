package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_COUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVERY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PAST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.util.Objects;
import java.util.function.IntPredicate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonRepeatCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Parses input arguments and creates a new LessonRepeatCommand object
 */
public class LessonRepeatCommandParser implements Parser<LessonRepeatCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonRepeatCommand
     * and returns a LessonRepeatCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonRepeatCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE,
                PREFIX_TIME, PREFIX_DURATION, PREFIX_VENUE, PREFIX_COUNT, PREFIX_EVERY, PREFIX_PAST);
        if (argMultimap.getValue(PREFIX_PAST).isPresent()) {
            throw new ParseException(LessonRepeatCommand.MESSAGE_PAST_NOT_ACCEPTED + "\n"
                    + LessonRepeatCommand.MESSAGE_USAGE);
        }
        ParserUtil.requirePrefixesPresent(argMultimap, LessonRepeatCommand.MESSAGE_USAGE, PREFIX_STUDENT,
                PREFIX_DATE, PREFIX_TIME, PREFIX_COUNT);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_DURATION, PREFIX_VENUE, PREFIX_COUNT, PREFIX_EVERY);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_DURATION, PREFIX_VENUE, PREFIX_COUNT, PREFIX_EVERY);

        Index studentIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_STUDENT).get(),
                LessonRepeatCommand.MESSAGE_USAGE);
        Subject subject = ParserUtil.parseOptional(argMultimap, PREFIX_SUBJECT, ParserUtil::parseSubject);
        Venue venue = ParserUtil.parseOptional(argMultimap, PREFIX_VENUE, ParserUtil::parseVenue);
        LessonDuration duration = Objects.requireNonNullElse(
                ParserUtil.parseOptional(argMultimap, PREFIX_DURATION, ParserUtil::parseDuration),
                new LessonDuration(LessonDuration.DEFAULT_MINUTES));
        LessonDate date = ParserUtil.parseDate(argMultimap.getValue(PREFIX_DATE).get());
        LessonTime time = ParserUtil.parseTime(argMultimap.getValue(PREFIX_TIME).get());

        int count = parseBounded(argMultimap.getValue(PREFIX_COUNT).get(), LessonRepeatCommand::isValidCount);
        int every = argMultimap.getValue(PREFIX_EVERY).isEmpty() ? LessonRepeatCommand.DEFAULT_EVERY
                : parseBounded(argMultimap.getValue(PREFIX_EVERY).get(), LessonRepeatCommand::isValidEvery);

        return new LessonRepeatCommand(studentIndex, subject, date, time, duration, venue, count, every);
    }

    /**
     * Parses {@code value} as a whole number accepted by {@code isValid}.
     *
     * @throws ParseException if it is not a whole number of at most 3 digits, or is not accepted.
     */
    private static int parseBounded(String value, IntPredicate isValid) throws ParseException {
        String trimmed = value.trim();
        // At most 3 digits, so that parsing cannot overflow; the largest accepted value is 52
        if (!trimmed.matches("\\d{1,3}") || !isValid.test(Integer.parseInt(trimmed))) {
            throw new ParseException(LessonRepeatCommand.MESSAGE_COUNT_CONSTRAINTS + "\n"
                    + LessonRepeatCommand.MESSAGE_USAGE);
        }
        return Integer.parseInt(trimmed);
    }

}
