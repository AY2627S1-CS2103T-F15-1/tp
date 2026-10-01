package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;

public class SortCommandParserTest {

    private SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_emptyArg_returnsAscendingSortCommand() {
        SortCommand expectedSortCommand = new SortCommand(false);

        assertParseSuccess(parser, "", expectedSortCommand);
        assertParseSuccess(parser, "     ", expectedSortCommand);
    }

    @Test
    public void parse_ascArg_returnsAscendingSortCommand() {
        SortCommand expectedSortCommand = new SortCommand(false);

        assertParseSuccess(parser, "asc", expectedSortCommand);

        // surrounding whitespace and mixed case are accepted
        assertParseSuccess(parser, " \n AsC \t", expectedSortCommand);
    }

    @Test
    public void parse_descArg_returnsDescendingSortCommand() {
        SortCommand expectedSortCommand = new SortCommand(true);

        assertParseSuccess(parser, "desc", expectedSortCommand);

        // surrounding whitespace and mixed case are accepted
        assertParseSuccess(parser, " \n DeSc \t", expectedSortCommand);
    }

    @Test
    public void parse_invalidArg_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        // unknown order
        assertParseFailure(parser, "name", expectedMessage);

        // more than one order
        assertParseFailure(parser, "asc desc", expectedMessage);

        // extra argument after a valid order
        assertParseFailure(parser, "desc 1", expectedMessage);
    }
}
