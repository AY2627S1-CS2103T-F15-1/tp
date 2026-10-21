package seedu.address.model.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class LedgerTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);
    private final Person alice = new PersonBuilder().withName("Alice").withRate("50").build();
    private final Person bob = new PersonBuilder().withName("Bob").withPhone("98765432").withRate("80").build();

    @Test
    public void balance_partialPaymentExactPaymentAndCredit_usesExactDecimals() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        book.addPerson(bob);
        book.addLedgerEntry(charge(book, alice, "100.10", DAY));
        book.addLedgerEntry(charge(book, bob, "80.00", DAY));
        book.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("40.05"), DAY));
        assertEquals(new BigDecimal("60.05"), Ledger.balance(book, alice));
        book.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("60.05"), DAY));
        assertEquals(Ledger.ZERO, Ledger.balance(book, alice));
        book.addLedgerEntry(LedgerEntry.payment(alice, BigDecimal.TEN, DAY));
        assertEquals(new BigDecimal("-10.00"), Ledger.balance(book, alice));
        assertEquals(List.of(new Ledger.Due(bob, new BigDecimal("80.00"), DAY)), Ledger.dues(book, DAY));
        assertEquals("$10.00", Ledger.format(BigDecimal.TEN));
    }

    @Test
    public void completion_capturesRateOnceAndKeepsItAcrossEdits() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        Lesson lesson = new LessonBuilder().withStudent(alice).withDate(DAY).build();
        book.addLesson(lesson);
        Lesson completed = lesson.complete("First notes");
        book.chargeLesson(lesson, completed);
        assertEquals(new BigDecimal("75.00"), Ledger.balance(book, alice));
        Person edited = new PersonBuilder(alice).withName("Alice Tan").withPhone("91234567").withRate("90").build();
        book.setPerson(alice, edited);
        Lesson renamed = book.getLessonList().getFirst();
        book.setLesson(renamed, renamed.complete("Updated notes"));
        assertEquals(1, book.getLedgerEntries().size());
        assertEquals(new BigDecimal("75.00"), Ledger.balance(book, book.getPersonList().getFirst()));
        assertEquals(book, new AddressBook(book));
        assertThrows(UnsupportedOperationException.class, () -> book.getLedgerEntries().clear());
        book.removePerson(book.getPersonList().getFirst());
        assertTrue(book.getLedgerEntries().isEmpty());
    }

    @Test
    public void entries_invalidAmountsDuplicatesAndOrphanStudents_rejected() {
        assertThrows(IllegalArgumentException.class, () -> {
            LedgerEntry.payment(alice, new BigDecimal("1.001"), DAY);
        });
        assertThrows(IllegalArgumentException.class, () -> LedgerEntry.payment(alice, BigDecimal.ZERO, DAY));
        assertThrows(IllegalArgumentException.class, () -> LedgerEntry.payment(alice, BigDecimal.ONE.negate(), DAY));
        AddressBook book = new AddressBook();
        Lesson lesson = new LessonBuilder().withStudent(alice).build();
        LedgerEntry charge = LedgerEntry.charge(alice, lesson);
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(charge));
        book.addPerson(alice);
        book.addLesson(lesson);
        book.addLedgerEntry(charge);
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(charge));
        assertEquals(new BigDecimal("75.00"), Ledger.balance(book, alice));
    }

    @Test
    public void legacyCompletedLesson_doesNotInventHistoricalDebt() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        Lesson completed = new LessonBuilder().withStudent(alice).withDate(DAY).build().complete(null);
        book.addLesson(completed);
        book.setLesson(completed, completed.complete("Revised notes"));
        assertEquals(Ledger.ZERO, Ledger.balance(book, alice));
    }

    @Test
    public void charge_hourlyRateTimesDuration_roundsHalfUpToCents() {
        Person sixty = new PersonBuilder(alice).withRate("60").build();
        assertEquals(new BigDecimal("90.00"), LedgerEntry.charge(sixty,
                new LessonBuilder().withStudent(sixty).withDuration(90).build()).amount());
        for (String rate : List.of("62.51", "62.50")) {
            Person student = new PersonBuilder(alice).withRate(rate).build();
            Lesson lesson = new LessonBuilder().withStudent(student).withDuration(45).build();
            assertEquals(new BigDecimal("46.88"), LedgerEntry.charge(student, lesson).amount());
        }
    }

    @Test
    public void charge_sameSlotAfterRename_hasIndependentStudentAndLessonIds() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        Lesson first = new LessonBuilder().withStudent(alice).withDate(DAY).build();
        book.addLesson(first);
        book.chargeLesson(first, first.complete(null));
        book.setPerson(alice, new PersonBuilder(alice).withName("Alice Tan").build());
        Person renamed = book.getPersonList().getFirst();
        Person newAlice = new PersonBuilder(alice).build();
        book.addPerson(newAlice);
        Lesson second = new LessonBuilder().withStudent(newAlice).withDate(DAY).build();
        book.addLesson(second);
        book.chargeLesson(second, second.complete(null));
        assertEquals(2, book.getLedgerEntries().size());
        assertEquals(new BigDecimal("75.00"), Ledger.balance(book, renamed));
        assertEquals(new BigDecimal("75.00"), Ledger.balance(book, newAlice));
        assertEquals(first.getId(), book.getLedgerEntries().getFirst().lessonId());
        assertEquals(second.getId(), book.getLedgerEntries().getLast().lessonId());
    }

    @Test
    public void dues_paymentsClearOldestChargesFirst_andIncludeArchivedStudents() {
        AddressBook book = new AddressBook();
        Person archivedBob = bob.withArchived(true);
        book.addPerson(alice);
        book.addPerson(archivedBob);
        book.addLedgerEntry(charge(book, alice, "40", DAY.minusDays(10)));
        book.addLedgerEntry(charge(book, alice, "60", DAY.minusDays(2)));
        book.addLedgerEntry(charge(book, archivedBob, "80", DAY.minusDays(5)));
        book.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("39.99"), DAY));
        assertEquals(DAY.minusDays(10), Ledger.dues(book, DAY).getFirst().oldestUnpaidDate());
        book.addLedgerEntry(LedgerEntry.payment(alice, new BigDecimal("0.01"), DAY));
        assertEquals(List.of(new Ledger.Due(archivedBob, new BigDecimal("80.00"), DAY.minusDays(5)),
                new Ledger.Due(alice, new BigDecimal("60.00"), DAY.minusDays(2))), Ledger.dues(book, DAY));
        assertEquals("DUES\n2 student(s) with outstanding balances \u2014 $140.00 total"
                + "\n  Bob (archived) $80.00 oldest unpaid 5 days\n  Alice $60.00 oldest unpaid 2 days",
                Ledger.formatDues(book, DAY));
    }

    @Test
    public void descriptions_creditZeroAndOutstanding_useSharedLabels() {
        assertEquals("IN CREDIT $10.00", Ledger.describeBalance(BigDecimal.TEN.negate()));
        assertEquals("OUTSTANDING $0.00", Ledger.describeBalance(Ledger.ZERO));
        assertEquals("OUTSTANDING $10.00", Ledger.describeBalance(BigDecimal.TEN));
        assertEquals("-$10.00", Ledger.format(BigDecimal.TEN.negate()));
        assertEquals("No outstanding balances. Everyone is up to date.",
                Ledger.formatDues(new AddressBook(), DAY));
    }

    @Test
    public void ledgerReferences_orphanLessonWrongStudentAndDuplicateCharge_rejected() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        book.addPerson(bob);
        Lesson lesson = new LessonBuilder().withStudent(alice).build();
        LedgerEntry entry = LedgerEntry.charge(alice, lesson);
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(entry));
        book.addLesson(lesson);
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(new LedgerEntry(UUID.randomUUID(),
                bob.getId(), lesson.getId(), LedgerEntry.Kind.CHARGE, BigDecimal.TEN, DAY)));
        book.addLedgerEntry(entry);
        assertThrows(IllegalArgumentException.class, () -> book.addLedgerEntry(new LedgerEntry(UUID.randomUUID(),
                alice.getId(), lesson.getId(), LedgerEntry.Kind.CHARGE, BigDecimal.TEN, DAY)));
        assertEquals(1, book.getLedgerEntries().size());
    }

    @Test
    public void setLesson_statusReplacementDoesNotBill_chargeLessonReturnsTheCapturedCharge() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        Lesson lesson = new LessonBuilder().withStudent(alice).withDate(DAY).build();
        book.addLesson(lesson);
        book.setLesson(lesson, lesson.complete(null));
        assertTrue(book.getLedgerEntries().isEmpty());
        book.setLesson(lesson.complete(null), lesson);
        LedgerEntry charge = book.chargeLesson(lesson, lesson.complete(null)).orElseThrow();
        assertEquals(charge, book.getLedgerEntries().getFirst());
        assertEquals(new BigDecimal("75.00"), charge.amount());
        assertThrows(IllegalArgumentException.class, () -> book.chargeLesson(lesson, lesson.complete(null)));
        assertEquals(1, book.getLedgerEntries().size());
    }

    @Test
    public void copyLargeLedger_preservesEveryEntryAndReusesAnImmutableLiveView() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        List<LedgerEntry> entries = IntStream.range(0, 10_000)
                .mapToObj(index -> LedgerEntry.payment(alice, BigDecimal.ONE, DAY)).toList();
        book.setLedgerEntries(entries);
        AddressBook copy = new AddressBook(book);
        assertEquals(entries, copy.getLedgerEntries());
        assertSame(copy.getLedgerEntries(), copy.getLedgerEntries());
        assertThrows(UnsupportedOperationException.class, () -> copy.getLedgerEntries().clear());
        var liveView = copy.getLedgerEntries();
        copy.addLedgerEntry(LedgerEntry.payment(alice, BigDecimal.TEN, DAY));
        assertEquals(10_001, liveView.size());
        assertEquals(10_000, book.getLedgerEntries().size());
        copy.resetData(book);
        assertEquals(entries, liveView);
        assertEquals(new BigDecimal("-10000.00"), Ledger.balance(copy, alice));
    }

    @Test
    public void bulkLedgerRestore_invalidReferencesOrDuplicates_leaveExistingRecordsIntact() {
        AddressBook book = new AddressBook();
        book.addPerson(alice);
        LedgerEntry valid = LedgerEntry.payment(alice, BigDecimal.ONE, DAY);
        book.addLedgerEntry(valid);
        assertThrows(IllegalArgumentException.class, () -> book.setLedgerEntries(List.of(valid, valid)));
        assertThrows(IllegalArgumentException.class, () -> book.setLedgerEntries(
                List.of(LedgerEntry.payment(bob, BigDecimal.ONE, DAY))));
        assertEquals(List.of(valid), book.getLedgerEntries());
    }

    @Test
    public void dues_oneDayAndArchivedLabels_matchTheSharedSpecification() {
        AddressBook book = new AddressBook();
        Person archived = alice.withArchived(true);
        book.addPerson(archived);
        book.addLedgerEntry(charge(book, archived, "40", DAY.minusDays(1)));
        assertEquals("DUES\n1 student(s) with outstanding balances \u2014 $40.00 total"
                + "\n  Alice (archived) $40.00 oldest unpaid 1 day", Ledger.formatDues(book, DAY));
    }

    private static LedgerEntry charge(AddressBook book, Person student, String amount, LocalDate date) {
        Lesson lesson = new LessonBuilder().withStudent(student).withDate(date)
                .withTime(student.getName().fullName.equals("Bob") ? "14:00" : "10:00").build();
        book.addLesson(lesson);
        return new LedgerEntry(UUID.randomUUID(), student.getId(), lesson.getId(), LedgerEntry.Kind.CHARGE,
                new BigDecimal(amount), date);
    }
}
