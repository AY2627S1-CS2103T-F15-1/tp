package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class AlphaReportedIssuesTest {
    @TempDir
    private Path directory;

    private final ModelManager model = new ModelManager();

    private JsonAddressBookStorage storage() {
        return new JsonAddressBookStorage(directory.resolve("data.json"));
    }

    private LogicManager logic(ModelManager currentModel) {
        return new LogicManager(currentModel, new StorageManager(storage(),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void find_unicodeCaseVariants_matchTheSameStudentIdentity() throws Exception {
        LogicManager logic = logic(model);
        logic.execute("add n/Işık p/91234567 l/S3 s/Math r/50");
        Person student = model.getFilteredPersonList().getFirst();

        logic.execute("find ışık");

        assertEquals(List.of(student), model.getFilteredPersonList());
    }

    @Test
    public void summaries_sameNameStudents_includePhonesEvenWhenOtherStudentIsFilteredOut() throws Exception {
        LogicManager logic = logic(model);
        logic.execute("add n/Alex p/91234567 l/S3 s/Math r/50");
        logic.execute("add n/alex p/92345678 l/S3 s/Math r/50");
        String yesterday = LocalDate.now().minusDays(1).toString();
        logic.execute("lesson add st/1 d/" + yesterday + " t/15:00 past/");
        logic.execute("lesson add st/2 d/" + yesterday + " t/16:00 past/");
        logic.execute("agenda d/" + yesterday);
        logic.execute("lesson done 1");
        logic.execute("lesson done 2");
        logic.execute("hw add st/1 desc/Math due/today");
        logic.execute("hw add st/2 desc/Math due/today");
        model.updateFilteredPersonList(student -> student.getPhone().value.equals("91234567"));

        String dues = logic.execute("dues").getFeedbackToUser();
        String homework = logic.execute("hw list").getFeedbackToUser();

        assertTrue(dues.contains("Alex (phone ending 4567) $50.00"));
        assertTrue(dues.contains("alex (phone ending 5678) $50.00"));
        assertTrue(homework.contains("Alex (phone ending 4567) Math"));
        assertTrue(homework.contains("alex (phone ending 5678) Math"));
        assertFalse(dues.contains("91234567"));
        assertFalse(homework.contains("92345678"));
        String dashboard = logic.execute("today").getFeedbackToUser();
        assertFalse(dashboard.contains("91234567"));
        assertFalse(dashboard.contains("92345678"));
        assertTrue(logic.execute("view 1").getFeedbackToUser().contains("Alex (phone ending 4567) Math"));
        assertEquals(2, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void edit_clearOptionalContactFields_preservesIdentityHistoryAndAbsentValuesAfterRestart() throws Exception {
        LogicManager logic = logic(model);
        logic.execute("add n/Alice p/91234567 l/S3 s/Math r/50 e/alice@example.com v/Old venue");
        logic.execute("lesson add st/1 d/" + LocalDate.now().plusDays(1) + " t/15:00");
        logic.execute("hw add st/1 desc/Algebra due/today");
        Person original = model.getFilteredPersonList().getFirst();
        var lessons = List.copyOf(model.getAddressBook().getLessonList());
        var homework = List.copyOf(model.getAddressBook().getHomeworkList());
        logic.execute("edit 1 e/");
        assertEquals(original.getVenue(), model.getFilteredPersonList().getFirst().getVenue());
        logic.execute("edit 1 v/   ");
        logic.execute("edit 1 l/S4");

        ModelManager restarted = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        Person restored = restarted.getFilteredPersonList().getFirst();
        assertEquals(original.getId(), restored.getId());
        assertTrue(restored.getEmail().isEmpty());
        assertTrue(restored.getVenue().isEmpty());
        assertEquals(lessons, restarted.getAddressBook().getLessonList());
        assertEquals(homework, restarted.getAddressBook().getHomeworkList());
        assertThrows(ParseException.class, () -> logic.execute("edit 1 p/"));
        assertThrows(ParseException.class, () -> logic.execute("edit 1 e/ e/new@example.com"));
        assertThrows(ParseException.class, () -> logic.execute("add n/Bob p/92345678 l/S3 s/Math r/50 e/"));
    }

    @Test
    public void summaries_collidingPhoneSuffixes_referenceMatchesTheExplicitStudentOverview() throws Exception {
        LogicManager logic = logic(model);
        logic.execute("add n/Alex p/91234567 l/S3 s/Math r/50");
        logic.execute("add n/Alex p/92344567 l/S3 s/Math r/50");
        var students = model.getAddressBook().getPersonList();
        logic.execute("hw add st/1 desc/Math due/today");
        logic.execute("hw add st/2 desc/Math due/today");
        String homework = logic.execute("hw list").getFeedbackToUser();
        for (int i = 0; i < students.size(); i++) {
            String label = "Alex (phone ending 4567, ref " + students.get(i).getId().toString().substring(0, 8) + ")";
            assertTrue(homework.contains(label));
            assertTrue(logic.execute("view " + (i + 1)).getFeedbackToUser().startsWith("Viewing: " + label));
        }
        assertFalse(homework.contains("91234567"));
        assertFalse(homework.contains("92344567"));
    }

    @Test
    public void feedback_clearAfterLifecycleCorrection_preservesHomeworkAndLinkedTasksAfterRestart() throws Exception {
        LogicManager logic = logic(model);
        logic.execute("add n/Alice p/91234567 l/S3 s/Math r/50");
        logic.execute("hw add st/1 desc/Algebra due/today");
        logic.execute("hw list");
        logic.execute("hw status 1 s/submitted");
        logic.execute("hw feedback 1 f/Wrong assignment");
        logic.execute("task add hw/1 desc/Review correction");
        logic.execute("hw status 1 s/assigned");
        Homework before = model.getAddressBook().getHomeworkList().getFirst();
        var tasks = List.copyOf(model.getAddressBook().getTaskList());

        assertTrue(logic.execute("hw feedback 1 f/").getFeedbackToUser().contains("Feedback cleared"));

        ModelManager restarted = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        Homework restored = restarted.getAddressBook().getHomeworkList().getFirst();
        assertEquals(before.getId(), restored.getId());
        assertEquals(before.getStudentId(), restored.getStudentId());
        assertEquals(before.getDueDate(), restored.getDueDate());
        assertEquals(before.getDescription(), restored.getDescription());
        assertEquals(before.getStatus(), restored.getStatus());
        assertTrue(restored.getFeedback().isEmpty());
        assertEquals(tasks, restarted.getAddressBook().getTaskList());
        LogicManager afterRestart = logic(restarted);
        assertFalse(afterRestart.execute("hw list").getFeedbackToUser().contains("Feedback:"));
        assertTrue(afterRestart.execute("hw feedback 1 f/").getFeedbackToUser().contains("Feedback cleared"));
        assertThrows(ParseException.class, () -> afterRestart.execute("hw feedback 1"));
        assertThrows(ParseException.class, () -> afterRestart.execute("hw feedback 1 f/ f/New"));
    }
}
