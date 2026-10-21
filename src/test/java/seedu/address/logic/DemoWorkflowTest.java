package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class DemoWorkflowTest {
    @TempDir
    private Path directory;

    private final ModelManager model = new ModelManager();

    private LogicManager logic() {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(directory.resolve("data.json")),
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    @Test
    public void demo_emptyOnly_requiresConfirmationAndNeverReplacesRecords() throws Exception {
        LogicManager logic = logic();
        assertTrue(logic.execute("demo").getFeedbackToUser().contains("demo confirm"));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("demo confirm");
        assertFalse(model.getAddressBook().getPersonList().isEmpty());
        int count = model.getAddressBook().getPersonList().size();
        assertThrows(CommandException.class, () -> logic.execute("demo confirm"));
        assertEquals(count, model.getAddressBook().getPersonList().size());
    }

}
