package seedu.address.logic.parser;

import java.math.BigDecimal;
import java.time.Clock;

import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses explicit charge amounts and charge reversals. */
public class ChargeCommandParser implements Parser<ChargeCommand> {
    private static final Prefix LESSON = new Prefix("l/");
    private static final Prefix AMOUNT = new Prefix("amt/");
    private final Clock clock;

    /** Uses the application's local calendar day. */
    public ChargeCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an explicit clock for deterministic date guards. */
    public ChargeCommandParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ChargeCommand parse(String args) throws ParseException {
        ParserUtil.requirePrintable(args, "Commands must contain printable text on one line.");
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + args.strip(), LESSON, AMOUNT);
        values.verifyNoDuplicatePrefixesFor(LESSON, AMOUNT);
        values.verifyNoEmptyValuesFor(LESSON, AMOUNT);
        boolean reverse = values.getPreamble().equals("reverse");
        if ((!values.getPreamble().isEmpty() && !reverse) || values.getValue(LESSON).isEmpty()
                || (reverse && values.getValue(AMOUNT).isPresent())) {
            throw new ParseException(ChargeCommand.MESSAGE_USAGE);
        }
        BigDecimal amount = values.getValue(AMOUNT).isPresent()
                ? FinanceParserUtil.parseAmount(values.getValue(AMOUNT).orElseThrow()) : null;
        return new ChargeCommand(ParserUtil.parseIndex(values.getValue(LESSON).orElseThrow()), amount, reverse, clock);
    }
}
