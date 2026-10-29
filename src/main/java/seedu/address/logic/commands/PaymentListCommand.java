package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;

/**
 * Lists the recorded payments, oldest first, and numbers them for {@code pay reverse}.
 *
 * The numbers stay as shown until the next {@code pay list}, even after payments are added or reversed, so that a
 * number typed after a mistake still refers to the row the user saw. A reversed payment stays in the list, marked.
 */
public class PaymentListCommand extends Command {
    public static final String LIST_KEYWORD = "list";
    public static final int MAX_DISPLAYED_PAYMENTS = 200;

    public static final String MESSAGE_USAGE = PaymentCommand.COMMAND_WORD + " " + LIST_KEYWORD
            + ": Lists recorded payments and numbers them for " + PaymentCommand.COMMAND_WORD + " "
            + PaymentReverseCommand.REVERSE_KEYWORD + ".\n"
            + "Parameters: [st/STUDENT_INDEX]\n"
            + "Example: " + PaymentCommand.COMMAND_WORD + " " + LIST_KEYWORD + " st/2";
    public static final String MESSAGE_HEADER = "PAYMENTS";
    public static final String MESSAGE_FOOTER = "Use 'pay reverse INDEX' with these rows. "
            + "Indices stay fixed until the next 'pay list'.";
    public static final String MESSAGE_REVERSED_MARK = " [REVERSED]";
    public static final String MESSAGE_NO_PAYMENTS = "No payments recorded.";
    public static final String MESSAGE_NO_PAYMENTS_FOR_STUDENT = "No payments recorded for %1$s.";
    public static final String MESSAGE_TRUNCATED = "Showing %1$d of %2$d payments. The remaining payments keep their "
            + "numbers; use 'pay reverse INDEX' to address them.";

    private final Index studentIndex; // null if every student's payments are listed

    /** Creates a command that lists the payments of the student at {@code studentIndex}, or all when it is null. */
    public PaymentListCommand(Index studentIndex) {
        this.studentIndex = studentIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = studentIndex == null ? null : LessonCommandUtil.getStudent(model, studentIndex);

        List<LedgerEntry> payments = model.getAddressBook().getLedgerEntries().stream()
                .filter(entry -> entry.kind() == LedgerEntry.Kind.PAYMENT)
                .filter(entry -> student == null || entry.isWith(student))
                .sorted(Comparator.comparing(LedgerEntry::date)) // stable, so the same-day order is the entry order
                .collect(Collectors.toList());
        model.setShownPayments(payments);

        if (payments.isEmpty()) {
            return new CommandResult(student == null ? MESSAGE_NO_PAYMENTS
                    : String.format(MESSAGE_NO_PAYMENTS_FOR_STUDENT, student.getName()));
        }
        // Built once, so listing n payments stays linear instead of scanning the students and the ledger per row.
        Map<UUID, String> studentNames = model.getAddressBook().getPersonList().stream()
                .collect(Collectors.toMap(Person::getId, person -> person.getName().toString()));
        Set<UUID> reversedIds = Ledger.reversedIds(model.getAddressBook());

        int shown = Math.min(payments.size(), MAX_DISPLAYED_PAYMENTS);
        String rows = IntStream.range(0, shown)
                .mapToObj(i -> (i + 1) + ". " + describe(payments.get(i), studentNames, reversedIds))
                .collect(Collectors.joining("\n"));
        if (shown < payments.size()) {
            rows += "\n" + String.format(MESSAGE_TRUNCATED, shown, payments.size());
        }
        return new CommandResult(MESSAGE_HEADER + "\n" + rows + "\n" + MESSAGE_FOOTER);
    }

    private static String describe(LedgerEntry payment, Map<UUID, String> studentNames, Set<UUID> reversedIds) {
        String name = studentNames.get(payment.studentId());
        if (name == null) {
            throw new IllegalStateException("Payment " + payment.id() + " refers to student " + payment.studentId()
                    + ", who is not in the address book.");
        }
        StringBuilder row = new StringBuilder(payment.date() + " " + name + " " + Ledger.format(payment.amount()));
        if (payment.notes() != null) {
            row.append(" — ").append(payment.notes());
        }
        if (reversedIds.contains(payment.id())) {
            row.append(MESSAGE_REVERSED_MARK);
        }
        return row.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof PaymentListCommand command
                && Objects.equals(studentIndex, command.studentIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex == null ? 0 : studentIndex.getOneBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).toString();
    }
}
