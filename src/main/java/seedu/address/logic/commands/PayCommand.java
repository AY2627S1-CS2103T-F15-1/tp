package seedu.address.logic.commands;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.FinanceQueries;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.finance.Money;
import seedu.address.model.person.Person;

/** Records a student's payment, including legitimate duplicate transfers and advance credit. */
public class PayCommand extends Command {
    public static final String MESSAGE_USAGE = "pay st/STUDENT_INDEX amt/AMOUNT [d/DATE] [n/NOTES]";
    private final Index studentIndex;
    private final BigDecimal amount;
    private final LocalDate date;
    private final String notes;
    private final Clock clock;

    /** Creates a payment with a resolved date and explicit local calendar clock. */
    public PayCommand(Index studentIndex, BigDecimal amount, LocalDate date, String notes, Clock clock) {
        this.studentIndex = studentIndex;
        this.amount = Money.positive(amount);
        this.date = date;
        this.notes = notes;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person student = FinanceCommandSupport.student(model, studentIndex);
        if (date.isAfter(LocalDate.now(clock))) {
            throw new CommandException("Payment dates cannot be in the future.");
        }
        model.addLedgerEntry(LedgerEntry.payment(student.getId(), amount, date, notes));
        return new CommandResult("Payment of " + Money.format(amount) + " recorded for " + student.getName()
                + " on " + date + ".\nBalance: " + FinanceQueries.balanceLabel(Ledger.balance(model.getAddressBook(),
                        student.getId())) + ".\nPayment indices stay as last shown; run 'pay list' to refresh.");
    }
}
