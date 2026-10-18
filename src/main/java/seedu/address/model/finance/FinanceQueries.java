package seedu.address.model.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Read-only finance presentation shared by payment feedback, accounts, and daily summaries. */
public final class FinanceQueries {
    /** An outstanding student account with its oldest remaining charge date. */
    public record Due(UUID studentId, BigDecimal balance, LocalDate oldestUnpaid) { }

    private FinanceQueries() {
    }

    /** Formats credit as a positive amount with an explicit label. */
    public static String balanceLabel(BigDecimal balance) {
        return balance.signum() < 0 ? "IN CREDIT " + Money.format(balance.negate())
                : "OUTSTANDING " + Money.format(balance);
    }

    /** Returns a complete account without assigning indices belonging to another command. */
    public static String account(ReadOnlyAddressBook book, UUID studentId) {
        List<Ledger.AccountRow> rows = Ledger.account(book, studentId);
        if (rows.isEmpty()) {
            return "No charges or payments recorded for " + studentLabel(book, studentId) + ".";
        }
        StringBuilder text = new StringBuilder("Account — " + studentLabel(book, studentId));
        for (Ledger.AccountRow row : rows) {
            text.append("\n").append(row.entry().date()).append(" ").append(description(book, row.entry()))
                    .append(" ").append(signedAmount(row.effect())).append(" bal ")
                    .append(signedAmount(row.balance()));
        }
        return text + "\nTotal charged " + Money.format(Ledger.total(book, studentId, LedgerEntry.Kind.CHARGE))
                + " · total paid " + Money.format(Ledger.total(book, studentId, LedgerEntry.Kind.PAYMENT))
                + " (after reversals) · " + balanceLabel(Ledger.balance(book, studentId));
    }

    /** Returns debts ordered by the oldest unpaid charge, then name and stable student identity. */
    public static List<Due> dues(ReadOnlyAddressBook book) {
        List<Due> dues = new ArrayList<>();
        for (Person student : book.getPersonList()) {
            List<Ledger.UnpaidCharge> unpaid = Ledger.unpaid(book, student.getId());
            if (!unpaid.isEmpty()) {
                dues.add(new Due(student.getId(), Ledger.balance(book, student.getId()),
                        unpaid.getFirst().charge().date()));
            }
        }
        dues.sort(Comparator.comparing(Due::oldestUnpaid)
                .thenComparing(due -> studentLabel(book, due.studentId()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Due::studentId));
        return List.copyOf(dues);
    }

    /** Formats all outstanding accounts without retargeting student or payment selection indices. */
    public static String duesSummary(ReadOnlyAddressBook book, LocalDate today) {
        List<Due> dues = dues(book);
        if (dues.isEmpty()) {
            return "DUES\nNo outstanding balances. Everyone is up to date.";
        }
        BigDecimal total = dues.stream().map(Due::balance).reduce(Money.ZERO, BigDecimal::add);
        StringBuilder text = new StringBuilder("DUES\n" + dues.size() + " student(s) with outstanding balances — "
                + Money.format(total) + " total");
        for (Due due : dues) {
            text.append("\n  ").append(studentLabel(book, due.studentId())).append(" ")
                    .append(Money.format(due.balance())).append(" oldest unpaid ")
                    .append(ChronoUnit.DAYS.between(due.oldestUnpaid(), today)).append(" days");
        }
        return text.toString();
    }

    /** Formats signed account movements with a conventional leading minus before the dollar sign. */
    private static String signedAmount(BigDecimal amount) {
        return (amount.signum() < 0 ? "-" : "") + Money.format(amount.abs());
    }

    /** Describes a compensating event with enough original context to audit the correction. */
    private static String description(ReadOnlyAddressBook book, LedgerEntry entry) {
        if (entry.kind() == LedgerEntry.Kind.REVERSAL) {
            LedgerEntry original = book.getLedgerEntries().stream()
                    .filter(item -> item.id().equals(entry.reversalOf())).findFirst().orElseThrow();
            return "reversal of " + description(book, original) + " (" + original.date() + ")";
        }
        return (entry.kind() == LedgerEntry.Kind.CHARGE ? "charge" : "payment")
                + (entry.description() == null ? "" : " " + entry.description());
    }

    /** Uses the student's current display identity without changing any financial snapshot. */
    public static String studentLabel(ReadOnlyAddressBook book, UUID studentId) {
        Person student = book.getPersonList().stream().filter(person -> person.getId().equals(studentId))
                .findFirst().orElseThrow();
        return student.getName() + (student.isArchived() ? " (archived)" : "");
    }
}
