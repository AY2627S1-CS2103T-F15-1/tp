package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonDoneCommand;
import seedu.address.model.lesson.Lesson;

public class LessonDoneCommandParserTest {

    private final LessonDoneCommandParser parser = new LessonDoneCommandParser();

    @Test
    public void parse_indexOnly_success() {
        assertParseSuccess(parser, " 1", new LessonDoneCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void parse_notes_normalisesSpacesAndPreservesPunctuation() {
        assertParseSuccess(parser, " 1 n/  Covered   roots. x = (-b ± √Δ)/2a.  ",
                new LessonDoneCommand(INDEX_FIRST_PERSON, "Covered roots. x = (-b ± √Δ)/2a."));
        assertParseSuccess(parser, " 1 n/" + "a".repeat(Lesson.MAX_NOTES_LENGTH),
                new LessonDoneCommand(INDEX_FIRST_PERSON, "a".repeat(Lesson.MAX_NOTES_LENGTH)));
    }

    @Test
    public void parse_invalidIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonDoneCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", " n/Notes", " 0", " -1", " abc", " 1.5", " 2147483648", " 1 2"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_invalidNotes_failure() {
        for (String notes : new String[] {"", "  ", "a".repeat(Lesson.MAX_NOTES_LENGTH + 1),
            "First\nSecond", "First\rSecond", "First\tSecond", "First\u2028Second"}) {
            assertParseFailure(parser, " 1 n/" + notes, Lesson.MESSAGE_NOTES_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedNotes_failure() {
        assertParseFailure(parser, " 1 n/Old n/New",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NOTES));
    }
}
