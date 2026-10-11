package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HelpCommand;

public class HelpCommandParserTest {

    private HelpCommandParser parser = new HelpCommandParser();

    @Test
    public void parse_noArguments_returnsHelpForEveryCommand() {
        assertParseSuccess(parser, "", new HelpCommand());
    }

    @Test
    public void parse_commandName_returnsHelpForThatCommand() {
        assertParseSuccess(parser, " lesson add", new HelpCommand("lesson add"));
    }
}
