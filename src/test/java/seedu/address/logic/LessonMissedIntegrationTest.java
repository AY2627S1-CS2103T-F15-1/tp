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

import seedu.address.logic.commands.LessonMissedCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LessonBuilder;

/**
 * Exercises missed lessons through the real parser, model, student overview and JSON storage.
 */
public class LessonMissedIntegrationTest {

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
    public void execute_markMissed_survivesReloadAndAppearsInHistory() throws Exception {
        Lesson original = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().minusDays(1))
                .withVenue("Online").withNotes("Prepared algebra").build();
        model.addLesson(original);
        logic.execute("lesson list st/1 all/");
        logic.execute("lesson missed 1");

        Model restored = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(original.miss(), restored.getAddressBook().getLessonList().get(0));
        Logic restoredLogic = createLogic(restored);
        assertTrue(restoredLogic.execute("view 1").getFeedbackToUser().contains("1 missed"));
        restoredLogic.execute("lesson list st/1");
        assertTrue(restored.getFilteredLessonList().isEmpty());
        restoredLogic.execute("lesson list st/1 all/");
        assertEquals(1, restored.getFilteredLessonList().size());
        assertEquals(LessonMissedCommand.MESSAGE_ALREADY_MISSED,
                restoredLogic.execute("lesson missed 1").getFeedbackToUser());

        // A mistaken missed status can be corrected using the existing completion command.
        restoredLogic.execute("lesson done 1");
        assertEquals(original.complete(null), addressBookStorage.readAddressBook().orElseThrow()
                .getLessonList().get(0));
    }

    @Test
    public void execute_invalidStatusOrDate_doesNotChangeSavedData() throws Exception {
        Lesson original = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build();
        Lesson[] rejected = {new LessonBuilder(original).withDate(LocalDate.now().plusDays(10)).build(),
            original.cancel("Student unwell"), original.complete("Covered algebra")};
        for (Lesson lesson : rejected) {
            model.addLesson(lesson);
            logic.execute("lesson list st/1 all/");
            String savedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
            assertThrows(CommandException.class, () -> logic.execute("lesson missed 1"));
            assertEquals(savedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
            assertEquals(lesson, model.getAddressBook().getLessonList().get(0));
            model.deleteLesson(lesson);
        }
    }

    @Test
    public void execute_invalidArgumentsOrIndex_doesNotChangeSavedData() throws Exception {
        Lesson original = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build();
        model.addLesson(original);
        logic.execute("lesson list st/1 all/");
        String savedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute("lesson missed 1 n/Notes"));
        assertThrows(CommandException.class, () -> logic.execute("lesson missed 2"));
        assertEquals(savedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(original, model.getAddressBook().getLessonList().get(0));
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userprefs.json"))));
    }
}
