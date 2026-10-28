package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PaymentReverseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code pay reverse PAYMENT_INDEX}. */
public class PaymentReverseCommandParser implements Parser<PaymentReverseCommand> {
    private final Clock clock;

    /** Dates the reversal with {@code clock}. */
    public PaymentReverseCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PaymentReverseCommand parse(String args) throws ParseException {
        Index index;
        try {
            index = ParserUtil.parseIndex(args.strip());
        } catch (ParseException e) {
            throw ParserUtil.invalidFormat(PaymentReverseCommand.MESSAGE_USAGE);
        }
        return new PaymentReverseCommand(index, clock);
    }
}
