package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.TravelCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class TravelCommandParserTest {
    @Test
    public void parse_validBoundaries_success() throws Exception {
        TravelCommandParser parser = new TravelCommandParser();
        for (int minutes : new int[]{0, 1, 30, 180}) {
            assertEquals(new TravelCommand(minutes), parser.parse(" buffer/" + minutes + " "));
            assertEquals(new TravelCommand(minutes), new AddressBookParser().parseCommand("travel buffer/" + minutes));
        }
    }

    @Test
    public void parse_invalidBuffer_rejectedWithUsage() {
        TravelCommandParser parser = new TravelCommandParser();
        for (String args : new String[]{"buffer/-1", "buffer/181", "buffer/1.5", "buffer/abc",
            "buffer/200", "buffer/999999999999999999999999", "buffer/30 confirm"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals(TravelCommand.MESSAGE_USAGE, error.getMessage());
        }
    }

    @Test
    public void parse_missingEmptyDuplicateAndPreamble_useCommonErrors() {
        for (String args : new String[]{"", "30", "BUFFER/30"}) {
            assertError(args, "Missing required parameter(s): buffer/\n" + TravelCommand.MESSAGE_USAGE);
        }
        assertError("buffer/  ", "Empty value for buffer/. Enter a value after this prefix.");
        assertError("buffer/30 buffer/40", Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_BUFFER));
        assertError("buffer/ buffer/40", Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_BUFFER));
        assertError("unexpected buffer/30", "Invalid command format!\n" + TravelCommand.MESSAGE_USAGE);
        assertError("5 buffer/10", "Invalid command format!\n" + TravelCommand.MESSAGE_USAGE);
        assertError("foo buffer/10", "Invalid command format!\n" + TravelCommand.MESSAGE_USAGE);
    }

    private static void assertError(String args, String expected) {
        ParseException error = assertThrows(ParseException.class, () -> new TravelCommandParser().parse(args));
        assertEquals(expected, error.getMessage());
    }
}
