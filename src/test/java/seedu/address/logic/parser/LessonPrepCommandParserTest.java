package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class LessonPrepCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_missingEmptyDuplicateOrLongNotes_rejectsInput() {
        for (String arguments : new String[]{"1", "1 n/", "1 n/First n/Second", "1 n/" + "a".repeat(1001),
            "0 n/Notes", "1 n/Line\nBreak"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("lesson prep " + arguments));
        }
    }
}
