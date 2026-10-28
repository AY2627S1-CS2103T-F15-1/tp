package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.finance.LedgerEntry;

/** Parses the displayed student index and a positive decimal payment amount. */
public class PaymentCommandParser implements Parser<PaymentCommand> {
    private final Clock clock;

    /** Uses the specified pay/amt syntax and the local system date. */
    public PaymentCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a deterministic date source. */
    public PaymentCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);

    }

    @Override
    public PaymentCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_AMOUNT,
                PREFIX_DATE, PREFIX_NOTES);
        ParserUtil.requirePrefixesPresent(arguments, PaymentCommand.MESSAGE_USAGE, PREFIX_STUDENT, PREFIX_AMOUNT);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_AMOUNT, PREFIX_DATE, PREFIX_NOTES);
        arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_AMOUNT, PREFIX_DATE, PREFIX_NOTES);
        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_STUDENT).orElseThrow(),
                PaymentCommand.MESSAGE_USAGE);
        String amountText = arguments.getValue(PREFIX_AMOUNT).orElseThrow();
        if (!amountText.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        BigDecimal amount = new BigDecimal(amountText);
        if (amount.signum() <= 0) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        LocalDate date = ParserUtil.parseOptional(arguments, PREFIX_DATE,
                value -> ParserUtil.parseDate(value, LocalDate.now(clock)).value);
        String notes = arguments.getValue(PREFIX_NOTES).map(value -> value.strip().replaceAll(" {2,}", " "))
                .orElse(null);
        if (notes != null && !LedgerEntry.isValidNotes(notes)) {
            throw new ParseException(LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
        }
        return new PaymentCommand(index, amount, date, notes, clock);
    }

}
