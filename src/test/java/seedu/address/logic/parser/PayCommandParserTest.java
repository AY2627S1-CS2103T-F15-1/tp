package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.commands.PaymentListCommand;
import seedu.address.logic.commands.PaymentReverseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class PayCommandParserTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));
    private final PayCommandParser parser = new PayCommandParser(CLOCK);

    @Test
    public void parse_list_returnsListCommand() throws Exception {
        assertEquals(new PaymentListCommand(null), parser.parse(" list"));
        assertEquals(new PaymentListCommand(null), parser.parse("   list   "));
    }

    @Test
    public void parse_listForStudent_returnsListCommandWithIndex() throws Exception {
        assertEquals(new PaymentListCommand(INDEX_SECOND_PERSON), parser.parse(" list st/2"));
    }

    @Test
    public void parse_listWithBadArguments_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse(" list 2"));
        assertThrows(ParseException.class, () -> parser.parse(" list st/"));
        assertThrows(ParseException.class, () -> parser.parse(" list st/0"));
        assertThrows(ParseException.class, () -> parser.parse(" list st/1 st/2"));
    }

    @Test
    public void parse_reverse_returnsReverseCommand() throws Exception {
        assertEquals(new PaymentReverseCommand(INDEX_FIRST_PERSON, CLOCK), parser.parse(" reverse 1"));
    }

    @Test
    public void parse_reverseWithBadIndex_throwsInvalidFormat() {
        for (String input : new String[] {" reverse", " reverse x", " reverse 0", " reverse 1 2", " reverse -1"}) {
            ParseException e = assertThrows(ParseException.class, () -> parser.parse(input));
            assertTrue(e.getMessage().contains(PaymentReverseCommand.MESSAGE_USAGE), input);
        }
    }

    @Test
    public void parse_payment_stillRecordsAPayment() throws Exception {
        assertTrue(parser.parse(" st/1 amt/50") instanceof PaymentCommand);
    }

    @Test
    public void parse_wordsThatOnlyStartLikeSubcommands_areTreatedAsPaymentArguments() {
        assertThrows(ParseException.class, () -> parser.parse(" listing"));
        assertThrows(ParseException.class, () -> parser.parse(" reversed 1"));
    }

    @Test
    public void parse_empty_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse(""));
    }

    @Test
    public void parseCommand_viaAddressBookParser_routesPay() throws Exception {
        AddressBookParser addressBookParser = new AddressBookParser(CLOCK);
        assertEquals(new PaymentListCommand(null), addressBookParser.parseCommand("pay list"));
        assertEquals(new PaymentReverseCommand(INDEX_SECOND_PERSON, CLOCK),
                addressBookParser.parseCommand("pay reverse 2"));
    }
}
