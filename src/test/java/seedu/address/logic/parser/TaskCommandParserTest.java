package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TaskAddCommand;
import seedu.address.logic.commands.TaskDeleteCommand;
import seedu.address.logic.commands.TaskDoneCommand;
import seedu.address.logic.commands.TaskEditCommand;
import seedu.address.logic.commands.TaskListCommand;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.task.Task;

public class TaskCommandParserTest {

    private static final String ADD_USAGE = TaskAddCommand.MESSAGE_USAGE;
    private static final String DESCRIPTION_TOO_LONG = "a".repeat(Task.MAX_DESCRIPTION_LENGTH + 1);

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-22T04:00:00Z"), ZoneId.of("Asia/Singapore"));

    private TaskCommandParser parser = new TaskCommandParser(CLOCK);

    // ---- routing

    @Test
    public void parse_missingOrUnknownSubCommand_failure() {
        assertParseFailure(parser, "  ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "Task commands: task add, task list, task edit, task done, task delete"));
        assertParseFailure(parser, " remove 1", MESSAGE_UNKNOWN_COMMAND);
        assertParseFailure(parser, " ADD st/1 desc/x", MESSAGE_UNKNOWN_COMMAND);
    }

    // ---- due dates

    @Test
    public void parseAdd_dueDate_pastIsRejectedAndTodayOnwardsAccepted() {
        assertParseFailure(parser, " add st/1 desc/x due/2026-10-21", TaskAddCommandParser.MESSAGE_PAST_DUE);
        assertParseSuccess(parser, " add st/1 desc/x due/2026-10-22",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "x", TODAY));
        assertParseSuccess(parser, " add st/1 desc/x due/2026-10-23",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "x", TODAY.plusDays(1)));
        assertParseSuccess(parser, " add st/1 desc/x due/today",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "x", TODAY));
        assertParseSuccess(parser, " add st/1 desc/x due/TMR",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "x", TODAY.plusDays(1)));
    }

    @Test
    public void parseEdit_dueDate_pastIsRejectedAndClearingStillWorks() {
        assertParseFailure(parser, " edit 1 desc/x due/2026-10-21", TaskAddCommandParser.MESSAGE_PAST_DUE);
        assertParseSuccess(parser, " edit 1 desc/x due/2026-10-22",
                new TaskEditCommand(INDEX_FIRST_PERSON, "x", TODAY, false));
        assertParseSuccess(parser, " edit 1 desc/x due/tmr",
                new TaskEditCommand(INDEX_FIRST_PERSON, "x", TODAY.plusDays(1), false));
        assertParseSuccess(parser, " edit 1 desc/x due/-", new TaskEditCommand(INDEX_FIRST_PERSON, "x", null, true));
    }

    // ---- task add

    @Test
    public void parseAdd_studentLink_success() {
        assertParseSuccess(parser, " add st/1 desc/Call parent",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call parent", null));
        assertParseSuccess(parser, " add desc/Call parent due/2026-12-01 st/2", new TaskAddCommand(
                INDEX_SECOND_PERSON, null, null, null, "Call parent", LocalDate.of(2026, 12, 1)));
    }

    @Test
    public void parseAdd_otherLinks_success() {
        assertParseSuccess(parser, " add lesson/1 desc/Prepare mock paper",
                new TaskAddCommand(null, INDEX_FIRST_PERSON, null, null, "Prepare mock paper", null));
        assertParseSuccess(parser, " add hw/2 desc/x",
                new TaskAddCommand(null, null, INDEX_SECOND_PERSON, null, "x", null));
        assertParseSuccess(parser, " add pay/1 desc/Remind about fees",
                new TaskAddCommand(null, null, null, INDEX_FIRST_PERSON, "Remind about fees", null));
    }

    @Test
    public void parseAdd_descriptionIsTidied() {
        assertParseSuccess(parser, " add st/1 desc/  Call    parent  ",
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null, "Call parent", null));
        assertParseSuccess(parser, " add st/1 desc/" + "a".repeat(Task.MAX_DESCRIPTION_LENGTH),
                new TaskAddCommand(INDEX_FIRST_PERSON, null, null, null,
                        "a".repeat(Task.MAX_DESCRIPTION_LENGTH), null));
    }

    @Test
    public void parseAdd_noLinkOrManyLinks_failureWithUsage() {
        assertParseFailure(parser, " add desc/x", ADD_USAGE);
        assertParseFailure(parser, " add st/1 lesson/1 desc/x", ADD_USAGE);
        assertParseFailure(parser, " add st/1 hw/1 pay/1 desc/x", ADD_USAGE);
        assertParseFailure(parser, " add", ADD_USAGE);
    }

    @Test
    public void parseAdd_missingDescription_failure() {
        assertParseFailure(parser, " add st/1",
                String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, "desc/", ADD_USAGE));
    }

    @Test
    public void parseAdd_invalidValues_failure() {
        assertParseFailure(parser, " add st/1 desc/", "Empty value for desc/. Enter a value after this prefix.");
        assertParseFailure(parser, " add st/1 desc/   due/2026-12-01",
                "Empty value for desc/. Enter a value after this prefix.");
        assertParseFailure(parser, " add st/1 desc/x due/", "Empty value for due/. Enter a value after this prefix.");
        assertParseFailure(parser, " add st/1 desc/x due/someday", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " add st/1 desc/x due/2026-02-30", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " add st/1 desc/" + DESCRIPTION_TOO_LONG, Task.MESSAGE_DESCRIPTION_CONSTRAINTS);
    }

    @Test
    public void parseAdd_invalidLinkIndex_usageFailure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskAddCommand.MESSAGE_USAGE);
        for (String prefix : new String[]{"st/", "lesson/", "hw/", "pay/"}) {
            for (String index : new String[]{"0", "-1", "abc", "1.5"}) {
                assertParseFailure(parser, " add " + prefix + index + " desc/x", expected);
            }
        }
    }

    @Test
    public void parseAdd_duplicateOrStrayText_failure() {
        assertParseFailure(parser, " add st/1 desc/x desc/y",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DESCRIPTION));
        assertParseFailure(parser, " add st/1 st/2 desc/x",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STUDENT));
        assertParseFailure(parser, " add oops st/1 desc/x",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ADD_USAGE));
    }

    // ---- task list

    @Test
    public void parseList_success() {
        assertParseSuccess(parser, " list", new TaskListCommand(false, null));
        assertParseSuccess(parser, " list all/", new TaskListCommand(true, null));
        assertParseSuccess(parser, " list st/1", new TaskListCommand(false, INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " list st/2 all/", new TaskListCommand(true, INDEX_SECOND_PERSON));
    }

    @Test
    public void parseList_invalid_failure() {
        assertParseFailure(parser, " list all/yes", String.format(ParserUtil.MESSAGE_FLAG_TAKES_NO_VALUE, "all/"));
        assertParseFailure(parser, " list st/", "Empty value for st/. Enter a value after this prefix.");
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskListCommand.MESSAGE_USAGE);
        for (String index : new String[]{"0", "-1", "abc", "1.5"}) {
            assertParseFailure(parser, " list st/" + index, expected);
        }
        assertParseFailure(parser, " list 1", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                TaskListCommand.MESSAGE_USAGE));
    }

    // ---- task edit

    @Test
    public void parseEdit_success() {
        assertParseSuccess(parser, " edit 1 desc/Call again",
                new TaskEditCommand(INDEX_FIRST_PERSON, "Call again", null, false));
        assertParseSuccess(parser, " edit 2 desc/Call again due/2026-12-01", new TaskEditCommand(
                INDEX_SECOND_PERSON, "Call again", LocalDate.of(2026, 12, 1), false));
        assertParseSuccess(parser, " edit 1 desc/Call again due/-",
                new TaskEditCommand(INDEX_FIRST_PERSON, "Call again", null, true));
    }

    @Test
    public void parseEdit_missingDescription_showsUsageWithDueExplanation() {
        assertParseFailure(parser, " edit 1", TaskEditCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " edit 1 due/-", TaskEditCommand.MESSAGE_USAGE);
    }

    @Test
    public void parseEdit_invalid_failure() {
        assertParseFailure(parser, " edit desc/x", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                TaskEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, " edit 0 desc/x", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                TaskEditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, " edit 1 desc/", "Empty value for desc/. Enter a value after this prefix.");
        assertParseFailure(parser, " edit 1 desc/x due/", "Empty value for due/. Enter a value after this prefix.");
        assertParseFailure(parser, " edit 1 desc/x due/--", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " edit 1 desc/" + DESCRIPTION_TOO_LONG, Task.MESSAGE_DESCRIPTION_CONSTRAINTS);
        assertParseFailure(parser, " edit 1 desc/x desc/y",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DESCRIPTION));
    }

    // ---- task done and delete

    @Test
    public void parseDone_success() {
        assertParseSuccess(parser, " done 1", new TaskDoneCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parseDone_invalid_failure() {
        String format = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskDoneCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " done", format);
        assertParseFailure(parser, " done 0", format);
        assertParseFailure(parser, " done a", format);
        assertParseFailure(parser, " done 1 2", format);
    }

    @Test
    public void parseDelete_success() {
        assertParseSuccess(parser, " delete 1", new TaskDeleteCommand(INDEX_FIRST_PERSON, false));
        assertParseSuccess(parser, " delete 2 confirm", new TaskDeleteCommand(INDEX_SECOND_PERSON, true));
    }

    @Test
    public void parseDelete_invalid_failure() {
        String format = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " delete", format);
        assertParseFailure(parser, " delete 0", format);
        assertParseFailure(parser, " delete 1 yes", format);
        assertParseFailure(parser, " delete confirm", format);
        assertParseFailure(parser, " delete 1 CONFIRM", format);
    }
}
