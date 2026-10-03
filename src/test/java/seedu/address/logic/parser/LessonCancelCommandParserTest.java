package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonCancelCommand;
import seedu.address.model.lesson.Lesson;

public class LessonCancelCommandParserTest {

    private LessonCancelCommandParser parser = new LessonCancelCommandParser();

    @Test
    public void parse_indexOnly_success() {
        assertParseSuccess(parser, " 1", new LessonCancelCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void parse_indexAndReason_success() {
        assertParseSuccess(parser, " 1 r/Student unwell", new LessonCancelCommand(INDEX_FIRST_PERSON,
                "Student unwell"));
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonCancelCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expected);
        assertParseFailure(parser, " abc r/Student unwell", expected);
    }

    @Test
    public void parse_invalidReason_failure() {
        assertParseFailure(parser, " 1 r/ ", Lesson.MESSAGE_REASON_CONSTRAINTS);
        assertParseFailure(parser, " 1 r/" + "a".repeat(Lesson.MAX_REASON_LENGTH + 1),
                Lesson.MESSAGE_REASON_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedReason_failure() {
        assertParseFailure(parser, " 1 r/Unwell r/Holiday",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_REASON));
    }
}
