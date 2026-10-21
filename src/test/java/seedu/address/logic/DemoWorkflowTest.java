package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Follows a demo from the command line: it stands in for the user's data, is never saved, and gives it back. */
public class DemoWorkflowTest {
    private static final String ADD_AMY = "add n/Amy Tan p/91234567 l/S3 s/Math r/50";

    @TempDir
    public Path directory;

    private Path dataFile;
    private int writeCount;
    private IOException writeFailure;
    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        dataFile = directory.resolve("data.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                writeCount++;
                if (writeFailure != null) {
                    throw writeFailure;
                }
                super.saveAddressBook(addressBook);
            }
        };
        model = new ModelManager();
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("prefs.json"))));
    }

    private List<String> names() {
        return model.getAddressBook().getPersonList().stream().map(student -> student.getName().fullName).toList();
    }

    private String rejection(String command) {
        return assertThrows(CommandException.class, () -> logic.execute(command)).getMessage();
    }

    @Test
    public void preview_saysWhatItWouldDoAndChangesNothing() throws Exception {
        logic.execute(ADD_AMY);

        String message = logic.execute("demo").getFeedbackToUser();

        assertEquals("Load 6 sample students for exploration. Type demo confirm to load them.\n"
                + "Your own data is kept safe and nothing in the demo is saved. Type demo exit to go back to it.",
                message);
        assertEquals(List.of("Amy Tan"), names());
        assertFalse(logic.isDemoMode());
    }

    @Test
    public void preview_offersTheConfirmingCommand() throws Exception {
        assertEquals(Optional.of("demo confirm"), logic.execute("demo").getConfirmationCommand());
        assertTrue(logic.execute("demo confirm").getConfirmationCommand().isEmpty());
    }

    @Test
    public void start_onEmptyData_loadsSamplesWithoutWritingAnything() throws Exception {
        String message = logic.execute("demo confirm").getFeedbackToUser();

        assertEquals("Sample students loaded. Try student list and student view 1.\n"
                + "Demo mode: nothing here is saved, and your own data is safe. Type demo exit to go back to it.",
                message);
        assertTrue(logic.isDemoMode());
        assertEquals(6, names().size());
        assertEquals(0, writeCount);
        assertFalse(Files.exists(dataFile));
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void start_onRecords_keepsThemSafeAndExitBringsThemBackUntouched() throws Exception {
        logic.execute(ADD_AMY);
        logic.execute("lesson add st/1 d/" + LocalDate.now().plusDays(1) + " t/16:30");
        logic.execute("hw add st/1 desc/Algebra due/tmr");
        byte[] fileBefore = Files.readAllBytes(dataFile);
        AddressBook before = new AddressBook(model.getAddressBook());
        int writes = writeCount;

        logic.execute("demo confirm");
        assertEquals(6, names().size());
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());

        assertEquals("Demo ended. Your own data is back.", logic.execute("demo exit").getFeedbackToUser());

        assertFalse(logic.isDemoMode());
        assertEquals(before, model.getAddressBook());
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
        assertEquals(1, model.getAddressBook().getLessonList().size());
        assertEquals(writes, writeCount, "starting and leaving a demo write nothing");
        assertArrayEquals(fileBefore, Files.readAllBytes(dataFile));
    }

    @Test
    public void whileInTheDemo_nothingYouDoIsSavedAndNothingIsUnsaved() throws Exception {
        logic.execute(ADD_AMY);
        writeFailure = new IOException("Disk full");
        int writes = writeCount;
        logic.execute("demo confirm");

        logic.execute("add n/Zed Lim p/92345678 l/J1 s/Physics r/60");
        logic.execute("delete 1 confirm");
        logic.execute("hw add st/1 desc/Sample homework due/tmr");
        logic.execute("clear confirm");

        assertEquals(writes, writeCount);
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        logic.execute("demo exit");
        assertEquals(List.of("Amy Tan"), names());
        assertFalse(logic.hasUnsavedChanges());
    }

    @Test
    public void saveAndExitInTheDemo_neverWrite() throws Exception {
        logic.execute("demo confirm");

        assertEquals("Nothing is saved in the demo. Type demo exit to go back to your own data.",
                logic.execute("save").getFeedbackToUser());
        assertTrue(logic.execute("exit").isExit());

        assertEquals(0, writeCount);
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void demoAlreadyRunning_isReportedForEveryFormOfStartingIt() throws Exception {
        logic.execute("demo confirm");

        String expected = "You are already in the demo. Type demo exit to go back to your own data.";
        assertEquals(expected, rejection("demo confirm"));
        assertEquals(expected, rejection("demo"));
        assertEquals(6, names().size());
    }

    @Test
    public void exitWithoutADemo_isReported() {
        assertEquals("There is no demo to leave. Type demo to see what it would do.", rejection("demo exit"));
    }

    @Test
    public void start_withUnsavedChanges_isRefusedUntilTheyAreSaved() throws Exception {
        writeFailure = new IOException("Disk full");
        assertThrows(CommandException.class, () -> logic.execute(ADD_AMY));
        assertTrue(logic.hasUnsavedChanges());

        assertEquals("You have unsaved changes. Type 'save' first, so that the demo cannot lose them.",
                rejection("demo confirm"));
        assertFalse(logic.isDemoMode());
        assertEquals(List.of("Amy Tan"), names());

        writeFailure = null;
        logic.execute("save");
        logic.execute("demo confirm");
        assertTrue(logic.isDemoMode());
        logic.execute("demo exit");
        assertEquals(List.of("Amy Tan"), names());
        assertTrue(Files.readString(dataFile).contains("Amy Tan"));
    }

    @Test
    public void preview_withUnsavedChanges_isStillAllowed() throws Exception {
        writeFailure = new IOException("Disk full");
        assertThrows(CommandException.class, () -> logic.execute(ADD_AMY));

        assertTrue(logic.execute("demo").getFeedbackToUser().startsWith("Load 6 sample students"));
    }

    @Test
    public void invalidDemoCommand_isInvalidCommandFormat() {
        assertThrows(ParseException.class, () -> logic.execute("demo now"));
    }
}
