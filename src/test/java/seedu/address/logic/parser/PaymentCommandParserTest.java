package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class PaymentCommandParserTest {
    private final PaymentCommandParser parser = new PaymentCommandParser();

    @Test
    public void parse_validArguments_acceptsEitherOrderAndOptionalDollarSign() {
        PaymentCommand expected = new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("50.00"));
        assertParseSuccess(parser, " st/1 a/50", expected);
        assertParseSuccess(parser, " a/$50.00 st/1", expected);
        assertParseSuccess(parser, " st/1   a/ 50.00 ", expected);
        assertParseSuccess(parser, " st/1 a/0.01", new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("0.01")));
        assertParseSuccess(parser, " st/1 a/100000", new PaymentCommand(INDEX_FIRST_PERSON, new BigDecimal("100000")));
    }

    @Test
    public void parse_missingEmptyRepeatedOrExtraArguments_rejected() {
        for (String args : new String[]{"", " st/1", " a/50", " st/ a/50", " st/1 a/",
            " unwanted st/1 a/50", " st/1 a/50 d/2026-10-21", " st/1 extra a/50"}) {
            assertThrows(ParseException.class, () -> parser.parse(args));
        }
        assertParseFailure(parser, " st/1 st/2 a/50",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STUDENT));
        assertParseFailure(parser, " st/1 a/50 a/20",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AMOUNT));
    }

    @Test
    public void parse_invalidIndex_rejected() {
        for (String index : new String[]{"0", "-1", "1.5", "abc", "2147483648"}) {
            assertParseFailure(parser, " st/" + index + " a/50", ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidAmount_rejected() {
        for (String amount : new String[]{"0", "0.00", "-1", "1.001", "1e3", "NaN", "Infinity", "1.",
            ".50", "$", "1,000", "1 0"}) {
            assertParseFailure(parser, " st/1 a/" + amount, PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
    }
}
