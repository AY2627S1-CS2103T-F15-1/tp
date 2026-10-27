package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonRepeatCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

public class LessonRepeatCommandParserTest {

    private static final String USAGE = LessonRepeatCommand.MESSAGE_USAGE;
    private static final String RANGE_ERROR = LessonRepeatCommand.MESSAGE_COUNT_CONSTRAINTS + "\n" + USAGE;
    private static final String REQUIRED_FIELDS = " st/1 d/2026-12-22 t/16:30 count/4";

    private LessonRepeatCommandParser parser = new LessonRepeatCommandParser();

    @Test
    public void parse_requiredFieldsOnly_usesDefaults() {
        LessonRepeatCommand expected = new LessonRepeatCommand(INDEX_FIRST_PERSON, null,
                new LessonDate("2026-12-22"), new LessonTime("16:30"),
                new LessonDuration(LessonDuration.DEFAULT_MINUTES), null, 4, 1);

        assertParseSuccess(parser, REQUIRED_FIELDS, expected);
    }

    @Test
    public void parse_allFieldsPresent_success() {
        LessonRepeatCommand expected = new LessonRepeatCommand(INDEX_FIRST_PERSON, new Subject("Math"),
                new LessonDate("2026-12-22"), new LessonTime("16:30"), new LessonDuration(90),
                new Venue("Online"), 10, 2);

        assertParseSuccess(parser, " every/2 count/10 v/Online dur/90 s/Math t/16:30 d/2026-12-22 st/1", expected);
    }

    @Test
    public void parse_boundaryCountAndEvery_success() {
        assertParseSuccess(parser, " st/1 d/2026-12-22 t/16:30 count/2 every/1", new LessonRepeatCommand(
                INDEX_FIRST_PERSON, null, new LessonDate("2026-12-22"), new LessonTime("16:30"),
                new LessonDuration(60), null, 2, 1));
        assertParseSuccess(parser, " st/1 d/2026-12-22 t/16:30 count/52 every/12", new LessonRepeatCommand(
                INDEX_FIRST_PERSON, null, new LessonDate("2026-12-22"), new LessonTime("16:30"),
                new LessonDuration(60), null, 52, 12));
    }

    @Test
    public void parse_missingRequiredFields_failure() {
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30",
                String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, "count/", USAGE));
        assertParseFailure(parser, " s/Math", String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS,
                "st/, d/, t/, count/", USAGE));
    }

    @Test
    public void parse_countOrEveryOutOfRange_failure() {
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/1", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/53", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/0", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/-3", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/4 every/0", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/4 every/13", RANGE_ERROR);
    }

    @Test
    public void parse_countOrEveryNotAWholeNumber_failure() {
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/four", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/4.5", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/+4", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/99999999999", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/4 every/x", RANGE_ERROR);
    }

    @Test
    public void parse_emptyOrDuplicateValues_failure() {
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/",
                "Empty value for count/. Enter a value after this prefix.");
        assertParseFailure(parser, REQUIRED_FIELDS + " count/5",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_COUNT));
        assertParseFailure(parser, REQUIRED_FIELDS + " every/1 every/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_EVERY));
    }

    @Test
    public void parse_pastFlagNotAccepted_failure() {
        String expected = LessonRepeatCommand.MESSAGE_PAST_NOT_ACCEPTED + "\n" + USAGE;

        assertParseFailure(parser, REQUIRED_FIELDS + " past/", expected);
        assertParseFailure(parser, " past/" + REQUIRED_FIELDS, expected);
    }

    @Test
    public void parse_countOrEveryNotAPlainNumber_failure() {
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/4abc", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/-4", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/+4", RANGE_ERROR);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/16:30 count/99999999999", RANGE_ERROR);
        assertParseFailure(parser, REQUIRED_FIELDS + " every/1.5", RANGE_ERROR);
    }

    @Test
    public void parse_invalidLessonFields_failure() {
        assertParseFailure(parser, " st/1 d/22-12 t/16:30 count/4", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/25:00 count/4", LessonTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " st/0 d/2026-12-22 t/16:30 count/4",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, USAGE));
    }

    @Test
    public void parse_textBeforeFirstPrefix_failure() {
        assertParseFailure(parser, " 1" + REQUIRED_FIELDS,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, USAGE));
    }
}
