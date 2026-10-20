package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ArchiveCommand;

public class ArchiveCommandParserTest {

    private final ArchiveCommandParser archiveParser = new ArchiveCommandParser(true);
    private final ArchiveCommandParser restoreParser = new ArchiveCommandParser(false);

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(archiveParser, " 1", new ArchiveCommand(INDEX_FIRST_PERSON, true));
        assertParseSuccess(restoreParser, "  1  ", new ArchiveCommand(INDEX_FIRST_PERSON, false));
    }

    @Test
    public void parse_invalidIndex_showsInvalidFormatWithTheUsageOfTheCommand() {
        String archiveError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ArchiveCommand.MESSAGE_USAGE_ARCHIVE);
        String restoreError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ArchiveCommand.MESSAGE_USAGE_RESTORE);

        for (String input : new String[] {"", " 0", " -1", " abc", " 1 2", " 1.5"}) {
            assertParseFailure(archiveParser, input, archiveError);
            assertParseFailure(restoreParser, input, restoreError);
        }
    }
}
