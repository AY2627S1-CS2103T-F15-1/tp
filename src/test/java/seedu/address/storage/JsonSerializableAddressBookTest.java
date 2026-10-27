package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;
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
    public void toModelType_missingBufferUsesDefaultAndInvalidBufferRejected() throws Exception {
        JsonSerializableAddressBook legacy = JsonUtil.fromJsonString("{\"persons\":[]}",
                JsonSerializableAddressBook.class);
        assertEquals(30, legacy.toModelType().getTravelBuffer());
        for (int invalid : new int[]{-1, 181}) {
            JsonSerializableAddressBook data = JsonUtil.fromJsonString("{\"persons\":[],\"travelBuffer\":"
                    + invalid + "}", JsonSerializableAddressBook.class);
            assertThrows(IllegalValueException.class, data::toModelType);
        }
    }

    @Test
    public void toModelType_sortedAddressBook_keepsItsOrderForLaterAdditions() throws Exception {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        addressBook.sortPersons(StudentOrder.DESCENDING);

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();
        assertEquals(addressBook, restored);
        restored.addPerson(new PersonBuilder().withName("Bob Choo").withPhone("90001234").build());

        List<String> names = restored.getPersonList().stream().map(person -> person.getName().fullName).toList();
        assertEquals(names.stream().sorted(String.CASE_INSENSITIVE_ORDER.reversed()).toList(), names);
    }

    @Test
    public void toModelType_missingOrInvalidStudentOrder() throws Exception {
        JsonSerializableAddressBook legacy = JsonUtil.fromJsonString("{\"persons\":[]}",
                JsonSerializableAddressBook.class);
        assertTrue(legacy.toModelType().getStudentOrder().isEmpty());
        JsonSerializableAddressBook invalid = JsonUtil.fromJsonString("{\"persons\":[],\"studentOrder\":\"SIDEWAYS\"}",
                JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_INVALID_STUDENT_ORDER,
                invalid::toModelType);
    }

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
                addressBook.getPersonList().stream().map(JsonAdaptedPerson::new).toList(), List.of(), List.of(),
                List.of(), List.of(task, task));
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_TASK,
                data::toModelType);
    }

    @Test
    public void toModelType_taskWithoutItsTarget_isSkippedAndTheRestOfTheFileLoads() throws Exception {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        Homework alicesHomework = Homework.assign(TypicalPersons.ALICE.getId(), "Practice set",
                LocalDate.of(2030, 1, 1));
        addressBook.addHomework(alicesHomework);
        String alice = TypicalPersons.ALICE.getId().toString();
        String benson = TypicalPersons.BENSON.getId().toString();
        String aliceMath = TypicalLessons.ALICE_MATH.getId().toString();
        String missing = UUID.randomUUID().toString();
        List<JsonAdaptedPerson> persons = addressBook.getPersonList().stream().map(JsonAdaptedPerson::new).toList();
        List<JsonAdaptedLesson> lessons = addressBook.getLessonList().stream().map(JsonAdaptedLesson::new).toList();
        List<JsonAdaptedHomework> homework = addressBook.getHomeworkList().stream()
                .map(JsonAdaptedHomework::new).toList();
        String a = TaskBuilder.idOf("a").toString();
        String b = TaskBuilder.idOf("b").toString();
        String c = TaskBuilder.idOf("c").toString();
        String d = TaskBuilder.idOf("d").toString();
        String e = TaskBuilder.idOf("e").toString();
        List<JsonAdaptedTask> unresolvable = List.of(
                // no such student
                new JsonAdaptedTask(a, "Call", null, false, missing, null, null),
                // no such lesson
                new JsonAdaptedTask(b, "Call", null, false, alice, missing, null),
                // Alice's lesson
                new JsonAdaptedTask(c, "Call", null, false, benson, aliceMath, null),
                // no such homework
                new JsonAdaptedTask(d, "Call", null, false, alice, null, missing),
                // Alice's homework
                new JsonAdaptedTask(e, "Call", null, false, benson, null, alicesHomework.getId().toString()));

        for (JsonAdaptedTask task : unresolvable) {
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(persons, lessons, homework,
                    List.of(), List.of(task));
            AddressBook loaded = data.toModelType();
            assertTrue(loaded.getTaskList().isEmpty());
            assertEquals(addressBook.getPersonList(), loaded.getPersonList());
            assertEquals(addressBook.getLessonList(), loaded.getLessonList());
            assertEquals(addressBook.getHomeworkList(), loaded.getHomeworkList());
        }

        // one stale task never costs the valid tasks that are stored beside it
        JsonAdaptedTask valid = new JsonAdaptedTask(TaskBuilder.idOf("ok").toString(), "Call", null, false,
                alice, null, null);
        JsonSerializableAddressBook mixed = new JsonSerializableAddressBook(persons, lessons, homework, List.of(),
                List.of(unresolvable.get(0), valid, unresolvable.get(3)));
        assertEquals(List.of(TaskBuilder.idOf("ok")),
                mixed.toModelType().getTaskList().stream().map(Task::getId).toList());
    }

    @Test
    public void toModelType_cancelledAndRebookedSlot_eachTaskKeepsItsOwnLessonThroughJson() throws Exception {
        AddressBook addressBook = TypicalLessons.getTypicalAddressBookWithLessons();
        Lesson cancelled = TypicalLessons.ALICE_MATH.cancel("Student unwell");
        addressBook.setLesson(TypicalLessons.ALICE_MATH, cancelled);
        Lesson rebooked = new LessonBuilder(TypicalLessons.ALICE_MATH).withId(UUID.randomUUID()).build();
        addressBook.addLesson(rebooked);
        Homework homework = Homework.assign(TypicalPersons.ALICE.getId(), "Practice set", LocalDate.of(2030, 1, 1));
        addressBook.addHomework(homework);
        addressBook.addTask(new TaskBuilder().withId("old").withLesson(cancelled).build());
        addressBook.addTask(new TaskBuilder().withId("new").withLesson(rebooked).build());
        addressBook.addTask(new TaskBuilder().withId("hw").withHomework(homework).build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();

        assertEquals(addressBook, restored);
        assertEquals(cancelled.getId(), restored.findTask(TaskBuilder.idOf("old")).get().getLessonId().get());
        assertEquals(rebooked.getId(), restored.findTask(TaskBuilder.idOf("new")).get().getLessonId().get());
        assertEquals(homework.getId(), restored.findTask(TaskBuilder.idOf("hw")).get().getHomeworkId().get());
        assertEquals(TypicalPersons.ALICE.getId(), restored.findTask(TaskBuilder.idOf("hw")).get().getStudentId());
        // moving the rebooked lesson after the reload still changes nothing about the cancelled lesson's task
        Lesson moved = rebooked.reschedule(new LessonDate("2031-05-04"), rebooked.getTime(),
                rebooked.getDuration(), null);
        Lesson restoredRebooked = restored.getLessonList().stream()
                .filter(lesson -> lesson.getId().equals(rebooked.getId())).findFirst().get();
        restored.setLesson(restoredRebooked, moved);
        assertEquals(addressBook.getTaskList(), restored.getTaskList());
    }

    @Test
    public void toModelType_nullTask_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(), List.of(), List.of(),
                java.util.Collections.singletonList(null));
        assertThrows(IllegalValueException.class, "Tasks list contains a null task.", data::toModelType);
    }
}
