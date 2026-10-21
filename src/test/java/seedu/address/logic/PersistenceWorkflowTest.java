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

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that every change is written before success is reported, and what is said when a write fails. */
public class PersistenceWorkflowTest {
    private static final String ADD_AMY = "add" + NAME_DESC_AMY + PHONE_DESC_AMY + LEVEL_DESC_AMY + SUBJECT_DESC_MATH
            + RATE_DESC_AMY;

    @TempDir
    public Path temporaryFolder;

    private Path dataFile;
    private IOException failure;
    private int writeCount;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        dataFile = temporaryFolder.resolve("data").resolve("tutorflow-v14.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                writeCount++;
                if (failure != null) {
                    throw failure;
                }
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private String rejection(String commandText) {
        return assertThrows(CommandException.class, () -> logic.execute(commandText)).getMessage();
    }

    @Test
    public void execute_mutatingCommand_writesTheDataFileBeforeReportingSuccess() throws Exception {
        logic.execute(ADD_AMY);

        assertEquals(1, writeCount);
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.readString(dataFile).contains("Amy Bee"));
    }

    @Test
    public void execute_commandsThatOnlyShow_neverWrite() throws Exception {
        logic.execute(ADD_AMY);
        writeCount = 0;

        for (String command : new String[] {"help", "list", "find amy", "view 1", "today", "agenda", "hw list",
            "lesson list st/1", "student list"}) {
            logic.execute(command);
        }

        assertEquals(0, writeCount);
    }

    @Test
    public void execute_failedWrite_reportsTheReasonThenTheUnsavedChangesLine() {
        failure = new IOException("No space left on device");

        assertEquals("Your change was applied in this session but could not be saved: No space left on device. "
                + "Fix this before closing TutorFlow.\n"
                + "Unsaved changes: type 'save' to retry before closing TutorFlow.", rejection(ADD_AMY));
        assertTrue(logic.hasUnsavedChanges());
    }

    @Test
    public void execute_unwritableFile_reportsThatTutorFlowCannotWriteIt() {
        failure = new AccessDeniedException("/data/tutorflow-v14.json");

        assertEquals("Your change was applied in this session but could not be saved: TutorFlow cannot write to "
                + "/data/tutorflow-v14.json. Fix this before closing TutorFlow.\n"
                + "Unsaved changes: type 'save' to retry before closing TutorFlow.", rejection(ADD_AMY));
    }

    @Test
    public void execute_failureWithoutAMessage_namesTheKindOfFailure() {
        failure = new IOException();

        assertEquals("Your change was applied in this session but could not be saved: IOException. "
                + "Fix this before closing TutorFlow.\n"
                + "Unsaved changes: type 'save' to retry before closing TutorFlow.", rejection(ADD_AMY));
    }

    @Test
    public void save_afterAFailedWrite_retriesUntilItSucceeds() throws Exception {
        failure = new IOException("Disk full");
        rejection(ADD_AMY);

        assertEquals("Your change was applied in this session but could not be saved: Disk full. "
                + "Fix this before closing TutorFlow.\n"
                + "Unsaved changes: type 'save' to retry before closing TutorFlow.", rejection("save"));
        assertTrue(logic.hasUnsavedChanges());

        failure = null;
        assertEquals("All changes have been saved.", logic.execute("save").getFeedbackToUser());
        assertFalse(logic.hasUnsavedChanges());
        assertTrue(Files.readString(dataFile).contains("Amy Bee"));
    }

    @Test
    public void save_nothingPending_reportsSavedWithoutWriting() throws Exception {
        assertEquals("All changes have been saved.", logic.execute("save").getFeedbackToUser());
        assertEquals(0, writeCount);
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void save_withParameters_isRejectedAndNothingIsWritten() {
        ParseException e = assertThrows(ParseException.class, () -> logic.execute("save now"));

        assertEquals("The 'save' command takes no parameters.", e.getMessage());
        assertEquals(0, writeCount);
    }

    @Test
    public void execute_afterAFailedWrite_unsavedLineKeepsFollowingEveryResult() throws Exception {
        failure = new IOException("Disk full");
        rejection(ADD_AMY);

        assertTrue(logic.execute("help").getFeedbackToUser()
                .endsWith("\nUnsaved changes: type 'save' to retry before closing TutorFlow."));
        assertThrows(CommandException.class, () -> logic.execute("exit"));
    }
}
