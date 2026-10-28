package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ListCommand;

public class SubCommandParserTest {

    private static final String USAGE = "Sample commands: sample clear, sample list";

    private final SubCommandParser parser = new SubCommandParser(createParsers(), USAGE);

    private static Map<String, Parser<? extends Command>> createParsers() {
        Map<String, Parser<? extends Command>> parsers = new LinkedHashMap<>();
        parsers.put("clear", new ClearCommandParser());
        parsers.put("list", new ListCommandParser());
        return parsers;
    }

    @Test
    public void parse_knownSubCommand_passesRemainingArgumentsToItsParser() {
        assertParseSuccess(parser, "clear confirm", new ClearCommand(true));
        assertParseSuccess(parser, "  clear  ", new ClearCommand(false));
        assertParseSuccess(parser, "list", new ListCommand(null, null, false, false));
    }

    @Test
    public void parse_missingSubCommand_showsUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
    }

    @Test
    public void parse_unknownSubCommand_throwsUnknownCommand() {
        assertParseFailure(parser, "remove", MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void parse_subCommandParserFailure_isPropagated() {
        assertParseFailure(parser, "clear later", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                ClearCommand.MESSAGE_USAGE));
    }
}
