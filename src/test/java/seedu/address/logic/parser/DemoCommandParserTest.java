package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;

public class DemoCommandParserTest {

    private final DemoCommandParser parser = new DemoCommandParser();

    @Test
    public void parse_noArguments_previewsSampleData() throws Exception {
        assertTrue(execute(parser.parse("")).startsWith("Load "));
        assertTrue(execute(parser.parse("   ")).startsWith("Load "));
    }

    @Test
    public void parse_confirm_loadsSampleData() throws Exception {
        assertTrue(execute(parser.parse(" confirm ")).startsWith("Sample students loaded."));
    }

    @Test
    public void parse_otherArguments_throwsParseException() {
        assertParseFailure(parser, " now", DemoCommandParser.MESSAGE_USAGE);
        assertParseFailure(parser, " confirm now", DemoCommandParser.MESSAGE_USAGE);
        assertParseFailure(parser, " CONFIRM", DemoCommandParser.MESSAGE_USAGE);
    }

    private static String execute(Command command) throws CommandException {
        return command.execute(new ModelManager()).getFeedbackToUser();
    }
}
