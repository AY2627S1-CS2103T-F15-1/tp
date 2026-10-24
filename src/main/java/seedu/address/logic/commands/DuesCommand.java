package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;

/** Shows all students who owe money, independently of the displayed student filter. */
public class DuesCommand extends Command {
    public static final String COMMAND_WORD = "dues";
    public static final String MESSAGE_USAGE = "dues: Lists all students with outstanding balances.\nExample: dues";
    public static final String MESSAGE_NO_DUES = "No outstanding balances. Everyone is up to date.";
    private final Clock clock;

    /** Uses the local system date to display unpaid ages. */
    public DuesCommand() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an explicit date source to make unpaid ages deterministic. */
    public DuesCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(Ledger.formatDues(model.getAddressBook(), LocalDate.now(clock)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DuesCommand command && clock.equals(command.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
