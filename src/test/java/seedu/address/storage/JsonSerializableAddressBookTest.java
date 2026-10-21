package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TaskBuilder;
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
    public void toModelType_lessonWithoutStudent_throwsIllegalValueException() {
        // the persons list has Bob, but the lesson is with Alice
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(TypicalPersons.BOB)),
                List.of(new JsonAdaptedLesson(TypicalLessons.ALICE_MATH)));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_LESSON_WITHOUT_STUDENT,
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

    @Test
    public void toModelType_addressBookWithTasks_roundTripSuccess() throws Exception {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        addressBook.addTask(new TaskBuilder().withId("s").withDue(LocalDate.of(2026, 12, 1)).build());
        addressBook.addTask(new TaskBuilder().withId("l").withLesson(TypicalLessons.ALICE_MATH).done().build());

        JsonSerializableAddressBook dataToSave = new JsonSerializableAddressBook(addressBook);

        assertEquals(addressBook, dataToSave.toModelType());
    }

    @Test
    public void toModelType_noTasksInFile_loadsWithoutTasks() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        assertEquals(List.of(), dataFromFile.toModelType().getTaskList());
    }

    @Test
    public void toModelType_duplicateTaskIds_throwsIllegalValueException() {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        JsonAdaptedTask task = new JsonAdaptedTask(new TaskBuilder().build());
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                addressBook.getPersonList().stream().map(JsonAdaptedPerson::new).toList(), List.of(),
                List.of(task, task));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_TASK,
                data::toModelType);
    }

    @Test
    public void toModelType_taskWithoutStudentOrLesson_throwsIllegalValueException() {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        var persons = addressBook.getPersonList().stream().map(JsonAdaptedPerson::new).toList();
        JsonAdaptedTask unknownStudent = new JsonAdaptedTask(new TaskBuilder()
                .withStudent(new PersonBuilder().withName("Nobody Here").withPhone("90000000").build()).build());
        Lesson missingLesson = new LessonBuilder(TypicalLessons.ALICE_MATH).withDate("2031-01-01").build();
        JsonAdaptedTask unknownLesson = new JsonAdaptedTask(new TaskBuilder().withLesson(missingLesson).build());

        for (JsonAdaptedTask task : List.of(unknownStudent, unknownLesson)) {
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(persons, List.of(
                    new JsonAdaptedLesson(TypicalLessons.ALICE_MATH)), List.of(task));
            assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_TASK_WITHOUT_TARGET,
                    data::toModelType);
        }
    }

    @Test
    public void toModelType_nullTask_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(),
                java.util.Collections.singletonList(null));
        assertThrows(IllegalValueException.class, "Tasks list contains a null task.", data::toModelType);
    }
}
