package seedu.address.logic.commands;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.finance.Money;

/** Appends a reversal against the original payment identity from the last shown payment list. */
public class PayReverseCommand extends Command {
    public static final String MESSAGE_USAGE = "pay reverse PAYMENT_INDEX (from the last 'pay list')";
    private final Index paymentIndex;
    private final Clock clock;

    /** Creates a reversal with a supplied local calendar clock. */
    public PayReverseCommand(Index paymentIndex, Clock clock) {
        this.paymentIndex = paymentIndex;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<LedgerEntry> rows = model.getDisplayedPayments();
        if (paymentIndex.getZeroBased() >= rows.size()) {
            throw new CommandException("Invalid payment index. Run 'pay list' to display payments first.");
        }
        LedgerEntry shown = rows.get(paymentIndex.getZeroBased());
        LedgerEntry original = model.getAddressBook().getLedgerEntries().stream()
                .filter(entry -> entry.id().equals(shown.id())).findFirst()
                .orElseThrow(() -> new CommandException("That payment was deleted. Run 'pay list' to refresh."));
        if (Ledger.isReversed(model.getAddressBook(), original.id())) {
            throw new CommandException("That payment has already been reversed.");
        }
        model.addLedgerEntry(LedgerEntry.reverse(original, LocalDate.now(clock)));
        return new CommandResult("Payment of " + Money.format(original.amount()) + " reversed."
                + "\nPayment indices stay as last shown; run 'pay list' to refresh.");
    }
}
