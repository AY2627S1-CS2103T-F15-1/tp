package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DemoCommand;

public class DemoCommandParserTest {
    private static final String INVALID_FORMAT = "Invalid command format!\n" + DemoCommand.MESSAGE_USAGE;

    private final DemoCommandParser parser = new DemoCommandParser();

    @Test
    public void parse_validArguments_success() {
        assertParseSuccess(parser, "", new DemoCommand(DemoCommand.Action.PREVIEW));
        assertParseSuccess(parser, "   ", new DemoCommand(DemoCommand.Action.PREVIEW));
        assertParseSuccess(parser, " confirm", new DemoCommand(DemoCommand.Action.START));
        assertParseSuccess(parser, "  confirm  ", new DemoCommand(DemoCommand.Action.START));
        assertParseSuccess(parser, " exit", new DemoCommand(DemoCommand.Action.EXIT));
    }

    @Test
    public void parse_anythingElse_invalidCommandFormat() {
        assertParseFailure(parser, " now", INVALID_FORMAT);
        assertParseFailure(parser, " Confirm", INVALID_FORMAT);
        assertParseFailure(parser, " EXIT", INVALID_FORMAT);
        assertParseFailure(parser, " confirm exit", INVALID_FORMAT);
        assertParseFailure(parser, " 1", INVALID_FORMAT);
    }
}
