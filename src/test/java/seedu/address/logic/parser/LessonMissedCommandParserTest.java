package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonMissedCommand;

public class LessonMissedCommandParserTest {

    private final LessonMissedCommandParser parser = new LessonMissedCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, " 1 ", new LessonMissedCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidIndexOrExtraParameters_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonMissedCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", " ", "0", "-1", "abc", "1.5", "2147483648", "1 2",
            "1 n/Notes", "1 r/Student absent", "1 all/"}) {
            assertParseFailure(parser, input, expected);
        }
    }
}
