package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validRemark_success() throws Exception {
        RemarkCommand expected = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming"));
        assertParseSuccess(parser, " 1 r/Likes swimming", expected);
        assertEquals(expected, new AddressBookParser().parseCommand("remark 1 r/Likes swimming"));
    }

    @Test
    public void parse_emptyOrOmittedRemark_clearsRemark() {
        RemarkCommand expected = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, " 1 r/", expected);
        assertParseSuccess(parser, " 1", expected);
    }

    @Test
    public void parse_invalidIndex_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " r/hello", "0 r/hello", "-1 r/hello", "one r/hello",
            "1.5 r/hello", "2147483648 r/hello"}) {
            assertParseFailure(parser, args, message);
        }
    }
}
