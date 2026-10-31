package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.ChargeCommandParser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.PaymentCommandParser;
import seedu.address.logic.parser.RemarkCommandParser;
import seedu.address.logic.parser.WithdrawCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Regression tests for the problems found by the adversarial testing run of 1 Nov 2026.
 */
public class AdversarialFindingsRegressionTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T01:00:00Z"), ZoneId.of("Asia/Singapore"));
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);

    // ---- dues crashed when a debt had no active charge ----

    @Test
    public void dues_debtCreatedByReversingAWithdrawnPayment_doesNotThrow() {
        Person alice = new PersonBuilder().withName("Alice").build();
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        LedgerEntry payment = LedgerEntry.payment(alice, new BigDecimal("100.00"), TODAY);
        book.addLedgerEntry(payment);
        book.addLedgerEntry(LedgerEntry.withdrawal(alice, new BigDecimal("100.00"), TODAY));
        book.addLedgerEntry(LedgerEntry.reversalOf(payment, TODAY));

        assertEquals(new BigDecimal("100.00"), Ledger.balance(book, alice));
        List<Ledger.Due> dues = Ledger.dues(book, TODAY);
        assertEquals(1, dues.size());
        assertEquals(alice, dues.get(0).student());
        assertEquals(TODAY, dues.get(0).oldestUnpaidDate());
        assertTrue(Ledger.formatDues(book, TODAY).contains("$100.00"));
    }

    // ---- non-ASCII digits were accepted as numbers ----

    @Test
    public void isNonZeroUnsignedInteger_nonAsciiDigits_rejected() {
        assertFalse(StringUtil.isNonZeroUnsignedInteger("١")); // Arabic-Indic one
        assertFalse(StringUtil.isNonZeroUnsignedInteger("１")); // full-width one
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1٢"));
        assertTrue(StringUtil.isNonZeroUnsignedInteger("12"));
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("١"));
        assertThrows(ParseException.class, () -> ParserUtil.parseDuration("٦٠"));
    }

    // ---- unbounded amounts ----

    @Test
    public void parseAmount_boundsAndFormat() throws Exception {
        String message = "constraints";
        assertEquals(new BigDecimal("0.01"), ParserUtil.parseAmount("0.01", message));
        assertEquals(new BigDecimal("100000000.50"), ParserUtil.parseAmount("100000000.50", message));
        assertEquals(new BigDecimal("0050"), ParserUtil.parseAmount("0050", message));
        String longest = "9".repeat(LedgerEntry.MAX_AMOUNT_LENGTH);
        assertEquals(new BigDecimal(longest), ParserUtil.parseAmount(longest, message));
        String tooLong = longest + "9";
        String huge = "9".repeat(1_000_000);
        assertEquals(LedgerEntry.MESSAGE_AMOUNT_TOO_LONG,
                assertThrows(ParseException.class, () -> ParserUtil.parseAmount(tooLong, message)).getMessage());
        assertEquals(LedgerEntry.MESSAGE_AMOUNT_TOO_LONG,
                assertThrows(ParseException.class, () -> ParserUtil.parseAmount(huge, message)).getMessage());
        for (String bad : new String[]{"0", "0.00", "-1", "1.001", "1e2", "$5", "", ".5", "5.", "\u0665"}) {
            assertEquals(message, assertThrows(ParseException.class, () -> ParserUtil.parseAmount(bad, message))
                    .getMessage(), bad);
        }
    }

    @Test
    public void ledgerCommandParsers_hugeAmount_rejectedQuickly() {
        String huge = "9".repeat(200_000);
        assertThrows(ParseException.class, () -> new PaymentCommandParser(CLOCK).parse(" st/1 amt/" + huge));
        assertThrows(ParseException.class, () -> new WithdrawCommandParser(CLOCK).parse(" st/1 amt/" + huge));
        assertThrows(ParseException.class, () -> new ChargeCommandParser(CLOCK).parse(" l/1 amt/" + huge));
    }

    // ---- fields without a length limit could make the save file unreadable ----

    @Test
    public void remarkParser_rejectsOverlongRemark() throws Exception {
        RemarkCommandParser parser = new RemarkCommandParser();
        parser.parse("1 r/" + "a".repeat(Remark.MAX_LENGTH));
        String tooLong = "1 r/" + "a".repeat(Remark.MAX_LENGTH + 1);
        ParseException e = assertThrows(ParseException.class, () -> parser.parse(tooLong));
        assertEquals(Remark.MESSAGE_TOO_LONG, e.getMessage());
        assertThrows(ParseException.class, () -> parser.parse("1 r/" + "a".repeat(2_000_000)));
    }

    @Test
    public void parseEmail_rejectsOverlongAddressButStoredOnesStillLoad() throws Exception {
        String longest = "a".repeat(Email.MAX_LENGTH - "@x.com".length()) + "@x.com";
        assertEquals(Email.MAX_LENGTH, ParserUtil.parseEmail(longest).value.length());
        String tooLong = "a" + longest;
        assertEquals(Email.MESSAGE_TOO_LONG,
                assertThrows(ParseException.class, () -> ParserUtil.parseEmail(tooLong)).getMessage());
        // data saved by earlier versions must keep loading
        assertEquals(tooLong, Email.fromStored(tooLong).value);
        assertEquals(1500, new Remark("r".repeat(1500)).value.length());
    }

    // ---- a 52-lesson series printed more than a hundred rows ----

    @Test
    public void formatLessonList_longList_isCapped() {
        Person alice = new PersonBuilder().withName("Alice").build();
        List<Lesson> many = IntStream.range(0, 104)
                .mapToObj(i -> new LessonBuilder().withStudent(alice).withDate(TODAY.plusDays(i + 1)).build()).toList();
        String text = Messages.formatLessonList(many);
        assertEquals(Messages.MAX_RENUMBERED_LESSONS_SHOWN + 1 + 1 + 1, text.split("\n").length - 1);
        assertTrue(text.contains("... and 74 more."));
        assertTrue(text.contains(Messages.MAX_RENUMBERED_LESSONS_SHOWN + ". "));
        assertFalse(text.contains("\n31. "));

        String small = Messages.formatLessonList(many.subList(0, 3));
        assertFalse(small.contains("more."));
        assertTrue(small.contains("\n3. "));
    }
}
