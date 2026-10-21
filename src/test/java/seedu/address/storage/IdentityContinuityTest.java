package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
}
