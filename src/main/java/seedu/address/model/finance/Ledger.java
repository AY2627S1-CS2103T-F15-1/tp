package seedu.address.model.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;

/** Single accounting policy for account rows, outstanding amounts, credit, and FIFO charge allocation. */
public final class Ledger {
    /** An unpaid portion after payments have been allocated oldest-charge-first. */
    public record UnpaidCharge(LedgerEntry charge, BigDecimal remaining) { }

    /** An immutable dated account row with its signed effect and running balance. */
    public record AccountRow(LedgerEntry entry, BigDecimal effect, BigDecimal balance) { }

    private Ledger() {
    }

    /** Returns whether an event has an explicit compensating reversal. */
    public static boolean isReversed(ReadOnlyAddressBook book, UUID id) {
        return book.getLedgerEntries().stream().anyMatch(entry -> id.equals(entry.reversalOf()));
    }

    /** Returns the one active charge for a lesson's persistent identity. */
    public static Optional<LedgerEntry> activeCharge(ReadOnlyAddressBook book, UUID lessonId) {
        return book.getLedgerEntries().stream().filter(entry -> lessonId.equals(entry.lessonId()))
                .filter(entry -> !isReversed(book, entry.id())).findFirst();
    }

    /** Returns ordered student events. Equal dates retain persisted append order across restarts. */
    public static List<LedgerEntry> entries(ReadOnlyAddressBook book, UUID studentId) {
        return book.getLedgerEntries().stream().filter(entry -> entry.studentId().equals(studentId))
                .sorted(Comparator.comparing(LedgerEntry::date)).toList();
    }

    /** Returns the signed effect of an event, including compensating reversals. */
    public static BigDecimal effect(ReadOnlyAddressBook book, LedgerEntry entry) {
        if (entry.kind() == LedgerEntry.Kind.REVERSAL) {
            LedgerEntry original = book.getLedgerEntries().stream()
                    .filter(item -> item.id().equals(entry.reversalOf())).findFirst().orElseThrow();
            return effect(book, original).negate();
        }
        return entry.kind() == LedgerEntry.Kind.CHARGE ? entry.amount() : entry.amount().negate();
    }

    /** Returns charges minus payments; negative balances represent advance credit. */
    public static BigDecimal balance(ReadOnlyAddressBook book, UUID studentId) {
        return entries(book, studentId).stream().map(entry -> effect(book, entry)).reduce(Money.ZERO, BigDecimal::add);
    }

    /** Returns all events, including reversals, with a chronological running balance. */
    public static List<AccountRow> account(ReadOnlyAddressBook book, UUID studentId) {
        List<AccountRow> rows = new ArrayList<>();
        BigDecimal running = Money.ZERO;
        for (LedgerEntry entry : entries(book, studentId)) {
            BigDecimal effect = effect(book, entry);
            running = running.add(effect);
            rows.add(new AccountRow(entry, effect, running));
        }
        return List.copyOf(rows);
    }

    /** Returns active totals of a specific original event type. */
    public static BigDecimal total(ReadOnlyAddressBook book, UUID studentId, LedgerEntry.Kind kind) {
        return entries(book, studentId).stream().filter(entry -> entry.kind() == kind)
                .filter(entry -> !isReversed(book, entry.id())).map(LedgerEntry::amount)
                .reduce(Money.ZERO, BigDecimal::add);
    }

    /** Allocates all active payments, including advance credit, to oldest active charges first. */
    public static List<UnpaidCharge> unpaid(ReadOnlyAddressBook book, UUID studentId) {
        BigDecimal available = total(book, studentId, LedgerEntry.Kind.PAYMENT);
        List<UnpaidCharge> unpaid = new ArrayList<>();
        for (LedgerEntry entry : entries(book, studentId)) {
            if (entry.kind() != LedgerEntry.Kind.CHARGE || isReversed(book, entry.id())) {
                continue;
            }
            BigDecimal allocated = available.min(entry.amount());
            available = available.subtract(allocated);
            BigDecimal remaining = entry.amount().subtract(allocated);
            if (remaining.signum() > 0) {
                unpaid.add(new UnpaidCharge(entry, remaining));
            }
        }
        return List.copyOf(unpaid);
    }

    /** Rejects dangling links, duplicate identities, second active charges, and repeated reversals. */
    public static void validateAppend(ReadOnlyAddressBook book, LedgerEntry entry) {
        if (book.getLedgerEntries().stream().anyMatch(item -> item.id().equals(entry.id()))) {
            throw new IllegalArgumentException("Duplicate ledger entry ID.");
        }
        if (book.getPersonList().stream().noneMatch(person -> person.getId().equals(entry.studentId()))) {
            throw new IllegalArgumentException("Ledger student does not exist.");
        }
        if (entry.kind() == LedgerEntry.Kind.CHARGE) {
            Lesson lesson = book.getLessonList().stream().filter(item -> item.getId().equals(entry.lessonId()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Charge lesson does not exist."));
            boolean linked = book.getPersonList().stream().anyMatch(person -> person.getId().equals(entry.studentId())
                    && lesson.isWith(person));
            if (!linked) {
                throw new IllegalArgumentException("Charge student does not match its lesson.");
            }
            if (activeCharge(book, entry.lessonId()).isPresent()) {
                throw new IllegalArgumentException("That lesson already has an active charge.");
            }
        } else if (entry.kind() == LedgerEntry.Kind.REVERSAL) {
            LedgerEntry original = book.getLedgerEntries().stream()
                    .filter(item -> item.id().equals(entry.reversalOf())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Reversed entry does not exist."));
            if (original.kind() == LedgerEntry.Kind.REVERSAL || isReversed(book, original.id())
                    || !original.studentId().equals(entry.studentId()) || !original.amount().equals(entry.amount())
                    || entry.date().isBefore(original.date())) {
                throw new IllegalArgumentException("Invalid or already applied reversal.");
            }
        }
    }
}
