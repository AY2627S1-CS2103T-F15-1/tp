package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UndoWorkflowTest {
    @TempDir
    public Path directory;
    private final ModelManager model = new ModelManager();
    private final boolean[] failSave = {false};
    private JsonAddressBookStorage storage;
    private LogicManager logic;

    private void startSession() {
        storage = new JsonAddressBookStorage(directory.resolve("undo.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook book) throws IOException {
                if (failSave[0]) {
                    throw new IOException("Simulated write failure");
                }
                super.saveAddressBook(book);
            }
        };
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    /** Builds a student with lessons, an outcome, a charge, homework, a payment, all task links and a setting. */
    private void richData() throws Exception {
        startSession();
        logic.execute("add n/Alice Tan p/91234567 l/S3 s/Math r/60 v/Blk 5");
        logic.execute("add n/Bob Lim p/92345678 l/S2 s/Math r/50 v/Jurong");
        logic.execute("lesson repeat st/1 s/Math d/" + LocalDate.now() + " t/10:00 count/3");
        logic.execute("lesson add st/2 s/Math d/" + LocalDate.now().plusDays(2) + " t/15:00");
        logic.execute("agenda week/");
        logic.execute("lesson done 1 n/Covered fractions");
        logic.execute("hw add st/1 desc/Worksheet due/tmr");
        logic.execute("hw list");
        logic.execute("pay st/1 amt/100");
        logic.execute("pay list");
        logic.execute("task add st/1 desc/Call parent");
        logic.execute("task add lesson/1 desc/Prepare slides");
        logic.execute("task add hw/1 desc/Check worksheet");
        logic.execute("task add pay/1 desc/Send receipt");
        logic.execute("task list");
        logic.execute("travel buffer/45");
    }

    private String stored() throws IOException {
        return storage.snapshotAddressBook(model.getAddressBook());
    }

    private String onDisk() throws Exception {
        return storage.snapshotAddressBook(storage.readAddressBook().orElseThrow());
    }

    @Test
    public void undo_studentDeleteWithAllLinkedHistory_restoresExactStateInMemoryAndOnDisk() throws Exception {
        richData();
        String before = stored();
        logic.execute("delete 1 confirm");
        assertNotEquals(before, stored());
        assertTrue(model.getAddressBook().getTaskList().isEmpty());

        logic.execute("undo");

        assertEquals(before, stored());
        assertEquals(before, onDisk());
        assertFalse(logic.hasUnsavedChanges());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(before, stored());
    }

    @Test
    public void undo_clear_restoresEveryDomainAndTheTravelSetting() throws Exception {
        richData();
        String before = stored();
        logic.execute("clear confirm");
        assertEquals(30, model.getAddressBook().getTravelBufferMinutes());
        logic.execute("undo");
        assertEquals(before, stored());
        assertEquals(45, model.getAddressBook().getTravelBufferMinutes());
        assertEquals(before, onDisk());
    }

    @Test
    public void undo_homeworkAndTaskDeletion_restoreTheDeletedRecord() throws Exception {
        richData();
        String before = stored();
        logic.execute("hw delete 1 confirm");
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
        logic.execute("undo");
        assertEquals(before, stored());

        logic.execute("task list");
        logic.execute("task delete 1 confirm");
        assertEquals(3, model.getAddressBook().getTaskList().size());
        logic.execute("undo");
        assertEquals(before, stored());
        assertEquals(before, onDisk());
    }

    @Test
    public void undo_afterAnotherDataChange_isRejectedAndKeepsTheNewWork() throws Exception {
        richData();
        logic.execute("delete 1 confirm");
        logic.execute("add n/Cara Ng p/93456789 l/S1 s/Math r/40");
        String withNewWork = stored();

        CommandException rejection = assertThrows(CommandException.class, () -> logic.execute("undo"));

        assertTrue(rejection.getMessage().startsWith("Nothing to undo"));
        assertEquals(withNewWork, stored());
        assertTrue(model.getUndoSnapshot().isEmpty());
    }

    @Test
    public void undo_afterReadOnlyAndPreviewCommands_stillRestores() throws Exception {
        richData();
        String before = stored();
        logic.execute("delete 1 confirm");
        for (String readOnly : new String[] {"list", "find Bob", "list", "view 1", "agenda", "today", "help",
            "hw list", "pay list", "task list", "dues", "delete 1", "clear", "lesson list st/1 all/"}) {
            logic.execute(readOnly);
            assertTrue(model.getUndoSnapshot().isPresent(), readOnly);
        }
        logic.execute("undo");
        assertEquals(before, stored());
    }

    @Test
    public void snapshot_isOnlyTakenByDestructiveCommandsThatChangeData() throws Exception {
        richData();
        assertTrue(model.getUndoSnapshot().isEmpty());
        logic.execute("edit 2 n/Bobby Lim");
        logic.execute("archive 2");
        logic.execute("list archived/");
        logic.execute("restore 1");
        logic.execute("list");
        logic.execute("hw status 1 s/submitted");
        logic.execute("pay reverse 1");
        assertTrue(model.getUndoSnapshot().isEmpty());
        assertThrows(CommandException.class, () -> logic.execute("delete 99 confirm"));
        assertTrue(model.getUndoSnapshot().isEmpty());
        logic.execute("delete 1");
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertTrue(model.getUndoSnapshot().isEmpty());
    }

    @Test
    public void undo_rejectedDeleteAfterARealOne_keepsTheEarlierSnapshot() throws Exception {
        richData();
        String before = stored();
        logic.execute("delete 1 confirm");
        assertThrows(CommandException.class, () -> logic.execute("delete 9 confirm"));
        logic.execute("undo");
        assertEquals(before, stored());
    }

    @Test
    public void undo_afterFailedSaveOfTheDeletion_stillRestoresAndPersists() throws Exception {
        richData();
        String before = stored();
        failSave[0] = true;
        CommandException saveFailure = assertThrows(CommandException.class, () -> logic.execute("delete 1 confirm"));
        assertTrue(saveFailure.getMessage().contains("could not be saved"));
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(model.getUndoSnapshot().isPresent());

        failSave[0] = false;
        logic.execute("undo");

        assertEquals(before, stored());
        assertEquals(before, onDisk());
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void undo_whenItsOwnSaveFails_keepsRestoredDataUnsavedUntilRetry() throws Exception {
        richData();
        String before = stored();
        logic.execute("delete 1 confirm");
        failSave[0] = true;
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(before, stored());
        assertTrue(logic.hasUnsavedChanges());
        assertNotEquals(before, onDisk());

        failSave[0] = false;
        logic.execute("save");
        assertEquals(before, onDisk());
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void undo_shownHomeworkRows_resolveTheSameRecordAfterRestore() throws Exception {
        richData();
        logic.execute("hw list");
        logic.execute("delete 1 confirm");
        logic.execute("undo");

        logic.execute("hw status 1 s/submitted");

        assertEquals(HomeworkStatus.SUBMITTED, model.getAddressBook().getHomeworkList().getFirst().getStatus());
    }

    @Test
    public void undoLabel_isDeclaredOnlyByDestructiveCommands() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (String destructive : new String[] {"delete 1", "delete 1 confirm", "student delete 1 confirm",
            "clear", "clear confirm", "hw delete 1 confirm", "task delete 1 confirm"}) {
            assertTrue(parser.parseCommand(destructive).undoLabel().isPresent(), destructive);
        }
        for (String other : new String[] {"list", "add n/A p/91234567 l/S1 s/Math r/10", "edit 1 n/B", "archive 1",
            "lesson cancel 1", "pay reverse 1", "charge reverse l/1", "undo", "help", "save", "today"}) {
            assertTrue(parser.parseCommand(other).undoLabel().isEmpty(), other);
        }
    }
}
