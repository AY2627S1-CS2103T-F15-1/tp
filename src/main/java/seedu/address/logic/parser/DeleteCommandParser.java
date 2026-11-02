package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    private final Clock clock;

    /** Uses the local system date. */
    public DeleteCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an explicit date source for upcoming lessons and homework. */
    public DeleteCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        ConfirmableIndex target = ConfirmableIndex.parse(args, DeleteCommand.CONFIRM_KEYWORD,
                DeleteCommand.MESSAGE_USAGE);
        return new DeleteCommand(target.getIndex(), target.isConfirmed(), clock);
    }

}
