package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LessonBuilder;

/**
 * Exercises lesson completion through the application's parser, model and real JSON storage.
 */
public class LessonDoneIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private final Model model = new ModelManager();
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model.addPerson(ALICE);
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = createLogic(model);
    }

    @Test
    public void execute_completeAndReviseNotes_survivesReloadAndAppearsInOverview() throws Exception {
        Lesson original = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().minusDays(1)).build();
        model.addLesson(original);
        logic.execute("lesson list st/1 all/");
        logic.execute("lesson done 1 n/Covered   algebra. x² = 4.");

        Model restoredModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        Lesson restored = restoredModel.getAddressBook().getLessonList().get(0);
        assertEquals(original.complete("Covered algebra. x² = 4."), restored);
        Logic restoredLogic = createLogic(restoredModel);
        String overview = restoredLogic.execute("view 1").getFeedbackToUser();
        assertTrue(overview.contains("Covered algebra. x² = 4."));
        assertTrue(overview.contains("1 completed"));

        restoredLogic.execute("lesson list st/1 all/");
        restoredLogic.execute("lesson done 1 n/Next lesson: factorisation.");
        restoredLogic.execute("lesson done 1");
        Lesson revised = addressBookStorage.readAddressBook().orElseThrow().getLessonList().get(0);
        assertEquals(LessonStatus.COMPLETED, revised.getStatus());
        assertEquals("Next lesson: factorisation.", revised.getNotes().orElseThrow());
        assertEquals(1, restoredModel.getAddressBook().getLessonList().size());
    }

    @Test
    public void execute_futureLesson_doesNotChangeSavedData() throws Exception {
        Lesson future = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(10)).build();
        model.addLesson(future);
        logic.execute("lesson list st/1 all/");
        String savedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute("lesson done 1 n/Notes"));

        assertEquals(savedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(future, model.getAddressBook().getLessonList().get(0));
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json"))));
    }
}
