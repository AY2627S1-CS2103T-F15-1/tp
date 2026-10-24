package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.Level;
import seedu.address.model.person.Subject;

public class ListCommandParserTest {

    private static final Level S3 = new Level("S3");
    private static final Subject MATH = new Subject("Math");

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noArguments_listsActiveStudents() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_filtersAndFlags_acceptedInAnyOrder() {
        assertParseSuccess(parser, " l/s3", new ListCommand(S3, null, false, false));
        assertParseSuccess(parser, " s/math", new ListCommand(null, MATH, false, false));
        assertParseSuccess(parser, " s/Math l/S3", new ListCommand(S3, MATH, false, false));
        assertParseSuccess(parser, " l/S3 s/Math archived/", new ListCommand(S3, MATH, true, false));
        assertParseSuccess(parser, " archived/ l/S3", new ListCommand(S3, null, true, false));
        assertParseSuccess(parser, " all/", new ListCommand(null, null, false, true));
        assertParseSuccess(parser, " all/ s/Math", new ListCommand(null, MATH, false, true));
    }

    @Test
    public void parse_textBeforeThePrefixes_showsInvalidFormat() {
        String error = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " foo", error);
        assertParseFailure(parser, " student", error);
        assertParseFailure(parser, " foo l/S3", error);
    }

    @Test
    public void parse_bothArchiveFlags_showsTheUsageOfList() {
        assertParseFailure(parser, " archived/ all/", ListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " all/ archived/", ListCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_flagWithValue_namesTheFlag() {
        assertParseFailure(parser, " archived/yes", String.format(ParserUtil.MESSAGE_FLAG_TAKES_NO_VALUE, "archived/"));
        assertParseFailure(parser, " all/x", String.format(ParserUtil.MESSAGE_FLAG_TAKES_NO_VALUE, "all/"));
    }

    @Test
    public void parse_invalidOrEmptyFilters_showsTheMessageOfTheField() {
        assertParseFailure(parser, " l/", "Empty value for l/. Enter a value after this prefix.");
        assertParseFailure(parser, " s/", "Empty value for s/. Enter a value after this prefix.");
        assertParseFailure(parser, " l/S9", Level.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " s/Math!", Subject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedFilter_showsDuplicatePrefixError() {
        assertParseFailure(parser, " l/S3 l/S4", duplicateError("l/"));
        assertParseFailure(parser, " s/Math s/Art", duplicateError("s/"));
        assertParseFailure(parser, " all/ all/", duplicateError("all/"));
    }

    private static String duplicateError(String prefix) {
        return "Multiple values specified for the following single-valued field(s): " + prefix;
    }
}
