package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonListCommand;

public class LessonListCommandParserTest {

    private LessonListCommandParser parser = new LessonListCommandParser();

    @Test
    public void parse_studentIndex_success() {
        assertParseSuccess(parser, " st/1", new LessonListCommand(INDEX_FIRST_PERSON, false));
    }

    @Test
    public void parse_allFlag_success() {
        assertParseSuccess(parser, " st/1 all/", new LessonListCommand(INDEX_FIRST_PERSON, true));
        assertParseSuccess(parser, " all/ st/1", new LessonListCommand(INDEX_FIRST_PERSON, true));
    }

    @Test
    public void parse_missingStudentIndex_failure() {
        assertParseFailure(parser, " all/", String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, "st/",
                LessonListCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidStudentIndex_failure() {
        assertParseFailure(parser, " st/x", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_allFlagWithValue_failure() {
        assertParseFailure(parser, " st/1 all/yes", String.format(ParserUtil.MESSAGE_FLAG_TAKES_NO_VALUE, "all/"));
    }

    @Test
    public void parse_repeatedStudentIndex_failure() {
        assertParseFailure(parser, " st/1 st/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STUDENT));
    }
}
