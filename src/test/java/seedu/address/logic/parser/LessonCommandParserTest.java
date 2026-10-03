package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonCancelCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Venue;

public class LessonCommandParserTest {

    private LessonCommandParser parser = new LessonCommandParser();

    @Test
    public void parse_lessonAdd_returnsLessonAddCommand() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON, null, new LessonDate("2026-12-22"),
                new LessonTime("16:30"), new LessonDuration(LessonDuration.DEFAULT_MINUTES), null);

        assertParseSuccess(parser, " add st/1 d/2026-12-22 t/16:30", expected);
    }

    @Test
    public void parse_lessonList_returnsLessonListCommand() {
        assertParseSuccess(parser, " list st/1 all/", new LessonListCommand(INDEX_FIRST_PERSON, true));
    }

    @Test
    public void parse_lessonMove_returnsLessonMoveCommand() {
        assertParseSuccess(parser, " move 1 v/Online", new LessonMoveCommand(INDEX_FIRST_PERSON, null, null, null,
                new Venue("Online")));
    }

    @Test
    public void parse_lessonCancel_returnsLessonCancelCommand() {
        assertParseSuccess(parser, " cancel 1", new LessonCancelCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void parse_missingLessonCommand_failure() {
        assertParseFailure(parser, "  ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "Lesson commands: lesson add, lesson list, lesson move, lesson cancel"));
    }

    @Test
    public void parse_unknownLessonCommand_failure() {
        assertParseFailure(parser, " fly st/1", MESSAGE_UNKNOWN_COMMAND);
    }
}
