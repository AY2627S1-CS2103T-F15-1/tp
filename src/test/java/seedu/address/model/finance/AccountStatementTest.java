package seedu.address.model.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class AccountStatementTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 17);
    private final Person bob = new PersonBuilder().withName("Lee Bob").withRate("90").build();
    private final Person cara = new PersonBuilder().withName("Cara Lim").withPhone("98765432").build();

    @Test
    public void format_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AccountStatement.format(null, bob));
        assertThrows(NullPointerException.class, () -> AccountStatement.format(new AddressBook(), null));
    }

    @Test
    public void format_noActivity_saysSo() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        assertEquals("No charges or payments recorded for Lee Bob.", AccountStatement.format(book, bob));
    }

    @Test
    public void format_chargePaymentAndCredit_listsRowsWithRunningBalance() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        book.addLedgerEntry(chargeFor(book, bob, "Chemistry", DAY));
        book.addLedgerEntry(LedgerEntry.payment(bob, new BigDecimal("90"), DAY.plusDays(2)));
        book.addLedgerEntry(LedgerEntry.payment(bob, new BigDecimal("10.50"), DAY.plusDays(2)));

        assertEquals("Account — Lee Bob\n"
                + "2026-10-17 charge Chemistry lesson $90.00 bal $90.00\n"
                + "2026-10-19 payment -$90.00 bal $0.00\n"
                + "2026-10-19 payment -$10.50 bal IN CREDIT $10.50\n"
                + "Total charged $90.00 · total paid $100.50 (after reversals) · total withdrawn $0.00"
                + " · IN CREDIT $10.50",
                AccountStatement.format(book, bob));
    }

    @Test
    public void format_entriesAreInDateOrderAndOtherStudentsAreLeftOut() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        book.addPerson(cara);
        book.addLedgerEntry(LedgerEntry.payment(bob, new BigDecimal("20"), DAY.plusDays(1)));
        book.addLedgerEntry(chargeFor(book, bob, "Chemistry", DAY));
        book.addLedgerEntry(LedgerEntry.payment(cara, new BigDecimal("5"), DAY));

        assertEquals("Account — Lee Bob\n"
                + "2026-10-17 charge Chemistry lesson $90.00 bal $90.00\n"
                + "2026-10-18 payment -$20.00 bal $70.00\n"
                + "Total charged $90.00 · total paid $20.00 (after reversals) · total withdrawn $0.00"
                + " · OUTSTANDING $70.00",
                AccountStatement.format(book, bob));
    }

    @Test
    public void format_reversedPayment_matchesSpecificationExample() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        book.addLedgerEntry(chargeFor(book, bob, "Chemistry", DAY));
        LedgerEntry payment = LedgerEntry.payment(bob, new BigDecimal("90"), DAY.plusDays(2));
        book.addLedgerEntry(payment);
        book.addLedgerEntry(LedgerEntry.reversalOf(payment, DAY.plusDays(2)));

        assertEquals("Account — Lee Bob\n"
                + "2026-10-17 charge Chemistry lesson $90.00 bal $90.00\n"
                + "2026-10-19 payment -$90.00 bal $0.00\n"
                + "2026-10-19 reversal of payment (2026-10-19) $90.00 bal $90.00\n"
                + "Total charged $90.00 · total paid $0.00 (after reversals) · total withdrawn $0.00"
                + " · OUTSTANDING $90.00",
                AccountStatement.format(book, bob));
    }

    @Test
    public void format_reversedCharge_takesChargeOutOfTotalsAndShowsOriginalDate() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        LedgerEntry charge = chargeFor(book, bob, "Chemistry", DAY);
        book.addLedgerEntry(charge);
        book.addLedgerEntry(LedgerEntry.reversalOf(charge, DAY.plusDays(1)));

        assertEquals("Account — Lee Bob\n"
                + "2026-10-17 charge Chemistry lesson $90.00 bal $90.00\n"
                + "2026-10-18 reversal of charge (2026-10-17) $90.00 bal $0.00\n"
                + "Total charged $0.00 · total paid $0.00 (after reversals) · total withdrawn $0.00"
                + " · OUTSTANDING $0.00",
                AccountStatement.format(book, bob));
    }

    @Test
    public void format_withdrawal_listsAndReducesCredit() {
        AddressBook book = new AddressBook();
        book.addPerson(bob);
        book.addLedgerEntry(LedgerEntry.payment(bob, new BigDecimal("100"), DAY));
        book.addLedgerEntry(LedgerEntry.withdrawal(bob, new BigDecimal("30"), DAY.plusDays(1), "cash"));

        assertEquals("Account — Lee Bob\n"
                + "2026-10-17 payment -$100.00 bal IN CREDIT $100.00\n"
                + "2026-10-18 withdrawal -$30.00 bal IN CREDIT $70.00\n"
                + "Total charged $0.00 · total paid $100.00 (after reversals) · total withdrawn $30.00"
                + " · IN CREDIT $70.00",
                AccountStatement.format(book, bob));
    }

    private static LedgerEntry chargeFor(AddressBook book, Person student, String subject, LocalDate date) {
        Lesson lesson = new LessonBuilder().withStudent(student).withSubject(subject).withDuration(60)
                .withDate(date).build();
        book.addLesson(lesson);
        return LedgerEntry.charge(student, lesson.complete(null));
    }
}
