package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LessonHistoryWorkflowTest {
    @TempDir
    private Path directory;

    private final ModelManager model = new ModelManager();

    private LogicManager logic() {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(directory.resolve("data.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void student_editAndPreparation_retainsHistoricalLevelAfterRestart() throws Exception {
        LogicManager logic = logic();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        UUID studentId = model.getFilteredPersonList().getFirst().getId();
        logic.execute("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        assertTrue(logic.execute("lesson list st/1 all/").getFeedbackToUser().contains("Level at booking: P1"));
        UUID lessonId = model.getFilteredLessonList().getFirst().getId();
        logic.execute("lesson prep 1 n/Bring fraction cards");
        logic.execute("student edit 1 n/Alicia l/P2");
        Lesson lesson = model.getAddressBook().getLessonList().getFirst();
        assertEquals("P1", lesson.getAcademicLevel().orElseThrow().value);
        assertEquals("Bring fraction cards", lesson.getPreparation().orElseThrow());
        assertTrue(lesson.isScheduled());
        assertEquals(studentId, lesson.getStudentId().orElseThrow());
        assertEquals(lessonId, lesson.getId());
        logic.execute("lesson done 1 n/Taught fractions");
        logic.execute("lesson prep 1 n/Review next time");
        assertEquals("Taught fractions", model.getFilteredLessonList().getFirst().getNotes().orElseThrow());
        assertEquals("completed", model.getFilteredLessonList().getFirst().getStatus().toString());
        var loaded = new JsonAddressBookStorage(directory.resolve("data.json")).readAddressBook().orElseThrow();
        assertEquals("P1", loaded.getLessonList().getFirst().getAcademicLevel().orElseThrow().value);
        assertEquals(lessonId, loaded.getLessonList().getFirst().getId());
    }


}
