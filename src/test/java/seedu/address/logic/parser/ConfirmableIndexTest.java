package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class ConfirmableIndexTest {

    private static final String KEYWORD = "confirm";
    private static final String USAGE = "sample INDEX [confirm]";

    @Test
    public void parse_indexOnly_isNotConfirmed() throws Exception {
        ConfirmableIndex target = ConfirmableIndex.parse(" 2 ", KEYWORD, USAGE);
        assertEquals(INDEX_SECOND_PERSON, target.getIndex());
        assertFalse(target.isConfirmed());
    }

    @Test
    public void parse_indexWithKeyword_isConfirmed() throws Exception {
        ConfirmableIndex target = ConfirmableIndex.parse("2   confirm", KEYWORD, USAGE);
        assertEquals(INDEX_SECOND_PERSON, target.getIndex());
        assertTrue(target.isConfirmed());
    }

    @Test
    public void parse_anythingElse_showsUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, USAGE);
        for (String args : new String[] {"", "a", "0", "confirm", "2 CONFIRM", "2 yes", "2 confirm confirm",
            "2 3"}) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    ConfirmableIndex.parse(args, KEYWORD, USAGE), args);
            assertEquals(expectedMessage, exception.getMessage(), args);
        }
    }
}
