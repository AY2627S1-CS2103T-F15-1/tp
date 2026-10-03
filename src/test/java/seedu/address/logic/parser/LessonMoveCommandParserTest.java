package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Venue;

public class LessonMoveCommandParserTest {

    private LessonMoveCommandParser parser = new LessonMoveCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        LessonMoveCommand expected = new LessonMoveCommand(INDEX_FIRST_PERSON, new LessonDate("2026-12-24"),
                new LessonTime("17:00"), new LessonDuration(60), new Venue("Online"));

        assertParseSuccess(parser, " 1 d/2026-12-24 t/17:00 dur/60 v/Online", expected);
    }

    @Test
    public void parse_someFieldsPresent_leavesTheOthersNull() {
        assertParseSuccess(parser, " 1 d/2026-12-24",
                new LessonMoveCommand(INDEX_FIRST_PERSON, new LessonDate("2026-12-24"), null, null, null));
        assertParseSuccess(parser, " 1 v/Online t/17:00", new LessonMoveCommand(INDEX_FIRST_PERSON, null,
                new LessonTime("17:00"), null, new Venue("Online")));
    }

    @Test
    public void parse_noFieldsPresent_failure() {
        assertParseFailure(parser, " 1", LessonMoveCommand.MESSAGE_NOT_MOVED + "\n" + LessonMoveCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonMoveCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " d/2026-12-24", expected); // no index
        assertParseFailure(parser, " x d/2026-12-24", expected); // not a number
        assertParseFailure(parser, " 0 d/2026-12-24", expected); // not positive
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " 1 d/2026-13-01", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 t/late", LessonTime.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 dur/10", LessonDuration.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 v/ ", Venue.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedField_failure() {
        assertParseFailure(parser, " 1 d/2026-12-24 d/2026-12-25",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }
}
