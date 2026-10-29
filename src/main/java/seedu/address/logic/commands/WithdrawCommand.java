package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;

/** Withdraws available credit from a student's account as an append-only ledger entry. */
public class WithdrawCommand extends Command {
    public static final String COMMAND_WORD = "withdraw";
    public static final String MESSAGE_USAGE = "withdraw: Withdraws credit from a student's account.\n"
            + "Parameters: st/STUDENT_INDEX amt/AMOUNT [d/DATE] [n/NOTES]\n"
            + "Example: withdraw st/1 amt/20.00\n"
            + "The amount cannot exceed the student's available credit.";
    public static final String MESSAGE_AMOUNT_CONSTRAINTS =
            "Amount must be greater than 0 with at most 2 decimal places.";
    public static final String MESSAGE_NO_CREDIT =
            "%1$s has no credit to withdraw. Balance: %2$s.";
    public static final String MESSAGE_EXCEEDS_CREDIT =
            "The withdrawal exceeds %1$s's available credit of %2$s.";
    public static final String MESSAGE_FUTURE_DATE = "Withdrawal dates cannot be in the future.";

    private final Index studentIndex;
    private final BigDecimal amount;
    private final LocalDate date;
    private final String notes;
    private final Clock clock;

    /** Creates a withdrawal for the displayed student, using today's date from the system clock. */
    public WithdrawCommand(Index studentIndex, BigDecimal amount) {
        this(studentIndex, amount, null, null, Clock.systemDefaultZone());
    }

    /** Creates a withdrawal with optional date and notes, using {@code clock} for defaults and checks. */
    public WithdrawCommand(Index studentIndex, BigDecimal amount, LocalDate date, String notes, Clock clock) {
        requireAllNonNull(studentIndex, amount, clock);
        if (amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException(MESSAGE_AMOUNT_CONSTRAINTS);
        }
        this.studentIndex = studentIndex;
        this.amount = amount.setScale(2);
        if (notes != null && !LedgerEntry.isValidNotes(notes)) {
            throw new IllegalArgumentException(LedgerEntry.MESSAGE_NOTES_CONSTRAINTS);
        }
        this.date = date;
        this.notes = notes;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.invalidPersonIndex(studentIndex, students.size()));
        }
        Person student = students.get(studentIndex.getZeroBased());
        LocalDate today = LocalDate.now(clock);
        LocalDate withdrawalDate = date == null ? today : date;
        if (withdrawalDate.isAfter(today)) {
            throw new CommandException(MESSAGE_FUTURE_DATE);
        }

        BigDecimal balanceBefore = Ledger.balance(model.getAddressBook(), student);
        BigDecimal credit = balanceBefore.negate();
        if (credit.signum() <= 0) {
            throw new CommandException(String.format(MESSAGE_NO_CREDIT, student.getName(),
                    Ledger.describeBalance(balanceBefore)));
        }
        if (amount.compareTo(credit) > 0) {
            throw new CommandException(String.format(MESSAGE_EXCEEDS_CREDIT, student.getName(),
                    Ledger.format(credit)));
        }

        try {
            model.addLedgerEntry(LedgerEntry.withdrawal(student, amount, withdrawalDate, notes));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        BigDecimal balanceAfter = Ledger.balance(model.getAddressBook(), student);
        return new CommandResult("Withdrawal of " + Ledger.format(amount) + " recorded for " + student.getName()
                + " on " + withdrawalDate + ".\nBalance: " + Ledger.describeBalance(balanceAfter) + ".");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof WithdrawCommand command
                && studentIndex.equals(command.studentIndex) && amount.equals(command.amount)
                && Objects.equals(date, command.date) && Objects.equals(notes, command.notes)
                && clock.equals(command.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("amount", amount)
                .add("date", date).add("notes", notes).toString();
    }
}
