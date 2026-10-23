package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises follow-up tasks (user story 31) through the parser, model and real JSON storage, the way a tutor would
 * type the commands.
 */
public class TasksIntegrationTest {

    private static final LocalDate DAY = LocalDate.now().plusDays(10);

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = createLogic(model);
    }

    @Test
    public void execute_taskLifecycle_isSavedAtEveryStep() throws Exception {
        logic.execute("task add st/1 desc/Call parent due/" + LocalDate.now().plusDays(1));
        assertEquals(1, saved().getTaskList().size());

        String list = logic.execute("task list").getFeedbackToUser();
        assertTrue(list.startsWith("Tasks (1)\n1. [open] Call parent due " + LocalDate.now().plusDays(1)));

        logic.execute("task edit 1 desc/Call parent again due/-");
        assertEquals("Call parent again", saved().getTaskList().get(0).getDescription());
        assertTrue(saved().getTaskList().get(0).getDue().isEmpty());

        logic.execute("task done 1");
        assertTrue(saved().getTaskList().get(0).isDone());
        assertFalse(logic.execute("task list").getFeedbackToUser().contains("Call parent again"));
        assertTrue(logic.execute("task list all/").getFeedbackToUser().contains("[done] Call parent again"));

        logic.execute("task list all/");
        logic.execute("task delete 1 confirm");
        assertTrue(saved().getTaskList().isEmpty());
    }

    @Test
    public void execute_lessonTaskFollowsLessonAndStudentChanges() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/16:00");
        logic.execute("agenda d/" + DAY);
        logic.execute("task add lesson/1 desc/Prepare mock paper");

        logic.execute("lesson move 1 t/18:00");
        logic.execute("edit 1 n/Alice Tan");

        String list = logic.execute("task list").getFeedbackToUser();
        assertTrue(list.contains("Alice Tan"), list);
        assertTrue(list.contains("lesson Math " + saved().getLessonList().get(0).getDate() + " 18:00-19:00"), list);
    }

    @Test
    public void execute_cancelledSlotRebookedThenMoved_eachTaskStaysWithItsOwnLesson() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/16:00");
        logic.execute("lesson list st/1");
        logic.execute("task add lesson/1 desc/Original lesson task");
        logic.execute("lesson cancel 1 r/Student unwell");
        logic.execute("lesson add st/1 d/" + DAY + " t/16:00"); // the same slot, booked again
        logic.execute("lesson list st/1");
        logic.execute("task add lesson/1 desc/Rebooked lesson task");

        logic.execute("lesson move 1 d/" + DAY.plusDays(1));

        var lessons = saved().getLessonList();
        var cancelled = lessons.stream().filter(lesson -> !lesson.isScheduled()).findFirst().get();
        var rebooked = lessons.stream().filter(lesson -> lesson.isScheduled()).findFirst().get();
        assertEquals(DAY, cancelled.getDate().value);
        assertEquals(DAY.plusDays(1), rebooked.getDate().value);
        var original = saved().getTaskList().stream()
                .filter(task -> task.getDescription().equals("Original lesson task")).findFirst().get();
        var second = saved().getTaskList().stream()
                .filter(task -> task.getDescription().equals("Rebooked lesson task")).findFirst().get();
        assertEquals(cancelled.getId(), original.getLessonId().get());
        assertEquals(rebooked.getId(), second.getLessonId().get());

        String list = logic.execute("task list").getFeedbackToUser();
        String originalRow = list.lines().filter(line -> line.contains("Original lesson task")).findFirst().get();
        String rebookedRow = list.lines().filter(line -> line.contains("Rebooked lesson task")).findFirst().get();
        assertTrue(originalRow.contains("lesson Math " + cancelled.getDate() + " 16:00"), originalRow);
        assertTrue(originalRow.toLowerCase().contains("cancelled"), originalRow);
        assertTrue(rebookedRow.contains("lesson Math " + rebooked.getDate() + " 16:00"), rebookedRow);
    }

    @Test
    public void execute_homeworkTask_isLinkedByIdAndDeletedWithTheHomework() throws Exception {
        logic.execute("hw add st/1 desc/Algebra Q1-10 due/" + DAY);
        logic.execute("hw list");
        logic.execute("task add hw/1 desc/Mark the algebra");
        logic.execute("task add st/1 desc/Call parent");

        var homework = saved().getHomeworkList().get(0);
        var task = saved().getTaskList().stream()
                .filter(each -> each.getDescription().equals("Mark the algebra")).findFirst().get();
        assertEquals(homework.getId(), task.getHomeworkId().get());
        assertEquals(ALICE.getId(), task.getStudentId());
        String list = logic.execute("task list").getFeedbackToUser();
        assertTrue(list.contains("homework Algebra Q1-10 due " + DAY), list);

        logic.execute("hw edit 1 desc/Algebra Q1-12"); // editing homework keeps the link
        assertTrue(logic.execute("task list").getFeedbackToUser().contains("homework Algebra Q1-12"));

        // Deleting homework with a linked task needs confirmation, and nothing is deleted without it
        String prompt = logic.execute("hw delete 1").getFeedbackToUser();
        assertTrue(prompt.contains("1 linked task(s)") && prompt.contains("hw delete 1 confirm"), prompt);
        assertEquals(2, saved().getTaskList().size());
        assertEquals(1, saved().getHomeworkList().size());

        logic.execute("hw delete 1 confirm");
        assertEquals(1, saved().getTaskList().size());
        assertEquals("Call parent", saved().getTaskList().get(0).getDescription());
    }

    @Test
    public void execute_taskListForStudent_includesTheirLessonAndHomeworkTasksOnly() throws Exception {
        logic.execute("lesson add st/1 d/" + DAY + " t/16:00");
        logic.execute("lesson list st/1");
        logic.execute("task add lesson/1 desc/Prepare mock paper");
        logic.execute("hw add st/1 desc/Algebra Q1-10 due/" + DAY);
        logic.execute("hw list");
        logic.execute("task add hw/1 desc/Mark the algebra");
        logic.execute("task add st/1 desc/Call parent");
        logic.execute("task add st/2 desc/Other student");

        // changing the student and the lesson does not change which tasks belong to the student
        logic.execute("edit 1 n/Alice Tan p/90000001");
        logic.execute("lesson move 1 d/" + DAY.plusDays(1));

        String alice = logic.execute("task list st/1").getFeedbackToUser();
        assertTrue(alice.startsWith("Tasks (3)"), alice);
        assertTrue(alice.contains("Prepare mock paper"), alice);
        assertTrue(alice.contains("Mark the algebra"), alice);
        assertTrue(alice.contains("Call parent"), alice);
        assertFalse(alice.contains("Other student"), alice);

        String benson = logic.execute("task list st/2").getFeedbackToUser();
        assertTrue(benson.startsWith("Tasks (1)"), benson);
        assertTrue(benson.contains("Other student"), benson);
    }

    @Test
    public void execute_deletingStudentWithTasks_needsConfirmAndTakesTasksAlong() throws Exception {
        logic.execute("task add st/1 desc/Call parent");
        logic.execute("task add st/2 desc/Other student");

        String prompt = logic.execute("delete 1").getFeedbackToUser();
        assertTrue(prompt.contains("1 task(s)"), prompt);
        assertEquals(2, saved().getTaskList().size());

        logic.execute("delete 1 confirm");
        assertEquals(1, saved().getTaskList().size());
        assertEquals("Other student", saved().getTaskList().get(0).getDescription());
    }

    @Test
    public void execute_clear_removesTasksToo() throws Exception {
        logic.execute("task add st/1 desc/Call parent");

        logic.execute("clear confirm");

        assertTrue(saved().getTaskList().isEmpty());
    }

    private ReadOnlyAddressBook saved() throws Exception {
        return addressBookStorage.readAddressBook().orElseThrow();
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json"))));
    }
}
