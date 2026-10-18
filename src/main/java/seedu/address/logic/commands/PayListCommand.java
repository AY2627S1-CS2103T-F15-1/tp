package seedu.address.logic.commands;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.FinanceQueries;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.finance.Money;

/** Displays a frozen numbered payment list; history views never change this selection. */
public class PayListCommand extends Command {
    public static final String MESSAGE_USAGE = "pay list [st/STUDENT_INDEX]";
    private final Index studentIndex;

    /** Creates a list for every student or one displayed student, including archived students. */
    public PayListCommand(Index studentIndex) {
        this.studentIndex = studentIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        UUID studentId = studentIndex == null ? null : FinanceCommandSupport.student(model, studentIndex).getId();
        List<LedgerEntry> rows = model.getAddressBook().getLedgerEntries().stream()
                .filter(entry -> entry.kind() == LedgerEntry.Kind.PAYMENT)
                .filter(entry -> studentId == null || entry.studentId().equals(studentId))
                .sorted(Comparator.comparing(LedgerEntry::date)).toList();
        StringBuilder text = new StringBuilder(rows.isEmpty() ? "No payments recorded." : "PAYMENTS");
        for (int i = 0; i < rows.size(); i++) {
            LedgerEntry entry = rows.get(i);
            text.append("\n").append(i + 1).append(". ").append(entry.date()).append(" ")
                    .append(FinanceQueries.studentLabel(model.getAddressBook(), entry.studentId())).append(" ")
                    .append(Money.format(entry.amount()));
            if (entry.description() != null) {
                text.append(" — ").append(entry.description());
            }
            if (Ledger.isReversed(model.getAddressBook(), entry.id())) {
                text.append(" [REVERSED]");
            }
        }
        text.append("\nUse 'pay reverse INDEX' with these rows. Indices stay fixed until the next 'pay list'.");
        model.showPayments(rows);
        return new CommandResult(text.toString()).withDisplayedPayments(rows);
    }
}
