package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_keywordTooLong_throwsParseException() {
        String longKeyword = "a".repeat(FindCommand.MAX_KEYWORD_LENGTH + 1);
        assertParseFailure(parser, "Alice " + longKeyword, FindCommand.MESSAGE_KEYWORD_TOO_LONG);

        // a keyword of exactly the maximum length is accepted
        String maxLengthKeyword = "a".repeat(FindCommand.MAX_KEYWORD_LENGTH);
        assertParseSuccess(parser, maxLengthKeyword,
                new FindCommand(new NameContainsKeywordsPredicate(List.of(maxLengthKeyword))));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

}
