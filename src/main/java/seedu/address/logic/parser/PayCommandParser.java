package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.PaymentListCommand;
import seedu.address.logic.commands.PaymentReverseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of {@code pay}, which records a payment, or is followed by {@code list} or {@code reverse}.
 */
public class PayCommandParser implements Parser<Command> {
    private final Clock clock;

    /** Creates a parser that reads today's date from {@code clock}. */
    public PayCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Command parse(String args) throws ParseException {
        Optional<CommandInput> input = CommandInput.split(args);
        String subCommandWord = input.map(CommandInput::getWord).orElse("");
        return switch (subCommandWord) {
            case PaymentListCommand.LIST_KEYWORD -> new PaymentListCommandParser().parse(input.get().getArguments());
            case PaymentReverseCommand.REVERSE_KEYWORD ->
                new PaymentReverseCommandParser(clock).parse(input.get().getArguments());
            default -> new PaymentCommandParser(clock).parse(args);
        };
    }
}
