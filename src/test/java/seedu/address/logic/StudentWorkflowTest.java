package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class StudentWorkflowTest {
    @TempDir
    private Path directory;

    private final ModelManager model = new ModelManager();

    private LogicManager logic() {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(directory.resolve("data.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void student_filtersAndArchive_restorePreservesLessonsAndIds() throws Exception {
        LogicManager logic = logic();
        logic.execute("student add n/Alice p/91234567 l/P1 s/Math r/50");
        logic.execute("student add n/Bob p/92345678 l/P2 s/Math r/60");
        logic.execute("student add n/Charlie p/93456789 l/P1 s/Science r/70");
        logic.execute("student list l/P1 s/Math");
        assertEquals(1, model.getFilteredPersonList().size());
        UUID id = model.getFilteredPersonList().getFirst().getId();
        logic.execute("lesson add st/1 d/" + LocalDate.now().plusDays(1) + " t/10:00");
        logic.execute("archive 1");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(1, model.getAddressBook().getLessonList().size());
        logic.execute("student list archived/");
        assertEquals(id, model.getFilteredPersonList().getFirst().getId());
        assertThrows(CommandException.class, () -> logic.execute(
                "lesson add st/1 d/" + LocalDate.now().plusDays(2) + " t/10:00"));
        logic.execute("restore 1");
        logic.execute("student list all/");
        assertEquals(3, model.getFilteredPersonList().size());
        assertFalse(model.getAddressBook().getPersonList().getFirst().isArchived());
    }


}
