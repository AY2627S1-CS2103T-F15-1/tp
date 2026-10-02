package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AgendaCommand;
import seedu.address.model.lesson.LessonDate;

public class AgendaCommandParserTest {

    private AgendaCommandParser parser = new AgendaCommandParser();

    @Test
    public void parse_noArguments_defaultsToToday() throws Exception {
        assertParseSuccess(parser, "", new AgendaCommand(LocalDate.now(), false));
    }

    @Test
    public void parse_dateAndWeek_success() {
        assertParseSuccess(parser, " d/2030-01-07", new AgendaCommand(LocalDate.of(2030, 1, 7), false));
        assertParseSuccess(parser, " week/ d/2030-01-07", new AgendaCommand(LocalDate.of(2030, 1, 7), true));
    }

    @Test
    public void parse_weekWithoutDate_usesToday() throws Exception {
        assertTrue(parser.parse(" week/").equals(new AgendaCommand(LocalDate.now(), true)));
    }

    @Test
    public void parse_invalidDate_failure() {
        assertParseFailure(parser, " d/tomorrow", LessonDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_weekWithValue_failure() {
        assertParseFailure(parser, " week/yes", String.format(ParserUtil.MESSAGE_FLAG_TAKES_NO_VALUE, "week/"));
    }

    @Test
    public void parse_textBeforeFirstPrefix_failure() {
        assertParseFailure(parser, " week",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AgendaCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_repeatedDate_failure() {
        assertParseFailure(parser, " d/2030-01-07 d/2030-01-08",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }
}
