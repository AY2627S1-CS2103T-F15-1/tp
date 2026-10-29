package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Venue;

/**
 * Parses input arguments and creates a new LessonMoveCommand object
 */
public class LessonMoveCommandParser implements Parser<LessonMoveCommand> {

    private final Clock clock;

    /** Uses the local system date. */
    public LessonMoveCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an explicit date source for relative dates. */
    public LessonMoveCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses the given {@code String} of arguments in the context of the LessonMoveCommand
     * and returns a LessonMoveCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonMoveCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_TIME, PREFIX_DURATION,
                PREFIX_VENUE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonMoveCommand.MESSAGE_USAGE),
                    pe);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TIME, PREFIX_DURATION, PREFIX_VENUE);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_DATE, PREFIX_TIME, PREFIX_DURATION, PREFIX_VENUE);

        LocalDate today = LocalDate.now(clock);
        LessonDate date = ParserUtil.parseOptional(argMultimap, PREFIX_DATE,
                value -> ParserUtil.parseDate(value, today));
        LessonTime time = ParserUtil.parseOptional(argMultimap, PREFIX_TIME, ParserUtil::parseTime);
        LessonDuration duration = ParserUtil.parseOptional(argMultimap, PREFIX_DURATION, ParserUtil::parseDuration);
        Venue venue = ParserUtil.parseOptional(argMultimap, PREFIX_VENUE, ParserUtil::parseVenue);

        if (date == null && time == null && duration == null && venue == null) {
            throw new ParseException(LessonMoveCommand.MESSAGE_NOT_MOVED + "\n" + LessonMoveCommand.MESSAGE_USAGE);
        }
        return new LessonMoveCommand(index, date, time, duration, venue);
    }

}
