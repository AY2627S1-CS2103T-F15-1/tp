package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Follows an import from the command line to the data file. */
public class ImportWorkflowTest {
    private static final String CSV = "name,phone,level,subjects,rate\n"
            + "Amy Tan,91234567,S3,Math,50\nBob Lim,92345678,J1,Physics,62.50\n";

    @TempDir
    public Path folder;

    private Path dataFile;
    private boolean shouldFailWrites;
    private int writeCount;
    private ModelManager model;
    private Logic logic;
    private String importConfirmed;

    @BeforeEach
    public void setUp() throws IOException {
        Path csv = folder.resolve("students.csv");
        Files.writeString(csv, CSV);
        importConfirmed = "import students file/" + csv + " confirm";
        dataFile = folder.resolve("data").resolve("tutorflow-v14.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                writeCount++;
                if (shouldFailWrites) {
                    throw new IOException("Disk full");
                }
                super.saveAddressBook(addressBook);
            }
        };
        model = new ModelManager();
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void preview_neverWritesAndNeverChangesTheData() throws Exception {
        logic.execute("add n/Dee Koh p/93456789 l/S2 s/Math r/40");
        int writes = writeCount;

        logic.execute(importConfirmed.replace(" confirm", ""));

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(writes, writeCount);
    }

    @Test
    public void confirmedImport_isSavedBeforeItIsReported() throws Exception {
        CommandResult result = logic.execute(importConfirmed);

        assertTrue(result.getFeedbackToUser().startsWith("Imported 2 students."));
        assertFalse(logic.hasUnsavedChanges());
        String saved = Files.readString(dataFile);
        assertTrue(saved.contains("Amy Tan") && saved.contains("Bob Lim"));
        assertEquals(2, result.getAddedStudents().size());
    }

    @Test
    public void importThatCannotBeSaved_staysInTheSessionAndIsFlaggedUnsaved() throws Exception {
        shouldFailWrites = true;

        CommandException e = assertThrows(CommandException.class, () -> logic.execute(importConfirmed));

        assertTrue(e.getMessage().endsWith("Unsaved changes: type 'save' to retry before closing TutorFlow."));
        assertTrue(logic.hasUnsavedChanges());
        assertEquals(2, model.getAddressBook().getPersonList().size());

        shouldFailWrites = false;
        assertEquals("All changes have been saved.", logic.execute("save").getFeedbackToUser());
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.readString(dataFile).contains("Amy Tan"));
    }

    @Test
    public void rejectedImport_changesAndWritesNothing() throws Exception {
        logic.execute("add n/Dee Koh p/93456789 l/S2 s/Math r/40");
        int writes = writeCount;
        Files.writeString(folder.resolve("bad.csv"), "name,phone\nAmy,1\n");

        String command = "import students file/" + folder.resolve("bad.csv") + " confirm";
        assertThrows(CommandException.class, () -> logic.execute(command));

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(writes, writeCount);
    }

    @Test
    public void importWithoutFile_isInvalidCommandFormat() {
        assertThrows(ParseException.class, () -> logic.execute("import students"));
    }
}
