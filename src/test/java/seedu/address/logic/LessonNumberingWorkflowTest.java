package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LessonNumberingWorkflowTest {
    private static final String NUMBERS_CHANGED = "Lesson numbers have changed";

    @TempDir
    private Path directory;

    private final ModelManager model = new ModelManager();

    private LogicManager logic() {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(directory.resolve("data.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void rename_sortedLessons_refreshesNumbersBeforeNextCommand() throws Exception {
        LogicManager logic = logic();
        String date = LocalDate.now().plusDays(1).toString();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        logic.execute("student add n/Bob p/92345678 l/P2 s/Math r/60");
        logic.execute("lesson add st/1 d/" + date + " t/10:00");
        logic.execute("lesson add st/2 d/" + date + " t/10:00");
        logic.execute("agenda d/" + date);
        String result = logic.execute("student edit 1 n/Zoe").getFeedbackToUser();
        assertTrue(result.contains("Current lesson list:"));
        assertTrue(result.contains("1. Bob"));
        assertTrue(result.contains("2. Zoe"));
        logic.execute("lesson prep 1 n/For Bob");
        assertEquals("Bob", model.getFilteredLessonList().getFirst().getStudentName().fullName);
        assertEquals("For Bob", model.getFilteredLessonList().getFirst().getPreparation().orElseThrow());
    }

    @Test
    public void add_earlierFilteredLesson_displaysNewNumbersForNineTenAndTwelve() throws Exception {
        LogicManager logic = logic();
        String date = LocalDate.now().plusDays(1).toString();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        logic.execute("lesson add st/1 d/" + date + " t/10:00");
        logic.execute("lesson add st/1 d/" + date + " t/12:00");
        logic.execute("lesson list st/1");
        String result = logic.execute("lesson add st/1 d/" + date + " t/09:00").getFeedbackToUser();
        assertTrue(result.contains("Current lesson list:"));
        assertTrue(result.contains("1. Alice"));
        assertTrue(result.contains("2. Alice"));
        assertTrue(result.contains("3. Alice"));
        logic.execute("lesson prep 2 n/For ten oclock");
        assertEquals("10:00", model.getFilteredLessonList().get(1).getTime().toString());
        assertEquals("For ten oclock", model.getFilteredLessonList().get(1).getPreparation().orElseThrow());
    }

    @Test
    public void rename_saveFailure_stillShowsChangedNumbers() throws Exception {
        LogicManager logic = logic();
        String date = LocalDate.now().plusDays(1).toString();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        logic.execute("student add n/Bob p/92345678 l/P2 s/Math r/60");
        logic.execute("lesson add st/1 d/" + date + " t/10:00");
        logic.execute("lesson add st/2 d/" + date + " t/10:00");
        logic.execute("agenda d/" + date);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(directory.resolve("data.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                throw new IOException("Read only");
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
        CommandException error = assertThrows(CommandException.class, () ->
                failingLogic.execute("student edit 1 n/Zoe"));
        assertTrue(error.getMessage().contains("Current lesson list:"));
        assertTrue(error.getMessage().contains("1. Bob"));
        assertTrue(failingLogic.hasUnsavedChanges());
    }

    @Test
    public void move_lessonKeepsItsNumber_doesNotSayNumbersChanged() throws Exception {
        LogicManager logic = logicWithTwoLessons();
        String result = logic.execute("lesson move 1 t/10:30").getFeedbackToUser();
        assertFalse(result.contains(NUMBERS_CHANGED), result);
        logic.execute("lesson prep 1 n/For Alice");
        assertEquals("For Alice", model.getFilteredLessonList().getFirst().getPreparation().orElseThrow());
        assertEquals("Alice", model.getFilteredLessonList().getFirst().getStudentName().fullName);
    }

    @Test
    public void move_pastTheNextLesson_saysNumbersChanged() throws Exception {
        LogicManager logic = logicWithTwoLessons();
        String result = logic.execute("lesson move 1 t/13:00").getFeedbackToUser();
        assertTrue(result.contains(NUMBERS_CHANGED), result);
        assertTrue(result.contains("1. Bob"), result);
        assertTrue(result.contains("2. Alice"), result);
    }

    @Test
    public void cancel_lessonStaysInTheAgenda_doesNotSayNumbersChanged() throws Exception {
        LogicManager logic = logicWithTwoLessons();
        String result = logic.execute("lesson cancel 2").getFeedbackToUser();
        assertFalse(result.contains(NUMBERS_CHANGED), result);
        assertEquals(2, model.getFilteredLessonList().size());
    }

    @Test
    public void rename_lessonKeepsItsNumber_doesNotSayNumbersChanged() throws Exception {
        LogicManager logic = logicWithTwoLessons();
        String result = logic.execute("student edit 1 n/Amy").getFeedbackToUser();
        assertFalse(result.contains(NUMBERS_CHANGED), result);
        assertEquals("Amy", model.getFilteredLessonList().getFirst().getStudentName().fullName);
    }

    /** Returns a logic whose agenda shows Alice's lesson at 10:00 as 1 and Bob's at 12:00 as 2. */
    private LogicManager logicWithTwoLessons() throws Exception {
        LogicManager logic = logic();
        String date = LocalDate.now().plusDays(1).toString();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        logic.execute("student add n/Bob p/92345678 l/P2 s/Math r/60");
        logic.execute("lesson add st/1 d/" + date + " t/10:00");
        logic.execute("lesson add st/2 d/" + date + " t/12:00");
        logic.execute("agenda d/" + date);
        return logic;
    }
}
