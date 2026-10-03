package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

public class LessonAddCommandParserTest {

    private static final String USAGE = LessonAddCommand.MESSAGE_USAGE;
    private static final String REQUIRED_FIELDS = " st/1 d/2026-12-22 t/16:30";

    private LessonAddCommandParser parser = new LessonAddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON, new Subject("Math"),
                new LessonDate("2026-12-22"), new LessonTime("16:30"), new LessonDuration(90), new Venue("Online"));

        assertParseSuccess(parser, REQUIRED_FIELDS + " s/Math dur/90 v/Online", expected);
    }

    @Test
    public void parse_fieldsInAnyOrder_success() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON, new Subject("Math"),
                new LessonDate("2026-12-22"), new LessonTime("16:30"), new LessonDuration(90), null);

        assertParseSuccess(parser, " dur/90 t/16:30 s/Math d/2026-12-22 st/1", expected);
    }

    @Test
    public void parse_optionalFieldsOmitted_usesDefaults() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON, null, new LessonDate("2026-12-22"),
                new LessonTime("16:30"), new LessonDuration(LessonDuration.DEFAULT_MINUTES), null);

        assertParseSuccess(parser, REQUIRED_FIELDS, expected);
    }

    @Test
    public void parse_missingRequiredFields_failure() {
        assertParseFailure(parser, " st/1 s/Math t/16:30",
                String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, "d/", USAGE));
        assertParseFailure(parser, " s/Math", String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, "st/, d/, t/",
                USAGE));
    }

    @Test
    public void parse_textBeforeFirstPrefix_failure() {
        assertParseFailure(parser, " 1" + REQUIRED_FIELDS,
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, USAGE));
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " st/0 d/2026-12-22 t/16:30", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " st/1 d/22-12 t/16:30", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " st/1 d/2026-12-22 t/4pm-ish", LessonTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, REQUIRED_FIELDS + " dur/20", LessonDuration.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, REQUIRED_FIELDS + " s/#math", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, REQUIRED_FIELDS + " v/ ", Venue.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedField_failure() {
        assertParseFailure(parser, REQUIRED_FIELDS + " t/17:30",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TIME));
    }
}
