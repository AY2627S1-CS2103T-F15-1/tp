package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Command words, sub-command words and the confirm keyword do not depend on letter case. */
public class CommandWordCaseTest {

    private final AddressBookParser parser = new AddressBookParser(
            Clock.fixed(Instant.parse("2026-10-26T04:00:00Z"), ZoneId.of("Asia/Singapore")));

    private void assertSameCommand(String lowerCase, String otherCase) throws ParseException {
        assertEquals(parser.parseCommand(lowerCase), parser.parseCommand(otherCase), otherCase);
    }

    @Test
    public void parseCommand_addInAnyCase_sameAsLowerCase() throws Exception {
        String lowerCase = "add n/Amy Bee p/91234567 l/S3 s/Math r/50";
        assertSameCommand(lowerCase, "ADD n/Amy Bee p/91234567 l/S3 s/Math r/50");
        assertSameCommand(lowerCase, "Add n/Amy Bee p/91234567 l/S3 s/Math r/50");
        assertSameCommand(lowerCase, "aDd n/Amy Bee p/91234567 l/S3 s/Math r/50");
        assertSameCommand(lowerCase, "STUDENT ADD n/Amy Bee p/91234567 l/S3 s/Math r/50");
    }

    @Test
    public void parseCommand_topLevelCommands_sameAsLowerCase() throws Exception {
        assertSameCommand("list", "LIST");
        assertSameCommand("find amy", "Find amy");
        assertSameCommand("view 1", "VIEW 1");
        assertSameCommand("sort desc", "SORT DESC");
        assertSameCommand("delete 1 confirm", "DELETE 1 CONFIRM");
        assertSameCommand("delete 1 confirm", "Delete 1 Confirm");
        assertSameCommand("clear confirm", "CLEAR CONFIRM");
        assertSameCommand("agenda week/", "AGENDA week/");
        assertSameCommand("balance st/1", "BALANCE st/1");
        assertTrue(parser.parseCommand("EXIT") instanceof ExitCommand);
        assertSameCommand("help lesson add", "HELP lesson add");
    }

    @Test
    public void parseCommand_subCommands_sameAsLowerCase() throws Exception {
        assertSameCommand("lesson add st/1 d/2026-12-22 t/16:30", "LESSON ADD st/1 d/2026-12-22 t/16:30");
        assertSameCommand("lesson done 1 n/Covered algebra", "Lesson Done 1 n/Covered algebra");
        assertSameCommand("lesson series cancel 1 confirm", "LESSON SERIES CANCEL 1 CONFIRM");
        assertSameCommand("task delete 1 confirm", "TASK DELETE 1 CONFIRM");
        assertSameCommand("task list all/", "Task List all/");
        assertSameCommand("hw status 1 s/submitted", "HW STATUS 1 s/submitted");
        assertSameCommand("hw delete 1 confirm", "Hw Delete 1 Confirm");
        assertSameCommand("student list archived/", "Student LIST archived/");
    }

    @Test
    public void parseCommand_valuesKeepTheirCase() throws Exception {
        assertSameCommand("remark 1 r/Likes MATH", "REMARK 1 r/Likes MATH");
        assertNotEquals(parser.parseCommand("remark 1 r/Likes math"), parser.parseCommand("REMARK 1 r/Likes MATH"));
    }

    @Test
    public void parseCommand_unknownWordInAnyCase_stillUnknown() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("FOO"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("LESSON FOO"));
    }
}
