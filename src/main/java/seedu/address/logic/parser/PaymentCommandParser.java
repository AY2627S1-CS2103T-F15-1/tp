package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEGACY_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.LessonDate;

/** Parses the displayed student index and a positive decimal payment amount. */
public class PaymentCommandParser implements Parser<PaymentCommand> {
    private final Clock clock;
    private final Prefix amountPrefix;
    private final boolean legacy;

    /** Uses the specified pay/amt syntax and the local system date. */
    public PaymentCommandParser() {
        this(Clock.systemDefaultZone(), false);
    }

    /** Selects canonical or backward-compatible syntax with a deterministic date source. */
    public PaymentCommandParser(Clock clock, boolean legacy) {
        this.clock = Objects.requireNonNull(clock);
        this.legacy = legacy;
        amountPrefix = legacy ? PREFIX_LEGACY_AMOUNT : PREFIX_AMOUNT;
    }

    @Override
    public PaymentCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, amountPrefix,
                PREFIX_DATE, PREFIX_NOTES);
        ParserUtil.requirePrefixesPresent(arguments, PaymentCommand.MESSAGE_USAGE, PREFIX_STUDENT, amountPrefix);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, amountPrefix, PREFIX_DATE, PREFIX_NOTES);
        arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT, amountPrefix, PREFIX_DATE, PREFIX_NOTES);
        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_STUDENT).orElseThrow());
        String amountText = arguments.getValue(amountPrefix).orElseThrow();
        if (!amountText.matches((legacy ? "\\$?" : "") + "[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        BigDecimal amount = new BigDecimal(amountText.startsWith("$") ? amountText.substring(1) : amountText);
        if (amount.signum() <= 0) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        LocalDate date = ParserUtil.parseOptional(arguments, PREFIX_DATE, this::parseDate);
        String notes = arguments.getValue(PREFIX_NOTES).map(value -> value.strip().replaceAll(" {2,}", " "))
                .orElse(null);
        if (notes != null && !LedgerEntry.isValidNotes(notes)) {
            throw new ParseException(LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
        }
        return new PaymentCommand(index, amount, date, notes, clock);
    }

    private LocalDate parseDate(String value) throws ParseException {
        String keyword = value.toLowerCase(Locale.ROOT);
        if (keyword.equals("today") || keyword.equals("tmr")) {
            return LocalDate.now(clock).plusDays(keyword.equals("tmr") ? 1 : 0);
        }
        try {
            return LocalDate.parse(value, value.contains("/")
                    ? DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
                    : DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ParseException(LessonDate.MESSAGE_CONSTRAINTS, e);
        }
    }
}
