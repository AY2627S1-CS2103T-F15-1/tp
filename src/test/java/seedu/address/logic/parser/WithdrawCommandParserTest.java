package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.WithdrawCommand;
import seedu.address.model.finance.LedgerEntry;

public class WithdrawCommandParserTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private final WithdrawCommandParser parser = new WithdrawCommandParser(CLOCK);

    @Test
    public void parse_validArguments_acceptsAnyPrefixOrder() {
        WithdrawCommand expected = new WithdrawCommand(INDEX_FIRST_PERSON, new BigDecimal("20.00"),
                null, null, CLOCK);
        assertParseSuccess(parser, " st/1 amt/20", expected);
        assertParseSuccess(parser, " amt/20.00 st/1", expected);
        assertParseSuccess(parser, " st/1 amt/20 d/2026-10-18 n/cash", new WithdrawCommand(
                INDEX_FIRST_PERSON, new BigDecimal("20"), LocalDate.of(2026, 10, 18), "cash", CLOCK));
    }

    @Test
    public void parse_missingEmptyDuplicateOrExtraArguments_rejected() {
        for (String args : new String[]{"", " st/1", " amt/20"}) {
            seedu.address.logic.parser.exceptions.ParseException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(
                            seedu.address.logic.parser.exceptions.ParseException.class, () -> parser.parse(args));
            assertTrue(exception.getMessage().startsWith("Missing required parameter(s):"), exception.getMessage());
        }
        for (String args : new String[]{" st/ amt/20", " st/1 amt/"}) {
            seedu.address.logic.parser.exceptions.ParseException exception =
                    org.junit.jupiter.api.Assertions.assertThrows(
                            seedu.address.logic.parser.exceptions.ParseException.class, () -> parser.parse(args));
            assertTrue(exception.getMessage().startsWith("Empty value for "), exception.getMessage());
        }
        for (String args : new String[]{" unwanted st/1 amt/20", " st/1 extra amt/20"}) {
            assertParseFailure(parser, args,
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, WithdrawCommand.MESSAGE_USAGE));
        }
        for (String args : new String[]{" st/1 amt/20 d/", " st/1 amt/20 n/"}) {
            assertParseFailure(parser, args, args.contains(" d/")
                    ? "Empty value for d/. Enter a value after this prefix."
                    : "Empty value for n/. Enter a value after this prefix.");
        }
        assertParseFailure(parser, " st/1 st/2 amt/20",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STUDENT));
        assertParseFailure(parser, " st/1 amt/20 amt/10",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AMOUNT));
    }

    @Test
    public void parse_invalidIndex_rejected() {
        for (String index : new String[]{"0", "-1", "1.5", "abc"}) {
            assertParseFailure(parser, " st/" + index + " amt/20",
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, WithdrawCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidAmount_rejected() {
        for (String amount : new String[]{"0", "0.00", "-1", "1.001", "1e3", "NaN", "Infinity", "1.",
            ".50", "$50.00", "1,000", "1 0"}) {
            assertParseFailure(parser, " st/1 amt/" + amount, WithdrawCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidDateOrNotes_rejected() {
        assertParseFailure(parser, " st/1 amt/20 d/2026-02-30",
                seedu.address.model.lesson.LessonDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " st/1 amt/20 n/" + "a".repeat(201),
                LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
    }
}
