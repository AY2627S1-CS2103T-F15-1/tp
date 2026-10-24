package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;

public class JsonAdaptedLedgerEntryTest {
    @Test
    public void toModelType_validEntry_roundTripsEveryField() throws Exception {
        LedgerEntry entry = LedgerEntry.payment(ALICE, new BigDecimal("12.30"), LocalDate.of(2026, 10, 1));
        assertEquals(entry, new JsonAdaptedLedgerEntry(entry).toModelType());
    }

    @Test
    public void toModelType_invalidRequiredFields_rejected() {
        String[] valid = {UUID.randomUUID().toString(), ALICE.getId().toString(), UUID.randomUUID().toString(),
            "CHARGE", "20.00", "2026-10-01"};
        for (int i = 0; i < valid.length; i++) {
            String[] missing = valid.clone();
            missing[i] = null;
            String field = new String[]{"id", "studentId", "lessonId", "kind", "amount", "date"}[i];
            assertThrows(IllegalValueException.class, "Ledger entry field '" + field + "' is missing.", () ->
                    entry(missing).toModelType());
        }
        String[] invalid = {"bad-id", "bad-student-id", "bad-lesson-id", "UNKNOWN", "-1.00", "2026-99-99"};
        for (int i = 0; i < valid.length; i++) {
            String[] fields = valid.clone();
            fields[i] = invalid[i];
            assertThrows(IllegalValueException.class, () -> entry(fields).toModelType());
        }
        assertThrows(IllegalValueException.class, () -> entry(new String[]{valid[0], valid[1], valid[2],
            "PAYMENT", "0", valid[5]}).toModelType());
        assertThrows(IllegalValueException.class, () -> entry(new String[]{valid[0], valid[1], valid[2],
            "CHARGE", "1.001", valid[5]}).toModelType());
    }

    @Test
    public void toModelType_duplicateOrOrphanEntries_rejected() {
        JsonAdaptedLedgerEntry payment = new JsonAdaptedLedgerEntry(
                LedgerEntry.payment(ALICE, BigDecimal.TEN, LocalDate.of(2026, 10, 1)));
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(ALICE)), List.of(), List.of(), List.of(payment, payment)).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(), List.of(), List.of(), List.of(payment)).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(ALICE)), List.of(), List.of(),
                Arrays.asList((JsonAdaptedLedgerEntry) null))
                .toModelType());
    }

    @Test
    public void toModelType_legacyFileWithoutLedger_startsWithNoDebt() throws Exception {
        assertTrue(new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE)), List.of())
                .toModelType().getLedgerEntries().isEmpty());
    }

    @Test
    public void paymentNotes_roundTripAndLegacyAbsentNotes_areSupported() throws Exception {
        LedgerEntry payment = LedgerEntry.payment(ALICE, BigDecimal.TEN, LocalDate.of(2026, 10, 18), "PayNow ref 4471");
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addLedgerEntry(payment);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(payment, restored.getLedgerEntries().getFirst());
        assertEquals("PayNow ref 4471", restored.getLedgerEntries().getFirst().notes());
        assertEquals(null, new JsonAdaptedLedgerEntry(payment.id().toString(), ALICE.getId().toString(),
                null, "PAYMENT", "10.00", "2026-10-18").toModelType().notes());
        for (String notes : List.of("", "a".repeat(201), "line\nbreak")) {
            assertThrows(IllegalValueException.class, () -> new JsonAdaptedLedgerEntry(payment.id().toString(),
                    ALICE.getId().toString(), null, "PAYMENT", "10.00", "2026-10-18", notes).toModelType());
        }
    }

    @Test
    public void storedStudentRenamed_ledgerKeepsStableLinksAndAmounts() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        Lesson lesson = new LessonBuilder().withStudent(ALICE).build().withStudent(ALICE);
        book.addLesson(lesson);
        book.chargeLesson(lesson, lesson.complete(null));
        LedgerEntry charge = book.getLedgerEntries().getFirst();
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        String renamed = json.replace("\"name\" : \"" + ALICE.getName() + "\"", "\"name\" : \"Alice Renamed\"")
                .replace("\"phone\" : \"" + ALICE.getPhone() + "\"", "\"phone\" : \"91234567\"");
        AddressBook restored = JsonUtil.fromJsonString(renamed, JsonSerializableAddressBook.class).toModelType();
        Person student = restored.getPersonList().getFirst();
        assertEquals("Alice Renamed", student.getName().fullName);
        assertEquals("91234567", student.getPhone().value);
        assertEquals(ALICE.getId(), student.getId());
        assertEquals(charge, restored.getLedgerEntries().getFirst());
        assertTrue(restored.getLedgerEntries().getFirst().isWith(student));
        assertEquals(lesson.getId(), restored.getLedgerEntries().getFirst().lessonId());
        assertTrue(json.contains("\"schemaVersion\" : 17"));
    }

    @Test
    public void schema_legacyVersionsDefaultEmptyLedgerAndFutureIsRefused() throws Exception {
        for (String version : List.of("", "\"schemaVersion\":14,", "\"schemaVersion\":15,")) {
            AddressBook book = JsonUtil.fromJsonString("{" + version + "\"persons\":[]}",
                    JsonSerializableAddressBook.class).toModelType();
            assertTrue(book.getLedgerEntries().isEmpty());
        }
        assertThrows(UnsupportedSchemaException.class, () -> JsonUtil.fromJsonString(
                "{\"schemaVersion\":18,\"persons\":[]}", JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void storedCharge_missingLessonWrongStudentAndDuplicateLesson_areRejected() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        Lesson lesson = new LessonBuilder().withStudent(ALICE).build();
        book.addLesson(lesson);
        book.chargeLesson(lesson, lesson.complete(null));
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        String orphan = json.replace("\"lessonId\" : \"" + lesson.getId() + "\"",
                "\"lessonId\" : \"" + UUID.randomUUID() + "\"");
        assertThrows(IllegalValueException.class, () ->
                JsonUtil.fromJsonString(orphan, JsonSerializableAddressBook.class).toModelType());
        JsonAdaptedLedgerEntry duplicate = new JsonAdaptedLedgerEntry(UUID.randomUUID().toString(),
                ALICE.getId().toString(), lesson.getId().toString(), "CHARGE", "20.00", "2026-10-01");
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(ALICE)), List.of(new JsonAdaptedLesson(lesson)), List.of(),
                List.of(new JsonAdaptedLedgerEntry(book.getLedgerEntries().getFirst()), duplicate)).toModelType());
        String wrongStudent = json.replace("\"studentId\" : \"" + ALICE.getId() + "\"",
                "\"studentId\" : \"" + UUID.randomUUID() + "\"");
        assertThrows(IllegalValueException.class, () ->
                JsonUtil.fromJsonString(wrongStudent, JsonSerializableAddressBook.class).toModelType());
    }

    private static JsonAdaptedLedgerEntry entry(String[] fields) {
        return new JsonAdaptedLedgerEntry(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);
    }
}
