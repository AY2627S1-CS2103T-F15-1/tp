package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.finance.Ledger;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class FinanceStorageTest {
    @TempDir
    public Path directory;

    @Test
    public void restart_reversalsAndSnapshots_preserveExactRepresentation() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Lesson lesson = new LessonBuilder().withDate("2026-01-01").build().withStudent(TypicalPersons.ALICE);
        book.addLesson(lesson);
        LedgerEntry charge = LedgerEntry.charge(lesson, TypicalPersons.ALICE, new BigDecimal("105"));
        LedgerEntry payment = LedgerEntry.payment(TypicalPersons.ALICE.getId(), new BigDecimal("300"),
                LocalDate.of(2026, 1, 2), "PayNow ref 4471");
        book.addLedgerEntry(charge);
        book.addLedgerEntry(payment);
        book.addLedgerEntry(LedgerEntry.reverse(charge, LocalDate.of(2026, 1, 3)));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("nested/finance.json"));
        storage.saveAddressBook(book);
        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(book, restored);
        assertEquals(book.hashCode(), restored.hashCode());
        assertEquals(storage.snapshotAddressBook(book), storage.snapshotAddressBook(restored));
        assertEquals(new BigDecimal("-300.00"), Ledger.balance(restored, TypicalPersons.ALICE.getId()));
        assertTrue(Files.readString(storage.getAddressBookFilePath()).contains("\"schemaVersion\" : 16"));
        restored.addLedgerEntry(LedgerEntry.reverse(payment, LocalDate.of(2026, 1, 4)));
        assertNotEquals(book, restored);
        restored.resetData(book);
        assertEquals(book, restored);
        restored.removePerson(TypicalPersons.ALICE);
        assertTrue(restored.getLedgerEntries().isEmpty());
        assertFalse(book.getLedgerEntries().isEmpty());
    }

    @Test
    public void legacy_noFinancialFields_defaultsEmpty() throws Exception {
        for (String version : List.of("", "\"schemaVersion\":14,", "\"schemaVersion\":15,")) {
            AddressBook book = JsonUtil.fromJsonString("{" + version + "\"persons\":[]}",
                    JsonSerializableAddressBook.class).toModelType();
            assertTrue(book.getLedgerEntries().isEmpty());
        }
    }

    @Test
    public void load_danglingDuplicateAndInvalidEntries_rejects() {
        LedgerEntry payment = LedgerEntry.payment(TypicalPersons.ALICE.getId(), BigDecimal.ONE,
                LocalDate.of(2026, 1, 2), null);
        JsonAdaptedLedgerEntry adapted = new JsonAdaptedLedgerEntry(payment);
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(List.of(), List.of(),
                List.of(), List.of(adapted)).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.ALICE)), List.of(), List.of(),
                List.of(adapted, adapted)).toModelType());
        JsonAdaptedLedgerEntry reversal = new JsonAdaptedLedgerEntry(LedgerEntry.reverse(payment, payment.date()));
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.ALICE)), List.of(), List.of(),
                List.of(reversal)).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLedgerEntry("bad", "bad", "PAYMENT",
                "2026-01-01", "NaN", null, null, null, null, null, false).toModelType());
    }
}
