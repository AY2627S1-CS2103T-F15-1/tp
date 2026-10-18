package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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
                "{\"schemaVersion\":16,\"persons\":[]}", JsonSerializableAddressBook.class).toModelType());
        assertEquals(15, StorageSchema.CURRENT_VERSION);
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
