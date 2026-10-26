package seedu.address.model.finance;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Formats the account of one student: every charge and payment in date order, with a running balance. */
public final class AccountStatement {
    public static final String MESSAGE_NO_ACTIVITY = "No charges or payments recorded for %1$s.";

    private AccountStatement() {}

    /**
     * Returns the statement of {@code student}. Entries are in date order and entries on the same day keep the order
     * in which they were recorded.
     */
    public static String format(ReadOnlyAddressBook book, Person student) {
        requireNonNull(book);
        requireNonNull(student);
        List<LedgerEntry> entries = book.getLedgerEntries().stream().filter(entry -> entry.isWith(student))
                .sorted(Comparator.comparing(LedgerEntry::date)).toList();
        if (entries.isEmpty()) {
            return String.format(MESSAGE_NO_ACTIVITY, student.getName());
        }

        StringBuilder statement = new StringBuilder("Account — ").append(student.getName());
        BigDecimal running = Ledger.ZERO;
        BigDecimal charged = Ledger.ZERO;
        BigDecimal paid = Ledger.ZERO;
        for (LedgerEntry entry : entries) {
            BigDecimal signed = entry.kind().increasesBalance() ? entry.amount() : entry.amount().negate();
            running = running.add(signed);
            if (entry.kind().refersToLesson()) {
                charged = charged.add(signed);
            } else {
                paid = paid.subtract(signed);
            }
            statement.append('\n').append(entry.date()).append(' ').append(describe(book, entry))
                    .append(" bal ").append(formatRunning(running));
        }
        return statement.append("\nTotal charged ").append(Ledger.format(charged))
                .append(" · total paid ").append(Ledger.format(paid))
                .append(" (after reversals) · ").append(Ledger.describeBalance(running)).toString();
    }

    /**
     * Names the kind of entry and its signed amount: charges and payment reversals add to the balance, while payments
     * take from it. A reversal names the kind and date of the entry it cancels.
     */
    private static String describe(ReadOnlyAddressBook book, LedgerEntry entry) {
        if (entry.kind().isReversal()) {
            return describeReversal(book, entry);
        }
        if (entry.kind() == LedgerEntry.Kind.PAYMENT) {
            return "payment -" + Ledger.format(entry.amount());
        }
        Optional<Lesson> lesson = book.getLessonList().stream()
                .filter(candidate -> candidate.getId().equals(entry.lessonId())).findFirst();
        return "charge " + lesson.map(found -> found.getSubject() + " lesson").orElse("lesson") + " "
                + Ledger.format(entry.amount());
    }

    private static String describeReversal(ReadOnlyAddressBook book, LedgerEntry reversal) {
        String what = reversal.kind() == LedgerEntry.Kind.CHARGE_REVERSAL ? "charge" : "payment";
        String original = book.getLedgerEntries().stream().filter(entry -> entry.id().equals(reversal.reverses()))
                .findFirst().map(entry -> " (" + entry.date() + ")").orElse("");
        return "reversal of " + what + original + " " + Ledger.format(reversal.amount());
    }

    /** A balance that has gone negative is credit, which is never shown as a negative number. */
    private static String formatRunning(BigDecimal running) {
        return running.signum() < 0 ? Ledger.describeBalance(running) : Ledger.format(running);
    }
}
