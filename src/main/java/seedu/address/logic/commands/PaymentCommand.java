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

/** Records money received from a student and shows the updated amount owed or credit. */
public class PaymentCommand extends Command {
    public static final String COMMAND_WORD = "pay";
    public static final String MESSAGE_USAGE = "pay: Records a payment from a student in the displayed list.\n"
            + "Parameters: st/STUDENT_INDEX amt/AMOUNT [d/DATE] [n/NOTES]\nExample: pay st/1 amt/50.00";
    public static final String MESSAGE_AMOUNT_CONSTRAINTS =
            "Amount must be greater than 0 with at most 2 decimal places.";
    /**
     * A payment of at least this amount is still recorded, but the reply asks the user to check it, because a
     * single payment this large is more likely to be a mistyped amount with extra digits than a real one.
     */
    public static final BigDecimal LARGE_AMOUNT = new BigDecimal("10000.00");
    public static final String MESSAGE_LARGE_AMOUNT =
            "Warning: %1$s is an unusually large payment. Check that the amount has no extra digits.";
    public static final String MESSAGE_FUTURE_DATE = "Payment dates cannot be in the future.";

    private final Index studentIndex;
    private final BigDecimal amount;
    private final LocalDate date;
    private final String notes;
    private final Clock clock;

    /** Creates a payment for the displayed student index, using an exact positive monetary amount. */
    public PaymentCommand(Index studentIndex, BigDecimal amount) {
        this(studentIndex, amount, null, null, Clock.systemDefaultZone());
    }

    /** Creates a payment with optional date/notes and an injectable clock for default dates and validation. */
    public PaymentCommand(Index studentIndex, BigDecimal amount, LocalDate date, String notes, Clock clock) {
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
        LocalDate paymentDate = date == null ? today : date;
        if (paymentDate.isAfter(today)) {
            throw new CommandException(MESSAGE_FUTURE_DATE);
        }
        try {
            model.addLedgerEntry(LedgerEntry.payment(student, amount, paymentDate, notes));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        BigDecimal balance = Ledger.balance(model.getAddressBook(), student);
        String feedback = "Payment of " + Ledger.format(amount) + " recorded for " + student.getName()
                + " on " + paymentDate + ".\nBalance: " + Ledger.describeBalance(balance) + ".";
        if (amount.compareTo(LARGE_AMOUNT) >= 0) {
            feedback += "\n" + String.format(MESSAGE_LARGE_AMOUNT, Ledger.format(amount));
        }
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PaymentCommand command
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
