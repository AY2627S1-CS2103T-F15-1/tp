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
