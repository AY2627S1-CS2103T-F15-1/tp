package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.PayListCommand;
import seedu.address.logic.commands.PayReverseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses payment recording, stable indexed listing, and explicit reversals. */
public class PayCommandParser implements Parser<Command> {
    private static final Prefix STUDENT = new Prefix("st/");
    private static final Prefix AMOUNT = new Prefix("amt/");
    private static final Prefix DATE = new Prefix("d/");
    private static final Prefix NOTES = new Prefix("n/");
    private final Clock clock;

    /** Uses the local system calendar. */
    public PayCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injected clock for date aliases and the future-date boundary. */
    public PayCommandParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Command parse(String args) throws ParseException {
        ParserUtil.requirePrintable(args, "Commands must contain printable text on one line.");
        String[] parts = args.strip().split("\\s+", 2);
        String remaining = parts.length == 2 ? parts[1] : "";
        if (parts[0].equals("reverse")) {
            return new PayReverseCommand(ParserUtil.parseIndex(remaining), clock);
        }
        if (parts[0].equals("list")) {
            ArgumentMultimap values = arguments(remaining, STUDENT);
            return new PayListCommand(ParserUtil.parseOptional(values, STUDENT, ParserUtil::parseIndex));
        }
        ArgumentMultimap values = arguments(args, STUDENT, AMOUNT, DATE, NOTES);
        if (values.getValue(STUDENT).isEmpty() || values.getValue(AMOUNT).isEmpty()) {
            throw new ParseException(PayCommand.MESSAGE_USAGE);
        }
        String notes = values.getValue(NOTES).orElse(null);
        if (notes != null && notes.length() > 200) {
            throw new ParseException("Payment notes must be 1-200 printable characters.");
        }
        LocalDate date = values.getValue(DATE).isEmpty() ? LocalDate.now(clock)
                : DateParser.parse(values.getValue(DATE).orElseThrow(), clock);
        return new PayCommand(ParserUtil.parseIndex(values.getValue(STUDENT).orElseThrow()),
                FinanceParserUtil.parseAmount(values.getValue(AMOUNT).orElseThrow()), date, notes, clock);
    }

    /** Requires only recognized prefix arguments, each with exactly one nonempty value. */
    private ArgumentMultimap arguments(String args, Prefix... prefixes) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + args.strip(), prefixes);
        values.verifyNoDuplicatePrefixesFor(prefixes);
        values.verifyNoEmptyValuesFor(prefixes);
        if (!values.getPreamble().isEmpty()) {
            throw new ParseException(PayCommand.MESSAGE_USAGE + " | " + PayListCommand.MESSAGE_USAGE
                    + " | " + PayReverseCommand.MESSAGE_USAGE);
        }
        return values;
    }
}
