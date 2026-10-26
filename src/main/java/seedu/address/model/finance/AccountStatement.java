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
            boolean isCharge = entry.kind() == LedgerEntry.Kind.CHARGE;
            running = isCharge ? running.add(entry.amount()) : running.subtract(entry.amount());
            charged = isCharge ? charged.add(entry.amount()) : charged;
            paid = isCharge ? paid : paid.add(entry.amount());
            statement.append('\n').append(entry.date()).append(' ').append(describe(book, entry))
                    .append(" bal ").append(formatRunning(running));
        }
        return statement.append("\nTotal charged ").append(Ledger.format(charged))
                .append(" · total paid ").append(Ledger.format(paid))
                .append(" (after reversals) · ").append(Ledger.describeBalance(running)).toString();
    }

    /** Names the kind of entry and its signed amount: charges add to the balance and payments take from it. */
    private static String describe(ReadOnlyAddressBook book, LedgerEntry entry) {
        if (entry.kind() == LedgerEntry.Kind.PAYMENT) {
            return "payment -" + Ledger.format(entry.amount());
        }
        Optional<Lesson> lesson = book.getLessonList().stream()
                .filter(candidate -> candidate.getId().equals(entry.lessonId())).findFirst();
        return "charge " + lesson.map(found -> found.getSubject() + " lesson").orElse("lesson") + " "
                + Ledger.format(entry.amount());
    }

    /** A balance that has gone negative is credit, which is never shown as a negative number. */
    private static String formatRunning(BigDecimal running) {
        return running.signum() < 0 ? Ledger.describeBalance(running) : Ledger.format(running);
    }
}
