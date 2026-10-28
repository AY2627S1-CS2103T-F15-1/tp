package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.TaskDeleteCommand;
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

    @Test
    public void parse_unicodeWhitespaceAround_isStripped() throws Exception {
        ConfirmableIndex target = ConfirmableIndex.parse("\u20032\u3000", KEYWORD, USAGE);
        assertEquals(INDEX_SECOND_PERSON, target.getIndex());
        assertFalse(target.isConfirmed());
    }

    @Test
    public void parse_unicodeWhitespaceBetween_isSplit() throws Exception {
        ConfirmableIndex target = ConfirmableIndex.parse("2\u3000confirm", KEYWORD, USAGE);
        assertEquals(INDEX_SECOND_PERSON, target.getIndex());
        assertTrue(target.isConfirmed());
    }

    @Test
    public void deleteCommands_unicodeWhitespaceAround_behaveLikeAscii() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (String space : new String[]{"\u2003", "\u3000", "\u2009", "\u1680"}) {
            assertEquals(new DeleteCommand(INDEX_SECOND_PERSON, false), parser.parseCommand("delete 2" + space));
            assertEquals(new DeleteCommand(INDEX_SECOND_PERSON, true),
                    parser.parseCommand("delete 2 confirm" + space));
            assertEquals(new TaskDeleteCommand(INDEX_SECOND_PERSON, false),
                    parser.parseCommand("task delete 2" + space));
            assertEquals(new TaskDeleteCommand(INDEX_SECOND_PERSON, true),
                    parser.parseCommand("task delete 2 confirm" + space));
            assertEquals(new HomeworkDeleteCommand(INDEX_SECOND_PERSON, false),
                    parser.parseCommand("hw delete 2" + space));
            assertEquals(new HomeworkDeleteCommand(INDEX_SECOND_PERSON, true),
                    parser.parseCommand("hw delete 2 confirm" + space));
        }
    }
}
