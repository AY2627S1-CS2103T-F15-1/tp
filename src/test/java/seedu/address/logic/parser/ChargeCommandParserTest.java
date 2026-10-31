package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigDecimal;
import java.time.Clock;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.commands.PaymentCommand;

public class ChargeCommandParserTest {
    private final ChargeCommandParser parser = new ChargeCommandParser(Clock.systemDefaultZone());
    private final String invalidFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ChargeCommand.MESSAGE_USAGE);

    @Test
    public void parse_charge_acceptsAnOptionalAmountInEitherOrder() {
        assertParseSuccess(parser, " l/1", new ChargeCommand(INDEX_FIRST_PERSON, null));
        assertParseSuccess(parser, " l/1 amt/50", new ChargeCommand(INDEX_FIRST_PERSON, new BigDecimal("50")));
        assertParseSuccess(parser, " amt/75.50 l/1", new ChargeCommand(INDEX_FIRST_PERSON, new BigDecimal("75.50")));
    }

    @Test
    public void parse_reverse_takesOnlyALessonIndex() {
        ChargeCommand expected = new ChargeCommand(INDEX_FIRST_PERSON, null, true, Clock.systemDefaultZone());
        assertParseSuccess(parser, " reverse l/1", expected);
        assertParseSuccess(parser, "   reverse   l/1  ", expected);
        assertParseFailure(parser, " reverse l/1 amt/5", "Unknown prefix for this command: amt/.");
    }

    @Test
    public void parse_missingOrMalformedArguments_rejected() {
        assertParseFailure(parser, "", "Missing required parameter(s): l/\n" + ChargeCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " reverse",
                "Missing required parameter(s): l/\n" + ChargeCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " l/0", invalidFormat);
        assertParseFailure(parser, " l/x", invalidFormat);
        assertParseFailure(parser, " 1", "Missing required parameter(s): l/\n" + ChargeCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_unknownSubcommand_listsAcceptedChargeForms() {
        for (String command : new String[]{" junk l/1", " reversed l/1", " revrse l/1", " reverseX l/1"}) {
            assertParseFailure(parser, command, ChargeCommand.MESSAGE_SUBCOMMANDS);
        }
    }

    @Test
    public void parse_badAmountOrRepeatedPrefix_rejected() {
        for (String amount : new String[]{"0", "-5", "1.234", "abc", "$5", ".5"}) {
            assertParseFailure(parser, " l/1 amt/" + amount, PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        assertParseFailure(parser, " l/1 l/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_LESSON_INDEX));
        assertParseFailure(parser, " l/1 amt/5 amt/6",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_AMOUNT));
        assertParseFailure(parser, " l/ amt/5", "Empty value for l/. Enter a value after this prefix.");
    }
}
