package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_noArguments_returnsUnconfirmedClearCommand() {
        assertParseSuccess(parser, "", new ClearCommand(false));
        assertParseSuccess(parser, "   ", new ClearCommand(false));
    }

    @Test
    public void parse_confirm_returnsConfirmedClearCommand() {
        assertParseSuccess(parser, " confirm", new ClearCommand(true));
        assertParseSuccess(parser, "  confirm  ", new ClearCommand(true));
    }

    @Test
    public void parse_anythingElse_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " 3", expected);
        assertParseFailure(parser, " confirm now", expected);
        assertParseFailure(parser, " CONFIRM", expected); // keywords are case-sensitive
    }
}
