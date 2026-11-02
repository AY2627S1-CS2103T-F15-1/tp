package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ViewCommand object
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    private final Clock clock;

    /** Uses the local system date. */
    public ViewCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an explicit date source for today's date. */
    public ViewCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses the given {@code String} of arguments in the context of the ViewCommand
     * and returns a ViewCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public ViewCommand parse(String args) throws ParseException {
        return new ViewCommand(ParserUtil.parseIndex(args, ViewCommand.MESSAGE_USAGE), LocalDate.now(clock));
    }

}
