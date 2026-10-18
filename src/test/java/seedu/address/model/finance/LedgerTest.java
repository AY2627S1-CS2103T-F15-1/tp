package seedu.address.model.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class LedgerTest {
    private final AddressBook book = new AddressBook();
    private final LocalDate today = LocalDate.of(2026, 10, 18);

    private Lesson lesson(String date) {
        if (book.getPersonList().isEmpty()) {
            book.addPerson(TypicalPersons.ALICE);
        }
        Lesson lesson = new LessonBuilder().withStudent(TypicalPersons.ALICE).withDate(date).build()
                .withStudent(TypicalPersons.ALICE);
        book.addLesson(lesson);
        return lesson;
    }

    @Test
    public void allocation_partialCreditAndReversals_recomputesOldestDebt() {
        Lesson first = lesson("2026-09-01");
        Lesson second = lesson("2026-09-08");
        LedgerEntry a = LedgerEntry.charge(first, TypicalPersons.ALICE, new BigDecimal("100"));
        LedgerEntry b = LedgerEntry.charge(second, TypicalPersons.ALICE, new BigDecimal("100"));
        book.addLedgerEntry(a);
        book.addLedgerEntry(b);
        LedgerEntry payment = LedgerEntry.payment(TypicalPersons.ALICE.getId(), new BigDecimal("150"), today, "Cash");
        book.addLedgerEntry(payment);
        assertEquals(new BigDecimal("50.00"), Ledger.balance(book, TypicalPersons.ALICE.getId()));
        assertEquals(second.getDate().value, Ledger.unpaid(book, TypicalPersons.ALICE.getId()).getFirst()
                .charge().getDate());
        book.addLedgerEntry(LedgerEntry.reverse(b, today));
        assertEquals(new BigDecimal("-50.00"), Ledger.balance(book, TypicalPersons.ALICE.getId()));
        assertTrue(Ledger.unpaid(book, TypicalPersons.ALICE.getId()).isEmpty());
        book.addLedgerEntry(LedgerEntry.reverse(payment, today));
        assertEquals(new BigDecimal("100.00"), Ledger.balance(book, TypicalPersons.ALICE.getId()));
        assertEquals(a, Ledger.unpaid(book, TypicalPersons.ALICE.getId()).getFirst().charge());
        assertEquals(new BigDecimal("100.00"), Ledger.account(book, TypicalPersons.ALICE.getId())
                .getLast().balance());
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(LedgerEntry.reverse(payment, today)));
    }

    @Test
    public void identities_duplicateTransfersAllowed_duplicateIdsAndActiveChargesRejected() {
        Lesson lesson = lesson("2026-09-01");
        LedgerEntry charge = LedgerEntry.charge(lesson, TypicalPersons.ALICE, new BigDecimal("50"));
        book.addLedgerEntry(charge);
        assertThrows(IllegalArgumentException.class, () ->
                book.addLedgerEntry(LedgerEntry.charge(lesson, TypicalPersons.ALICE, new BigDecimal("55"))));
        LedgerEntry first = LedgerEntry.payment(TypicalPersons.ALICE.getId(), new BigDecimal("20"), today, null);
        book.addLedgerEntry(first);
        book.addLedgerEntry(LedgerEntry.payment(TypicalPersons.ALICE.getId(), new BigDecimal("20"), today, null));
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(first));
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(
                LedgerEntry.payment(UUID.randomUUID(), new BigDecimal("20"), today, null)));
        assertEquals(new BigDecimal("10.00"), Ledger.balance(book, TypicalPersons.ALICE.getId()));
        assertEquals(book, new AddressBook(book));
        assertThrows(UnsupportedOperationException.class, () -> book.getLedgerEntries().clear());
    }

    @Test
    public void money_invalidPrecisionOrNonpositive_rejected() {
        for (String invalid : new String[] {"0", "-1", "1.001"}) {
            assertThrows(IllegalArgumentException.class, () ->
                    LedgerEntry.payment(UUID.randomUUID(), new BigDecimal(invalid), today, null));
        }
        assertThrows(IllegalArgumentException.class, () -> LedgerEntry.payment(UUID.randomUUID(),
                BigDecimal.ONE, today, "\n"));
    }
}
