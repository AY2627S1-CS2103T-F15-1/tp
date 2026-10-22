package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.person.Person;

/** Shows a student's current balance without exposing transaction history. */
public class BalanceCommand extends Command {
    public static final String COMMAND_WORD = "balance";
    public static final String MESSAGE_USAGE = "balance: Shows the balance of a student in the displayed list.\n"
            + "Parameters: st/INDEX (must be a positive integer)\nExample: balance st/1";
    private final Index targetIndex;

    public BalanceCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.invalidPersonIndex(targetIndex, students.size()));
        }
        Person student = students.get(targetIndex.getZeroBased());
        BigDecimal balance = Ledger.balance(model.getAddressBook(), student);
        return new CommandResult("Balance for " + student.getName() + " (phone: " + student.getPhone() + ")\n"
                + Ledger.describeBalance(balance));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BalanceCommand command && targetIndex.equals(command.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
