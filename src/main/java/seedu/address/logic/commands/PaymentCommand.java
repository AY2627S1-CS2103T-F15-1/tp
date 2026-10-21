package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;

/** Records money received from a student and shows the updated amount owed or credit. */
public class PaymentCommand extends Command {
    public static final String COMMAND_WORD = "payment";
    public static final String MESSAGE_USAGE = "payment: Records a payment from a student in the displayed list.\n"
            + "Parameters: st/STUDENT_INDEX a/AMOUNT\nExample: payment st/1 a/50.00";
    public static final String MESSAGE_AMOUNT_CONSTRAINTS =
            "Payment amount must be greater than zero with at most two decimal places, e.g. 50 or 62.50.";

    private final Index studentIndex;
    private final BigDecimal amount;

    /** Creates a payment for the displayed student index, using an exact positive monetary amount. */
    public PaymentCommand(Index studentIndex, BigDecimal amount) {
        requireAllNonNull(studentIndex, amount);
        if (amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException(MESSAGE_AMOUNT_CONSTRAINTS);
        }
        this.studentIndex = studentIndex;
        this.amount = amount.setScale(2);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.invalidPersonIndex(studentIndex, students.size()));
        }
        Person student = students.get(studentIndex.getZeroBased());
        try {
            model.addLedgerEntry(LedgerEntry.payment(student, amount, LocalDate.now()));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        BigDecimal balance = Ledger.balance(model.getAddressBook(), student);
        return new CommandResult("Recorded payment of " + Ledger.format(amount) + " from " + student.getName()
                + " (phone: " + student.getPhone() + ").\n" + Ledger.describeBalance(balance));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PaymentCommand command
                && studentIndex.equals(command.studentIndex) && amount.equals(command.amount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("amount", amount).toString();
    }
}
