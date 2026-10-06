package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TodayCommand;

public class TodayCommandParserTest {

    private TodayCommandParser parser = new TodayCommandParser();

    @Test
    public void parse_noArguments_usesToday() {
        assertParseSuccess(parser, "", new TodayCommand(LocalDate.now()));
        assertParseSuccess(parser, "   ", new TodayCommand(LocalDate.now()));
    }

    @Test
    public void parse_extraText_failure() {
        assertParseFailure(parser, " now", TodayCommand.MESSAGE_NO_PARAMETERS);
        assertParseFailure(parser, " d/2030-01-07", TodayCommand.MESSAGE_NO_PARAMETERS);
    }

}
