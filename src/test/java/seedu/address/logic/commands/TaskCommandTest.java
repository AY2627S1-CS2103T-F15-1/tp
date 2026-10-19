package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class TaskCommandTest {
    @TempDir
    public Path directory;
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    private CommandResult run(String input) throws Exception {
        return parser.parseCommand(input).execute(model);
    }

    /** Runs a command that must be refused with a message for the user, and returns that message. */
    private String rejection(String input) {
        Exception e = assertThrows(Exception.class, () -> run(input));
        assertTrue(e instanceof CommandException || e instanceof ParseException,
                "Expected a message for the user but got " + e);
        return e.getMessage();
    }

    private void parents() throws Exception {
        model.addPerson(new PersonBuilder().withSubjects("Math").build());
        run("lesson add st/1 d/" + LocalDate.now() + " t/10:00");
        run("hw add st/1 desc/Worksheet due/tmr");
        run("hw list");
        run("pay st/1 amt/100");
        run("pay list");
    }

    @Test
    public void tasks_allFourLinks_statusEditArchiveAndReversalPreserveHistory() throws Exception {
        parents();
        run("task add st/1 desc/Call parent due/tmr");
        run("task add lesson/1 desc/Prepare slides");
        run("task add hw/1 desc/Check worksheet");
        run("task add pay/1 desc/Send receipt");
        String shown = run("task list").getFeedbackToUser();
        assertTrue(shown.contains("Call parent"));
        assertTrue(shown.contains("Prepare slides"));
        assertTrue(shown.contains("Worksheet"));
        assertTrue(shown.contains("100.00"));
        run("task done 1");
        run("task edit 2 desc/Prepare practice slides due/today");
        run("pay reverse 1");
        run("archive 1");
        String history = run("task list all/").getFeedbackToUser();
        assertTrue(history.contains("done"));
        assertTrue(history.contains("archived"));
        assertTrue(history.contains("reversed"));
        assertTrue(history.contains("Prepare practice slides"));
        assertFalse(run("task list").getFeedbackToUser().contains("Call parent"));
    }

    @Test
    public void taskSelection_dueEditsAdditionsAndDeletion_neverRetargetShownIndices() throws Exception {
        parents();
        run("task add st/1 desc/First due/tmr");
        run("task add st/1 desc/Second");
        run("task list");
        run("task add st/1 desc/Earlier due/" + LocalDate.now().minusDays(1));
        run("task edit 2 desc/Second edited due/today");
        run("task delete 1");
        assertTrue(run("task list all/").getFeedbackToUser().contains("First"));
        // Refresh selects Earlier, Second edited, First by due date and creation order.
        run("task list all/");
        run("task delete 1 confirm");
        assertEquals("That task was deleted. Run 'task list' to refresh.", rejection("task done 1"));
        run("task done 2");
        String all = run("task list all/").getFeedbackToUser();
        assertFalse(all.contains("Earlier"));
        assertTrue(all.contains("Second edited"));
        assertTrue(all.contains("done"));
    }

    @Test
    public void taskParents_homeworkAndStudentDeletion_requireConfirmationAndCascade() throws Exception {
        parents();
        run("task add hw/1 desc/Review later");
        assertTrue(run("hw delete 1").getFeedbackToUser().contains("task"));
        assertEquals(1, model.getAddressBook().getHomeworkList().size());
        run("hw delete 1 confirm");
        assertFalse(run("task list all/").getFeedbackToUser().contains("Review later"));
        run("task add st/1 desc/Call family");
        assertTrue(run("delete 1").getFeedbackToUser().contains("task"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        run("delete 1 confirm");
        assertFalse(run("task list all/").getFeedbackToUser().contains("Call family"));
    }

    @Test
    public void tasks_restartClearAndDeletedShownParents_preserveOrRejectLinks() throws Exception {
        parents();
        run("task add pay/1 desc/Receipt due/tmr");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("tasks.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), restored.getAddressBook());
        assertEquals(model.getAddressBook().hashCode(), restored.getAddressBook().hashCode());
        Command done = parser.parseCommand("task done 1");
        Exception notShown = assertThrows(CommandException.class, () -> done.execute(restored));
        assertEquals("Invalid task index. Run 'task list' first.", notShown.getMessage());
        assertTrue(parser.parseCommand("task list").execute(restored).getFeedbackToUser().contains("Receipt"));
        parser.parseCommand("task done 1").execute(restored);
        storage.saveAddressBook(restored.getAddressBook());
        assertEquals(restored.getAddressBook(), storage.readAddressBook().orElseThrow());
        run("clear confirm");
        assertEquals("That homework was deleted. Run 'hw list' to refresh.",
                rejection("task add hw/1 desc/Dangling"));
        assertEquals("That payment was deleted. Run 'pay list' to refresh.", rejection("task add pay/1 desc/Dangling"));
        assertFalse(run("task list all/").getFeedbackToUser().contains("Receipt"));
    }

    @Test
    public void tasks_invalidFieldsOrMultipleLinks_rejectWithoutMutation() throws Exception {
        parents();
        AddressBook before = new AddressBook(model.getAddressBook());
        String[][] cases = {
            {"task add desc/No link", "choose exactly one link"},
            {"task add st/1 hw/1 desc/Two links", "choose exactly one link"},
            {"task add st/1 desc/", "Empty value for desc/."},
            {"task add st/1 desc/" + "x".repeat(201), "Task description must be 1-200 printable one-line characters."},
            {"task add st/1 desc/Bad date due/2026-02-30", "Dates must be in YYYY-MM-DD or DD/MM/YYYY format"},
            {"task list all/yes", "The 'all/' flag does not take a value."},
            {"task done 0", "Index must be a positive integer."}};
        for (String[] testCase : cases) {
            assertTrue(rejection(testCase[0]).contains(testCase[1]), testCase[0]);
            assertEquals(before, model.getAddressBook());
        }
    }
}
