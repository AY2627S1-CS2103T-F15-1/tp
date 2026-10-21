package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
    public void parse_malformedOrRepeatedOrOutOfRange_rejected() {
        TravelCommandParser parser = new TravelCommandParser();
        for (String args : new String[]{"", "buffer/", "buffer/-1", "buffer/181", "buffer/1.5", "buffer/abc",
            "30", "buffer/30 buffer/40", "buffer/999999999999999999999999", "buffer/30 confirm", "BUFFER/30"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals(TravelCommand.MESSAGE_USAGE, error.getMessage());
        }
    }
}
