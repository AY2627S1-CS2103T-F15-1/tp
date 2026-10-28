package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkFeedbackCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;

public class HomeworkCommandParserTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);
    private static final String INVALID_FORMAT = MESSAGE_INVALID_COMMAND_FORMAT;

    private final HomeworkCommandParser parser = new HomeworkCommandParser(
            Clock.fixed(Instant.parse("2030-01-10T00:00:00Z"), ZoneOffset.UTC));

    private static String missing(String prefixes, String usage) {
        return String.format(ParserUtil.MESSAGE_MISSING_PARAMETERS, prefixes, usage);
    }

    private static String emptyValue(String prefix) {
        return "Empty value for " + prefix + ". Enter a value after this prefix.";
    }

    @Test
    public void parse_noOrUnknownSubcommand_listsTheSubcommands() {
        assertParseFailure(parser, "", HomeworkCommandParser.MESSAGE_SUBCOMMANDS);
        assertParseFailure(parser, "   ", HomeworkCommandParser.MESSAGE_SUBCOMMANDS);
        assertParseFailure(parser, "assign st/1", HomeworkCommandParser.MESSAGE_SUBCOMMANDS);
    }

    @Test
    public void parse_controlCharacters_rejected() {
        assertParseFailure(parser, "list\n", HomeworkCommandParser.MESSAGE_NOT_PRINTABLE);
        assertParseFailure(parser, "add st/1 desc/A\tB due/tmr", HomeworkCommandParser.MESSAGE_NOT_PRINTABLE);
    }

    @Test
    public void parseAdd_validArguments_success() {
        HomeworkAddCommand expected = new HomeworkAddCommand(INDEX_FIRST_PERSON, "Algebra Q1-10",
                TODAY.plusDays(1), TODAY);

        assertParseSuccess(parser, "add st/1 desc/Algebra Q1-10 due/tmr", expected);
        assertParseSuccess(parser, "add due/TMR desc/Algebra Q1-10 st/1", expected);
        assertParseSuccess(parser, "add st/1 desc/  Algebra   Q1-10  due/2030-01-11", expected);
        assertParseSuccess(parser, "add st/ 1  desc/Algebra Q1-10 due/11/01/2030", expected);
        assertParseSuccess(parser, "add st/2 desc/Ex 7.2 Q1-15 due/today",
                new HomeworkAddCommand(INDEX_SECOND_PERSON, "Ex 7.2 Q1-15", TODAY, TODAY));
    }

    @Test
    public void parseAdd_textThatLooksLikeAnotherPrefix_staysInTheDescription() {
        assertParseSuccess(parser, "add st/1 desc/Read n/a notes and s/o p/3 due/tmr",
                new HomeworkAddCommand(INDEX_FIRST_PERSON, "Read n/a notes and s/o p/3", TODAY.plusDays(1), TODAY));
    }

    @Test
    public void parseAdd_missingPrefixes_namesEachOne() {
        String usage = HomeworkAddCommand.MESSAGE_USAGE;

        assertParseFailure(parser, "add", missing("st/, desc/, due/", usage));
        assertParseFailure(parser, "add st/1 due/tmr", missing("desc/", usage));
        assertParseFailure(parser, "add desc/A due/tmr", missing("st/", usage));
        assertParseFailure(parser, "add st/1 desc/A", missing("due/", usage));
        assertParseFailure(parser, "add desc/A", missing("st/, due/", usage));
    }

    @Test
    public void parseAdd_invalidStudentIndex_invalidCommandFormat() {
        String expected = String.format(INVALID_FORMAT, HomeworkAddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "add st/0 desc/A due/tmr", expected);
        assertParseFailure(parser, "add st/-1 desc/A due/tmr", expected);
        assertParseFailure(parser, "add st/one desc/A due/tmr", expected);
        assertParseFailure(parser, "add st/1.5 desc/A due/tmr", expected);
    }

    @Test
    public void parseAdd_textBeforeFirstPrefix_invalidCommandFormat() {
        assertParseFailure(parser, "add 1 st/1 desc/A due/tmr",
                String.format(INVALID_FORMAT, HomeworkAddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parseAdd_emptyDuplicateOrInvalidValues_namesTheProblem() {
        assertParseFailure(parser, "add st/ desc/A due/tmr", emptyValue("st/"));
        assertParseFailure(parser, "add st/1 desc/ due/tmr", emptyValue("desc/"));
        assertParseFailure(parser, "add st/1 desc/A due/", emptyValue("due/"));
        assertParseFailure(parser, "add st/1 desc/A due/tmr due/today",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("due/")));
        assertParseFailure(parser, "add st/1 desc/" + "x".repeat(201) + " due/tmr", Homework.DESCRIPTION_CONSTRAINTS);
        assertParseFailure(parser, "add st/1 desc/A due/2030-02-30", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "add st/1 desc/A due/9/10", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "add st/1 desc/A due/next week", LessonDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseAdd_descriptionLengthBoundaries() {
        assertParseSuccess(parser, "add st/1 desc/" + "x".repeat(200) + " due/tmr",
                new HomeworkAddCommand(INDEX_FIRST_PERSON, "x".repeat(200), TODAY.plusDays(1), TODAY));
        assertParseFailure(parser, "add st/1 desc/" + "x".repeat(1001) + " due/tmr", Homework.DESCRIPTION_CONSTRAINTS);
    }

    @Test
    public void parseEdit_validArguments_success() {
        assertParseSuccess(parser, "edit 1 due/2030-01-11",
                new HomeworkEditCommand(INDEX_FIRST_PERSON, null, TODAY.plusDays(1), TODAY));
        assertParseSuccess(parser, "edit 2 desc/Trigonometry worksheet, Q1-8",
                new HomeworkEditCommand(INDEX_SECOND_PERSON, "Trigonometry worksheet, Q1-8", null, TODAY));
        assertParseSuccess(parser, "edit  1  due/tmr   desc/New  text",
                new HomeworkEditCommand(INDEX_FIRST_PERSON, "New text", TODAY.plusDays(1), TODAY));
    }

    @Test
    public void parseEdit_noFieldGiven_answersWithTheUsageString() {
        assertParseFailure(parser, "edit 1", HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "edit", HomeworkEditCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "edit abc", HomeworkEditCommand.MESSAGE_USAGE);
    }

    @Test
    public void parseEdit_invalidIndexOrValues_namesTheProblem() {
        String expected = String.format(INVALID_FORMAT, HomeworkEditCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "edit desc/A", expected);
        assertParseFailure(parser, "edit 0 desc/A", expected);
        assertParseFailure(parser, "edit -3 due/tmr", expected);
        assertParseFailure(parser, "edit x desc/A", expected);
        assertParseFailure(parser, "edit 1 desc/", emptyValue("desc/"));
        assertParseFailure(parser, "edit 1 due/", emptyValue("due/"));
        assertParseFailure(parser, "edit 1 due/tmr due/today",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("due/")));
        assertParseFailure(parser, "edit 1 due/32/01/2030", LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "edit 1 desc/" + "x".repeat(201), Homework.DESCRIPTION_CONSTRAINTS);
    }

    @Test
    public void parseList_validArguments_success() {
        assertParseSuccess(parser, "list", new HomeworkListCommand(null, false, false, TODAY));
        assertParseSuccess(parser, "list st/1", new HomeworkListCommand(INDEX_FIRST_PERSON, false, false, TODAY));
        assertParseSuccess(parser, "list overdue/", new HomeworkListCommand(null, true, false, TODAY));
        assertParseSuccess(parser, "list outstanding/", new HomeworkListCommand(null, false, true, TODAY));
        assertParseSuccess(parser, "list st/2 outstanding/",
                new HomeworkListCommand(INDEX_SECOND_PERSON, false, true, TODAY));
        assertParseSuccess(parser, "list outstanding/ st/2",
                new HomeworkListCommand(INDEX_SECOND_PERSON, false, true, TODAY));
        assertParseSuccess(parser, "list   overdue/   st/1  ",
                new HomeworkListCommand(INDEX_FIRST_PERSON, true, false, TODAY));
    }

    @Test
    public void parseList_bothFlags_chooseOne() {
        assertParseFailure(parser, "list overdue/ outstanding/", HomeworkListCommand.MESSAGE_FLAGS_EXCLUSIVE);
        assertParseFailure(parser, "list st/1 outstanding/ overdue/", HomeworkListCommand.MESSAGE_FLAGS_EXCLUSIVE);
    }

    @Test
    public void parseList_flagGivenAValue_answersWithTheUsageString() {
        assertParseFailure(parser, "list overdue/yes", HomeworkListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "list outstanding/ 1", HomeworkListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "list st/1 overdue/now", HomeworkListCommand.MESSAGE_USAGE);
    }

    @Test
    public void parseList_badStudentIndexOrExtraText_invalidCommandFormat() {
        String expected = String.format(INVALID_FORMAT, HomeworkListCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "list st/0", expected);
        assertParseFailure(parser, "list st/abc", expected);
        assertParseFailure(parser, "list 1", expected);
        assertParseFailure(parser, "list everything", expected);
        assertParseFailure(parser, "list st/", emptyValue("st/"));
        assertParseFailure(parser, "list st/1 st/2",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("st/")));
        assertParseFailure(parser, "list overdue/ overdue/",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("overdue/")));
    }

    @Test
    public void parseStatus_validArguments_success() {
        assertParseSuccess(parser, "status 1 s/submitted",
                new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED));
        assertParseSuccess(parser, "status 2 s/reviewed",
                new HomeworkStatusCommand(INDEX_SECOND_PERSON, HomeworkStatus.REVIEWED));
        assertParseSuccess(parser, "status 1 s/assigned",
                new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.ASSIGNED));
        assertParseSuccess(parser, "status 1 s/SuBmItTeD ",
                new HomeworkStatusCommand(INDEX_FIRST_PERSON, HomeworkStatus.SUBMITTED));
    }

    @Test
    public void parseStatus_missingStatus_namesThePrefix() {
        assertParseFailure(parser, "status 1", missing("s/", HomeworkStatusCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "status", missing("s/", HomeworkStatusCommand.MESSAGE_USAGE));
    }

    @Test
    public void parseStatus_invalidIndexOrStatus_namesTheProblem() {
        String expected = String.format(INVALID_FORMAT, HomeworkStatusCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "status s/submitted", expected);
        assertParseFailure(parser, "status 0 s/submitted", expected);
        assertParseFailure(parser, "status one s/submitted", expected);
        assertParseFailure(parser, "status 1 s/", emptyValue("s/"));
        assertParseFailure(parser, "status 1 s/overdue", HomeworkStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "status 1 s/done", HomeworkStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "status 1 s/submitted s/reviewed",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("s/")));
    }

    @Test
    public void parseFeedback_validArguments_success() {
        assertParseSuccess(parser, "feedback 1 f/Sign errors throughout Q8-12. Revisit negative indices.",
                new HomeworkFeedbackCommand(INDEX_FIRST_PERSON,
                        "Sign errors throughout Q8-12. Revisit negative indices."));
        assertParseSuccess(parser, "feedback 2 f/  Spaced   out ",
                new HomeworkFeedbackCommand(INDEX_SECOND_PERSON, "Spaced out"));
        assertParseSuccess(parser, "feedback 1 f/Check st/1 and desc/x",
                new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "Check st/1 and desc/x"));
        assertParseSuccess(parser, "feedback 1 f/" + "x".repeat(1000),
                new HomeworkFeedbackCommand(INDEX_FIRST_PERSON, "x".repeat(1000)));
    }

    @Test
    public void parseFeedback_missingOrInvalid_namesTheProblem() {
        String expected = String.format(INVALID_FORMAT, HomeworkFeedbackCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "feedback 1", missing("f/", HomeworkFeedbackCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "feedback", missing("f/", HomeworkFeedbackCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "feedback f/Good", expected);
        assertParseFailure(parser, "feedback 0 f/Good", expected);
        assertParseFailure(parser, "feedback 1 f/", emptyValue("f/"));
        assertParseFailure(parser, "feedback 1 f/" + "x".repeat(1001), Lesson.MESSAGE_NOTES_CONSTRAINTS);
        assertParseFailure(parser, "feedback 1 f/Good f/Better",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("f/")));
    }

    @Test
    public void parseDelete_validArguments_success() {
        assertParseSuccess(parser, "delete 1", new HomeworkDeleteCommand(INDEX_FIRST_PERSON, false));
        assertParseSuccess(parser, "delete 2 confirm", new HomeworkDeleteCommand(INDEX_SECOND_PERSON, true));
        assertParseSuccess(parser, "delete   2    confirm  ", new HomeworkDeleteCommand(INDEX_SECOND_PERSON, true));
    }

    @Test
    public void parseDelete_invalidArguments_invalidCommandFormat() {
        String expected = String.format(INVALID_FORMAT, HomeworkDeleteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "delete", expected);
        assertParseFailure(parser, "delete 0", expected);
        assertParseFailure(parser, "delete one", expected);
        assertParseFailure(parser, "delete confirm", expected);
        assertParseFailure(parser, "delete 1 yes", expected);
        assertParseFailure(parser, "delete 1 confirm confirm", expected);
        assertParseFailure(parser, "delete 1 2", expected);
    }
}
