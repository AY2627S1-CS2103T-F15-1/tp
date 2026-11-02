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
import seedu.address.model.person.StudentSummaryLabels;

/** Calculates student balances from recorded charges and payments, without changing the underlying records. */
public final class Ledger {
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Ledger() {}

    /** A student with a positive amount owed. */
    public record Due(Person student, BigDecimal amount, LocalDate oldestUnpaidDate) { }

    /** Returns charges and withdrawals minus payments. A negative balance represents credit. */
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

    /**
     * Returns the IDs of every entry that some reversal in {@code book} cancels, found in one pass. Callers that
     * test many entries should build this once and look entries up in it, instead of calling {@link #isReversed}
     * for each entry, which scans the whole ledger every time.
     */
    public static Set<UUID> reversedIds(ReadOnlyAddressBook book) {
        requireNonNull(book);
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

    /**
     * Returns the date from which {@code student}, who must owe money, has been in debt.
     * Payments cover the oldest charges first, so this is the date of the oldest charge that is not fully covered.
     * If every charge is covered but the student still owes money, for example after a payment that was already
     * withdrawn is reversed, the debt dates from the entry that made the account go into debt, and it keeps that
     * date when later payments reduce it. A reversal dated today therefore gives a debt that is 0 days old.
     */
    private static LocalDate oldestUnpaidDate(ReadOnlyAddressBook book, Person student) {
        assert balance(book, student).signum() > 0 : "Only debtors have an oldest unpaid date";
        List<LedgerEntry> entries = book.getLedgerEntries().stream().filter(entry -> entry.isWith(student)).toList();
        Set<UUID> reversedIds = reversedIds(book);
        List<LedgerEntry> charges = entries.stream().filter(entry -> entry.kind() == LedgerEntry.Kind.CHARGE)
                .filter(entry -> !reversedIds.contains(entry.id()))
                .sorted(Comparator.comparing(LedgerEntry::date).thenComparing(LedgerEntry::id)).toList();
        BigDecimal credit = entries.stream().filter(entry -> entry.kind() == LedgerEntry.Kind.PAYMENT)
                .filter(entry -> !reversedIds.contains(entry.id()))
                .map(LedgerEntry::amount).reduce(ZERO, BigDecimal::add)
                .subtract(entries.stream().filter(entry -> entry.kind() == LedgerEntry.Kind.WITHDRAWAL)
                        .map(LedgerEntry::amount).reduce(ZERO, BigDecimal::add));
        for (LedgerEntry charge : charges) {
            if (credit.compareTo(charge.amount()) < 0) {
                return charge.date();
            }
            credit = credit.subtract(charge.amount());
        }
        return debtOriginDate(entries);
    }

    /**
     * Returns the date of the entry after which the balance of {@code entries} has been above zero without a break,
     * in date order. Later payments that leave a balance above zero do not change it.
     */
    private static LocalDate debtOriginDate(List<LedgerEntry> entries) {
        List<LedgerEntry> inOrder = entries.stream().sorted(Comparator.comparing(LedgerEntry::date)).toList();
        BigDecimal running = ZERO;
        LocalDate origin = null;
        for (LedgerEntry entry : inOrder) {
            BigDecimal before = running;
            running = running.add(entry.kind().increasesBalance() ? entry.amount() : entry.amount().negate());
            if (running.signum() <= 0) {
                origin = null;
            } else if (before.signum() <= 0) {
                origin = entry.date();
            }
        }
        // A debtor's running balance ends above zero, so an origin exists; the latest date is only a safeguard.
        return origin != null ? origin : inOrder.getLast().date();
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
        StudentSummaryLabels labels = new StudentSummaryLabels(book);
        for (Due due : dues) {
            long days = Math.max(0, ChronoUnit.DAYS.between(due.oldestUnpaidDate(), today));
            result.append("\n  ").append(labels.format(due.student().getId()))
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
