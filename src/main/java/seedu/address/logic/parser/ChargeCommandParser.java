package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_INDEX;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code charge l/INDEX [amt/AMOUNT]} and {@code charge reverse l/INDEX}. */
public class ChargeCommandParser implements Parser<ChargeCommand> {
    private final Clock clock;

    /** Uses the local system date for the date of a reversal. */
    public ChargeCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a deterministic date source. */
    public ChargeCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ChargeCommand parse(String args) throws ParseException {
        String trimmed = args.strip();
        String firstWord = CommandInput.split(trimmed).map(CommandInput::getWord).orElse("");
        boolean isReversal = firstWord.equals(ChargeCommand.REVERSE_KEYWORD);
        if (firstWord.matches("\\p{L}+") && !isReversal) {
            throw new ParseException(ChargeCommand.MESSAGE_SUBCOMMANDS);
        }
        if (isReversal) {
            trimmed = trimmed.substring(ChargeCommand.REVERSE_KEYWORD.length());
        }
        ArgumentMultimap arguments = isReversal
                ? ArgumentTokenizer.tokenize(" " + trimmed, PREFIX_LESSON_INDEX)
                : ArgumentTokenizer.tokenize(" " + trimmed, PREFIX_LESSON_INDEX, PREFIX_AMOUNT);
        arguments.verifyNoUnexpectedPrefixes(List.of(), List.of());
        ParserUtil.requirePrefixesPresent(arguments, ChargeCommand.MESSAGE_USAGE, PREFIX_LESSON_INDEX);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_LESSON_INDEX, PREFIX_AMOUNT);
        arguments.verifyNoEmptyValuesFor(PREFIX_LESSON_INDEX, PREFIX_AMOUNT);
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getValue(PREFIX_LESSON_INDEX).orElseThrow());
        } catch (ParseException e) {
            throw ParserUtil.invalidFormat(ChargeCommand.MESSAGE_USAGE);
        }
        BigDecimal amount = null;
        if (arguments.getValue(PREFIX_AMOUNT).isPresent()) {
            String amountText = arguments.getValue(PREFIX_AMOUNT).get();
            if (!amountText.matches("[0-9]+(?:\\.[0-9]{1,2})?") || new BigDecimal(amountText).signum() <= 0) {
                throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
            }
            amount = new BigDecimal(amountText);
        }
        return new ChargeCommand(index, amount, isReversal, clock);
    }
}
