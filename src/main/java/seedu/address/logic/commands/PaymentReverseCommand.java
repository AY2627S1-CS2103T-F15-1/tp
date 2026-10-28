package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;

/**
 * Reverses a payment that was entered wrongly, by adding an opposite entry. The payment itself is kept, so the
 * account shows both the mistake and its correction. A payment is reversed once; to correct a wrongly reversed
 * payment, the payment is recorded again.
 */
public class PaymentReverseCommand extends Command {
    public static final String REVERSE_KEYWORD = "reverse";

    public static final String MESSAGE_USAGE = PaymentCommand.COMMAND_WORD + " " + REVERSE_KEYWORD
            + ": Reverses the payment with the given number from the last " + PaymentCommand.COMMAND_WORD + " "
            + PaymentListCommand.LIST_KEYWORD + ".\n"
            + "Parameters: PAYMENT_INDEX\n"
            + "Example: " + PaymentCommand.COMMAND_WORD + " " + REVERSE_KEYWORD + " 2";
    public static final String MESSAGE_SUCCESS = "Payment of %1$s reversed.";
    public static final String MESSAGE_INVALID_INDEX =
            "Invalid payment index. Run 'pay list' to display payments first.";
    public static final String MESSAGE_ALREADY_REVERSED = "That payment has already been reversed.";

    private final Index paymentIndex;
    private final Clock clock;

    /** Reverses the payment at {@code paymentIndex} of the last payment list, dating the reversal today. */
    public PaymentReverseCommand(Index paymentIndex) {
        this(paymentIndex, Clock.systemDefaultZone());
    }

    /** Creates a command whose reversal is dated by {@code clock}. */
    public PaymentReverseCommand(Index paymentIndex, Clock clock) {
        this.paymentIndex = requireNonNull(paymentIndex);
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<LedgerEntry> shown = model.getShownPayments();
        if (paymentIndex.getZeroBased() >= shown.size()) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        LedgerEntry payment = shown.get(paymentIndex.getZeroBased());
        // A payment can vanish after it was listed, when its student is deleted or all data is cleared
        if (!model.getAddressBook().getLedgerEntries().contains(payment)) {
            throw new CommandException(MESSAGE_INVALID_INDEX);
        }
        if (Ledger.isReversed(model.getAddressBook(), payment)) {
            throw new CommandException(MESSAGE_ALREADY_REVERSED);
        }
        try {
            model.addLedgerEntry(LedgerEntry.reversalOf(payment, LocalDate.now(clock)));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, Ledger.format(payment.amount())));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PaymentReverseCommand command
                && paymentIndex.equals(command.paymentIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentIndex.getOneBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("paymentIndex", paymentIndex).toString();
    }
}
