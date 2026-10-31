package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.WithdrawCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.finance.LedgerEntry;

/** Parses the displayed student index and a positive decimal withdrawal amount. */
public class WithdrawCommandParser implements Parser<WithdrawCommand> {
    private final Clock clock;

    /** Creates a parser that reads today's date from the system clock. */
    public WithdrawCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a parser that uses {@code clock} for relative dates and the default date. */
    public WithdrawCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public WithdrawCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_AMOUNT,
                PREFIX_DATE, PREFIX_NOTES);
        arguments.verifyNoUnexpectedPrefixes(List.of(PREFIX_NOTES), List.of(PREFIX_NOTES));
        ParserUtil.requirePrefixesPresent(arguments, WithdrawCommand.MESSAGE_USAGE, PREFIX_STUDENT, PREFIX_AMOUNT);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_AMOUNT, PREFIX_DATE, PREFIX_NOTES);
        arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_AMOUNT, PREFIX_DATE, PREFIX_NOTES);
        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_STUDENT).orElseThrow(),
                WithdrawCommand.MESSAGE_USAGE);
        String amountText = arguments.getValue(PREFIX_AMOUNT).orElseThrow();
        if (!amountText.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new ParseException(WithdrawCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        BigDecimal amount = new BigDecimal(amountText);
        if (amount.signum() <= 0) {
            throw new ParseException(WithdrawCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        LocalDate date = ParserUtil.parseOptional(arguments, PREFIX_DATE,
                value -> ParserUtil.parseDate(value, LocalDate.now(clock)).value);
        String notes = arguments.getValue(PREFIX_NOTES).map(value -> value.strip().replaceAll(" {2,}", " "))
                .orElse(null);
        if (notes != null && !LedgerEntry.isValidNotes(notes)) {
            throw new ParseException(LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
        }
        return new WithdrawCommand(index, amount, date, notes, clock);
    }
}
