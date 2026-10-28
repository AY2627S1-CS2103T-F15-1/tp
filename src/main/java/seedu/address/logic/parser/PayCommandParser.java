package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.PaymentListCommand;
import seedu.address.logic.commands.PaymentReverseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code pay}, which records a payment, or is followed by {@code list} or {@code reverse}.
 */
public class PayCommandParser implements Parser<Command> {
    private static final Pattern SUB_COMMAND_FORMAT = Pattern.compile("(?<word>\\S+)(?<arguments>.*)", Pattern.DOTALL);

    private final Clock clock;

    /** Creates a parser that reads today's date from {@code clock}. */
    public PayCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Command parse(String args) throws ParseException {
        Matcher matcher = SUB_COMMAND_FORMAT.matcher(args.strip());
        String subCommandWord = matcher.matches() ? matcher.group("word") : "";
        return switch (subCommandWord) {
            case PaymentListCommand.LIST_KEYWORD -> new PaymentListCommandParser().parse(matcher.group("arguments"));
            case PaymentReverseCommand.REVERSE_KEYWORD ->
                new PaymentReverseCommandParser(clock).parse(matcher.group("arguments"));
            default -> new PaymentCommandParser(clock).parse(args);
        };
    }
}
