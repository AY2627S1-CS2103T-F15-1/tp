package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.TypicalPersons;

public class HomeworkStorageTest {
    @Test
    public void roundTrip_historyAndCopy_preserveEveryField() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Homework homework = new Homework(UUID.randomUUID(), TypicalPersons.ALICE.getId(), "Algebra",
                LocalDate.of(2020, 1, 1), HomeworkStatus.REVIEWED, "Check signs");
        book.addHomework(homework);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(book, restored);
        assertEquals(json, JsonUtil.toJsonString(new JsonSerializableAddressBook(restored)));
        AddressBook copy = new AddressBook(book);
        copy.setHomework(homework, homework.withStatus(HomeworkStatus.ASSIGNED));
        assertNotEquals(book, copy);
        assertEquals("Check signs", copy.getHomeworkList().getFirst().getFeedback().orElseThrow());
        assertTrue(copy.getHomeworkList().getFirst().isOverdue(LocalDate.now()));
        copy.resetData(book);
        assertEquals(book, copy);
        copy.removePerson(TypicalPersons.ALICE);
        assertTrue(copy.getHomeworkList().isEmpty());
    }

    @Test
    public void load_knownVersionsWithoutHomework_defaultEmptyAndRejectFuture() throws Exception {
        for (String version : new String[] {"", "\"schemaVersion\":14,", "\"schemaVersion\":15,"}) {
            AddressBook book = JsonUtil.fromJsonString("{" + version + "\"persons\":[]}",
                    JsonSerializableAddressBook.class).toModelType();
            assertTrue(book.getHomeworkList().isEmpty());
        }
        assertThrows(IllegalValueException.class, () -> JsonUtil.fromJsonString(
                "{\"schemaVersion\":" + (StorageSchema.CURRENT_VERSION + 1) + ",\"persons\":[]}",
                JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void load_invalidHomeworkFieldsAndDuplicateRecords_rejects() {
        String id = UUID.randomUUID().toString();
        String student = TypicalPersons.ALICE.getId().toString();
        for (JsonAdaptedHomework item : List.of(
                new JsonAdaptedHomework("bad", student, "A", "2030-01-10", "ASSIGNED", null),
                new JsonAdaptedHomework(id, student, "", "2030-01-10", "ASSIGNED", null),
                new JsonAdaptedHomework(id, student, "A", "2030-02-30", "ASSIGNED", null),
                new JsonAdaptedHomework(id, student, "A", "2030-01-10", "OVERDUE", null),
                new JsonAdaptedHomework(id, null, "A", "2030-01-10", "ASSIGNED", null),
                new JsonAdaptedHomework(id, student, "A", "2030-01-10", "SUBMITTED", "A\nB"))) {
            assertThrows(IllegalValueException.class, item::toModelType);
        }
        JsonAdaptedHomework valid = new JsonAdaptedHomework(id, student, "A", "2020-01-01", "ASSIGNED", "History");
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(List.of(), List.of(),
                List.of(valid)).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.ALICE)), List.of(), List.of(valid, valid)).toModelType());
    }

    @Test
    public void roundTrip_backwardCorrection_retainsFeedbackWhileAssigned() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Homework corrected = Homework.assign(TypicalPersons.ALICE.getId(), "A", LocalDate.of(2020, 1, 1))
                .withStatus(HomeworkStatus.REVIEWED).withFeedback("Keep history").withStatus(HomeworkStatus.ASSIGNED);
        book.addHomework(corrected);
        AddressBook restored = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)),
                JsonSerializableAddressBook.class).toModelType();
        assertEquals(corrected, restored.getHomeworkList().getFirst());
    }

    @Test
    public void replace_equalCopiedTarget_updatesExactIdentity() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Homework original = Homework.assign(TypicalPersons.ALICE.getId(), "Algebra", LocalDate.now());
        book.addHomework(original);
        Homework copied = new Homework(original.getId(), original.getStudentId(), original.getDescription(),
                original.getDueDate(), original.getStatus(), null);
        book.setHomework(copied, copied.withStatus(HomeworkStatus.SUBMITTED));
        assertEquals(HomeworkStatus.SUBMITTED, book.getHomeworkList().getFirst().getStatus());
    }

    @Test
    public void add_duplicateOrOrphan_rejectsWithoutChangingBook() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Homework homework = Homework.assign(TypicalPersons.ALICE.getId(), "Algebra", LocalDate.now());
        book.addHomework(homework);
        assertThrows(IllegalArgumentException.class, () -> book.addHomework(
                Homework.assign(TypicalPersons.ALICE.getId(), "Algebra", LocalDate.now())));
        assertThrows(IllegalArgumentException.class, () -> book.addHomework(
                Homework.assign(UUID.randomUUID(), "Orphan", LocalDate.now())));
        assertEquals(1, book.getHomeworkList().size());
        assertFalse(homework.isOverdue(LocalDate.now()));
        assertTrue(homework.isOverdue(LocalDate.now().plusDays(1)));
        assertFalse(homework.withStatus(HomeworkStatus.SUBMITTED).isOverdue(LocalDate.now().plusDays(1)));
    }
}
