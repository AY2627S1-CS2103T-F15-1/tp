package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalLessons;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_addressBookWithLessons_roundTripSuccess() throws Exception {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        JsonSerializableAddressBook dataToSave = new JsonSerializableAddressBook(addressBook);
        assertEquals(addressBook, dataToSave.toModelType());
    }

    @Test
    public void toModelType_noLessonsInFile_loadsPersonsWithoutLessons() throws Exception {
        // the typical persons file was written before lessons existed
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        assertEquals(List.of(), dataFromFile.toModelType().getLessonList());
    }

    @Test
    public void toModelType_duplicateLessons_throwsIllegalValueException() {
        Lesson sameAliceMath = new LessonBuilder(TypicalLessons.ALICE_MATH).withVenue("Online").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addLesson(TypicalLessons.ALICE_MATH);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(),
                List.of(new JsonAdaptedLesson(TypicalLessons.ALICE_MATH), new JsonAdaptedLesson(sameAliceMath)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_LESSON,
                data::toModelType);
    }

    @Test
    public void toModelType_invalidLesson_throwsIllegalValueException() {
        JsonAdaptedLesson invalidLesson = new JsonAdaptedLesson("Alice Pauline", "94351253", "Math", "2026-02-30",
                "16:30", 90, null, null, null, null);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(invalidLesson));
        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

}
