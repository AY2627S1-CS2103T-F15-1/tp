package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class IdentityContinuityTest {
    @Test
    public void replace_equalCancelledLessons_updatesSelectedRecord() {
        Lesson first = new LessonBuilder().build().cancel("Unavailable");
        Lesson second = new LessonBuilder().build().cancel("Unavailable");
        assertEquals(first, second);
        AddressBook book = new AddressBook();
        book.addLesson(first);
        book.addLesson(second);
        book.setLesson(second, second.complete("Second record"));
        assertEquals(first, book.getLessonList().get(0));
        assertNotEquals(first, book.getLessonList().get(1));
    }

    @Test
    public void serialize_legacyRecords_assignsPersistentIdentifiers() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        book.addLesson(new LessonBuilder().withStudent(TypicalPersons.ALICE).build());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        assertTrue(json.contains("\"id\""));
        assertTrue(json.contains("\"studentId\""));
        assertTrue(json.contains("\"schemaVersion\""));
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(json, JsonUtil.toJsonString(new JsonSerializableAddressBook(restored)));
    }
    @Test
    public void roundTrip_equalCancelledLessons_keepDistinctIdsAndUpdateOnlySecond() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        book.addLesson(new LessonBuilder().withStudent(TypicalPersons.ALICE).build().cancel("Unavailable"));
        book.addLesson(new LessonBuilder().withStudent(TypicalPersons.ALICE).build().cancel("Unavailable"));
        AddressBook restored = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)),
                JsonSerializableAddressBook.class).toModelType();
        Lesson first = restored.getLessonList().get(0);
        Lesson second = restored.getLessonList().get(1);
        assertEquals(first, second);
        assertNotEquals(first.getId(), second.getId());
        restored.setLesson(second, second.prepare("Second cancellation"));
        assertTrue(restored.getLessonList().get(0).getPreparation().isEmpty());
        assertEquals("Second cancellation", restored.getLessonList().get(1).getPreparation().orElseThrow());
        assertTrue(first.getAcademicLevel().isEmpty());
    }

    @Test
    public void deserialize_unsupportedVersionOrDuplicateIds_rejectsData() throws Exception {
        AddressBook book = TypicalPersons.getTypicalAddressBook();
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        String future = json.replace("\"schemaVersion\" : " + StorageSchema.CURRENT_VERSION, "\"schemaVersion\" : 99");
        assertNotEquals(json, future);
        assertThrows(IllegalValueException.class, () ->
                 JsonUtil.fromJsonString(future, JsonSerializableAddressBook.class).toModelType());
        String duplicate = json.replace(book.getPersonList().get(1).getId().toString(),
                book.getPersonList().get(0).getId().toString());
        assertThrows(IllegalValueException.class, () ->
                 JsonUtil.fromJsonString(duplicate, JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void reset_copyRetainsMetadataAndIds_andNewMetadataChangesEquality() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE.withArchived(true));
        Lesson lesson = new LessonBuilder().withStudent(TypicalPersons.ALICE).build()
                .snapshotStudent(TypicalPersons.ALICE).prepare("Cards");
        book.addLesson(lesson);
        AddressBook copy = new AddressBook(book);
        assertEquals(book, copy);
        assertEquals(lesson.getId(), copy.getLessonList().getFirst().getId());
        assertEquals(lesson.getAcademicLevel(), copy.getLessonList().getFirst().getAcademicLevel());
        assertTrue(copy.getPersonList().getFirst().isArchived());
        copy.setLesson(lesson, lesson.prepare("Workbook"));
        assertNotEquals(book, copy);
        copy.resetData(book);
        assertEquals(book, copy);
    }

}
