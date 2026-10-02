package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Parses input arguments and creates a new LessonAddCommand object
 */
public class LessonAddCommandParser implements Parser<LessonAddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonAddCommand
     * and returns a LessonAddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonAddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE,
                PREFIX_TIME, PREFIX_DURATION, PREFIX_VENUE);
        ParserUtil.requirePrefixesPresent(argMultimap, LessonAddCommand.MESSAGE_USAGE, PREFIX_STUDENT, PREFIX_DATE,
                PREFIX_TIME);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_SUBJECT, PREFIX_DATE, PREFIX_TIME,
                PREFIX_DURATION, PREFIX_VENUE);

        Index studentIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_STUDENT).get());

        // The subject and venue are optional, and stay null when they are not given
        Subject subject = null;
        if (argMultimap.getValue(PREFIX_SUBJECT).isPresent()) {
            subject = ParserUtil.parseSubject(argMultimap.getValue(PREFIX_SUBJECT).get());
        }
        Venue venue = null;
        if (argMultimap.getValue(PREFIX_VENUE).isPresent()) {
            venue = ParserUtil.parseVenue(argMultimap.getValue(PREFIX_VENUE).get());
        }
        LessonDuration duration = new LessonDuration(LessonDuration.DEFAULT_MINUTES);
        if (argMultimap.getValue(PREFIX_DURATION).isPresent()) {
            duration = ParserUtil.parseDuration(argMultimap.getValue(PREFIX_DURATION).get());
        }

        LessonDate date = ParserUtil.parseDate(argMultimap.getValue(PREFIX_DATE).get());
        LessonTime time = ParserUtil.parseTime(argMultimap.getValue(PREFIX_TIME).get());

        return new LessonAddCommand(studentIndex, subject, date, time, duration, venue);
    }

}
