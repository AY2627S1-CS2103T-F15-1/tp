package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.finance.LedgerEntry;

public class PaymentCommandParserTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private final PaymentCommandParser parser = new PaymentCommandParser(CLOCK);

    @Test
    public void parse_validArguments_acceptsEitherOrder() {
        PaymentCommand expected = new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("50.00"), null, null, CLOCK);
        assertParseSuccess(parser, " st/1 amt/50", expected);
        assertParseSuccess(parser, " amt/50.00 st/1", expected);
        assertParseSuccess(parser, " st/1   amt/ 50.00 ", expected);
        assertParseSuccess(parser, " st/1 amt/0.01",
                new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("0.01"), null, null, CLOCK));
        assertParseSuccess(parser, " st/1 amt/100000",
                new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("100000"), null, null, CLOCK));
    }

    @Test
    public void parse_missingEmptyRepeatedOrExtraArguments_rejected() {
        for (String args : new String[]{"", " st/1", " amt/50", " st/ amt/50", " st/1 amt/",
            " unwanted st/1 amt/50", " st/1 amt/50 d/invalid", " st/1 extra amt/50"}) {
            assertThrows(ParseException.class, () -> parser.parse(args));
        }
        assertParseFailure(parser, " st/1 st/2 amt/50",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STUDENT));
        assertParseFailure(parser, " st/1 amt/50 amt/20",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AMOUNT));
    }

    @Test
    public void parse_invalidIndex_rejected() {
        for (String index : new String[]{"0", "-1", "1.5", "abc", "2147483648"}) {
            assertParseFailure(parser, " st/" + index + " amt/50",
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, PaymentCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidAmount_rejected() {
        for (String amount : new String[]{"0", "0.00", "-1", "1.001", "1e3", "NaN", "Infinity", "1.",
            ".50", "$", "1,000", "1 0", "$50.00"}) {
            assertParseFailure(parser, " st/1 amt/" + amount, PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
    }

    @Test
    public void parse_largeAmount_accepted() {
        // A large amount is only warned about when the payment is recorded, so the parser has no upper limit
        for (String amount : new String[]{"10000", "100000000.50", "99999999999999999999"}) {
            assertParseSuccess(parser, " st/1 amt/" + amount,
                    new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal(amount), null, null, CLOCK));
        }
    }

    @Test
    public void parse_optionalDatesAndNotes_acceptsSpecifiedFormatsAndTrimsNotes() {
        PaymentCommand expected = new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN,
                LocalDate.of(2026, 10, 18), "PayNow ref 4471", CLOCK);
        assertParseSuccess(parser, " st/1 amt/10 d/2026-10-18 n/PayNow  ref 4471", expected);
        assertParseSuccess(parser, " n/PayNow ref 4471 d/18/10/2026 amt/10 st/1", expected);
        assertParseSuccess(parser, " st/1 amt/10 d/TODAY", new PaymentCommand(INDEX_FIRST_PERSON,
                BigDecimal.TEN, LocalDate.of(2026, 10, 19), null, CLOCK));
        assertParseSuccess(parser, " st/1 amt/10 d/tmr", new PaymentCommand(INDEX_FIRST_PERSON,
                BigDecimal.TEN, LocalDate.of(2026, 10, 20), null, CLOCK));
    }

    @Test
    public void parse_invalidDatesAndNotesOrRepeatedOptionalFields_rejected() {
        for (String date : new String[]{"2026-02-30", "30/02/2026", "yesterday", "19/10"}) {
            assertThrows(ParseException.class, () -> parser.parse(" st/1 amt/10 d/" + date));
        }
        assertParseFailure(parser, " st/1 amt/10 n/" + "a".repeat(201), LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
        assertThrows(ParseException.class, () -> parser.parse(" st/1 amt/10 n/"));
        assertThrows(ParseException.class, () -> parser.parse(" st/1 amt/10 n/one n/two"));
        assertThrows(ParseException.class, () -> parser.parse(" st/1 amt/10 d/today d/today"));
        assertThrows(ParseException.class, () -> parser.parse(" st/1 amt/10 d/"));
    }

    @Test
    public void parse_legacySyntax_rejected() {
        assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("payment st/1 a/10"));
        assertThrows(ParseException.class, () -> parser.parse(" st/1 a/10"));
    }

    @Test
    public void parse_singleDigitDates_matchesSharedParser() {
        for (String date : new String[]{"5/10/2026", "05/10/2026", "5/1/2026", "2026-01-05"}) {
            assertParseSuccess(parser, " st/1 amt/10 d/" + date, new PaymentCommand(INDEX_FIRST_PERSON,
                    BigDecimal.TEN, new seedu.address.model.lesson.LessonDate(date).value, null, CLOCK));
        }
    }

    @Test
    public void parse_defaultConstructor_acceptsPaymentWithoutOptionalDate() throws Exception {
        PaymentCommand parsed = new PaymentCommandParser().parse(" st/1 amt/10");

        assertEquals(new PaymentCommand(INDEX_FIRST_PERSON, BigDecimal.TEN), parsed);
    }
}
