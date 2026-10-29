package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.RATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_MATH;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.GEORGE;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Integration tests of Undo with LogicManager, testing undo's response to save failures.
 */
public class UndoIntegrationTest {
    @TempDir
    public static Path temporaryFolder;

    private static JsonAddressBookStorage addressStorage;
    private static boolean willSaveFail;

    @BeforeAll
    public static void initStorage() {
        addressStorage = new JsonAddressBookStorage(temporaryFolder.resolve("retry.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (willSaveFail) {
                    throw new IOException("dummy");
                }
                super.saveAddressBook(addressBook);
            }
        };
    }

    private LogicManager createLogic(Model model) {
        return new LogicManager(model, new StorageManager(
                addressStorage, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    @Test
    public void execute_deleteSaveFailure_undoWorks() throws CommandException, ParseException {
        willSaveFail = true;

        Model model = new ModelManager();
        model.addPerson(GEORGE);
        LogicManager logic = createLogic(model);

        assertThrows(CommandException.class, () -> logic.execute("delete 1 confirm"));
        willSaveFail = false;
        assertEquals(new CommandResult("Undone: student deletion. Your data is back as it was before."),
                logic.execute("undo"));
    }

    @Test
    public void execute_restoreSaveFailure_undoWorks() throws CommandException, ParseException {
        willSaveFail = false;

        Model model = new ModelManager();
        model.addPerson(ELLE);
        model.addPerson(BENSON);
        LogicManager logic = createLogic(model);

        logic.execute("delete 1 confirm");

        willSaveFail = true;
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(logic.hasUnsavedChanges());
        assertTrue(logic.getFilteredPersonList().contains(ELLE));
        assertTrue(logic.getFilteredPersonList().contains(BENSON));

        willSaveFail = false;
        logic.execute("save");
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(logic.getFilteredPersonList().contains(ELLE));
        assertTrue(logic.getFilteredPersonList().contains(BENSON));
    }

    @Test
    public void execute_nonDestructive_undoFails() throws CommandException, ParseException {
        willSaveFail = false;

        Model model = new ModelManager();
        model.addPerson(GEORGE);
        model.addPerson(CARL);
        LogicManager logic = createLogic(model);

        logic.execute("list");

        CommandException ex = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(ex.getMessage().contains("Nothing to undo"));

        logic.execute("add" + NAME_DESC_AMY + PHONE_DESC_AMY
                + LEVEL_DESC_AMY + SUBJECT_DESC_MATH + RATE_DESC_AMY);

        ex = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(ex.getMessage().contains("Nothing to undo"));

        assertThrows(ParseException.class, () -> logic.execute("lesson repeat st/1 d/2026-12-23 t/12:34"));

        ex = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(ex.getMessage().contains("Nothing to undo"));

        logic.execute("lesson repeat st/1 d/2026-12-23 t/12:34 count/34 s/Math");

        ex = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(ex.getMessage().contains("Nothing to undo"));

        assertThrows(CommandException.class, () -> logic.execute(
                "lesson repeat st/1 d/2026-12-23 t/12:34 count/34 s/Math"));

        ex = assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(ex.getMessage().contains("Nothing to undo"));
    }
}
