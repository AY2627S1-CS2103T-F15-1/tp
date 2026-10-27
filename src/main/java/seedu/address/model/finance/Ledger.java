package seedu.address.model.finance;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Calculates student balances from recorded charges and payments, without changing the underlying records. */
public final class Ledger {
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Ledger() {}

    /** A student with a positive amount owed. */
    public record Due(Person student, BigDecimal amount, LocalDate oldestUnpaidDate) { }

    /** Returns charges minus payments. A negative balance represents credit. */
    public static BigDecimal balance(ReadOnlyAddressBook book, Person student) {
        requireNonNull(book);
        requireNonNull(student);
        return book.getLedgerEntries().stream().filter(entry -> entry.isWith(student))
                .map(entry -> entry.kind().increasesBalance() ? entry.amount() : entry.amount().negate())
                .reduce(ZERO, BigDecimal::add);
    }

    /** Returns whether a later reversal in {@code book} cancels {@code entry}. */
    public static boolean isReversed(ReadOnlyAddressBook book, LedgerEntry entry) {
        requireNonNull(book);
        requireNonNull(entry);
        return book.getLedgerEntries().stream().anyMatch(other -> entry.id().equals(other.reverses()));
    }

    /** Returns the ids of every entry that a reversal in {@code book} cancels, found in one pass. */
    private static Set<UUID> reversedIds(ReadOnlyAddressBook book) {
        return book.getLedgerEntries().stream().map(LedgerEntry::reverses).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Returns the charge of a lesson that has not been reversed. A lesson has at most one. */
    public static Optional<LedgerEntry> activeCharge(ReadOnlyAddressBook book, UUID lessonId) {
        requireNonNull(book);
        requireNonNull(lessonId);
        return book.getLedgerEntries().stream()
                .filter(entry -> entry.kind() == LedgerEntry.Kind.CHARGE && lessonId.equals(entry.lessonId()))
                .filter(entry -> !isReversed(book, entry)).findFirst();
    }

    /** Returns all debtors, including archived and filtered students, using the system date. */
    public static List<Due> dues(ReadOnlyAddressBook book) {
        return dues(book, LocalDate.now());
    }

    /** Returns debts oldest first, allocating all payments against the earliest charges first. */
    public static List<Due> dues(ReadOnlyAddressBook book, LocalDate today) {
        requireNonNull(book);
        requireNonNull(today);
        return book.getPersonList().stream().filter(student -> balance(book, student).signum() > 0)
                .map(student -> new Due(student, balance(book, student), oldestUnpaidDate(book, student)))
                .sorted(Comparator.comparing(Due::oldestUnpaidDate)
                        .thenComparing(due -> due.student().getName().fullName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(due -> due.student().getPhone().value))
                .toList();
    }

    private static LocalDate oldestUnpaidDate(ReadOnlyAddressBook book, Person student) {
        assert balance(book, student).signum() > 0 : "Only debtors have an oldest unpaid charge";
        List<LedgerEntry> entries = book.getLedgerEntries().stream().filter(entry -> entry.isWith(student)).toList();
        Set<UUID> reversedIds = reversedIds(book);
        BigDecimal payments = entries.stream().filter(entry -> entry.kind() == LedgerEntry.Kind.PAYMENT)
                .filter(entry -> !reversedIds.contains(entry.id()))
                .map(LedgerEntry::amount).reduce(ZERO, BigDecimal::add);
        List<LedgerEntry> charges = entries.stream().filter(entry -> entry.kind() == LedgerEntry.Kind.CHARGE)
                .filter(entry -> !reversedIds.contains(entry.id()))
                .sorted(Comparator.comparing(LedgerEntry::date).thenComparing(LedgerEntry::id)).toList();
        for (LedgerEntry charge : charges) {
            if (payments.compareTo(charge.amount()) < 0) {
                return charge.date();
            }
            payments = payments.subtract(charge.amount());
        }
        throw new IllegalStateException("A positive balance must have an unpaid charge.");
    }

    /** Shared account label for balance, payment, student details, and dashboards. */
    public static String describeBalance(BigDecimal amount) {
        requireNonNull(amount);
        return (amount.signum() < 0 ? "IN CREDIT " : "OUTSTANDING ") + format(amount.abs());
    }

    /** Formats the shared DUES section using a caller-supplied date for consistent ages. */
    public static String formatDues(ReadOnlyAddressBook book, LocalDate today) {
        List<Due> dues = dues(book, today);
        if (dues.isEmpty()) {
            return "No outstanding balances. Everyone is up to date.";
        }
        BigDecimal total = dues.stream().map(Due::amount).reduce(ZERO, BigDecimal::add);
        StringBuilder result = new StringBuilder("DUES\n").append(dues.size())
                .append(" student(s) with outstanding balances \u2014 ").append(format(total)).append(" total");
        for (Due due : dues) {
            long days = Math.max(0, ChronoUnit.DAYS.between(due.oldestUnpaidDate(), today));
            result.append("\n  ").append(due.student().getName())
                    .append(due.student().isArchived() ? " (archived)" : "")
                    .append(" ").append(format(due.amount())).append(" oldest unpaid ").append(days)
                    .append(days == 1 ? " day" : " days");
        }
        return result.toString();
    }

    /** Formats money without depending on the computer's locale. */
    public static String format(BigDecimal amount) {
        requireNonNull(amount);
        return (amount.signum() < 0 ? "-$" : "$") + amount.abs().setScale(2).toPlainString();
    }
}
