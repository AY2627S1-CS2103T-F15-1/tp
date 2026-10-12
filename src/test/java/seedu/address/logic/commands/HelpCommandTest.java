package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {

    private Model model = new ModelManager();

    @Test
    public void constructor_nullCommandName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HelpCommand(null));
    }

    @Test
    public void execute_noCommandName_listsEveryCommandWithSummary() throws Exception {
        String message = new HelpCommand().execute(model).getFeedbackToUser();

        assertTrue(message.startsWith(HelpCommand.MESSAGE_ALL_COMMANDS));
        for (String command : new String[] {"add", "edit", "delete", "list", "find", "view", "sort", "remark",
            "lesson add", "lesson list", "lesson move", "lesson cancel", "lesson done", "lesson missed",
            "agenda", "today", "clear", "help", "exit"}) {
            assertTrue(message.contains("\n  " + command + " "), command + " is missing from the list");
        }
        assertTrue(message.contains("Schedules a lesson"));
    }

    @Test
    public void execute_everyListedCommand_showsItsUsage() throws Exception {
        assertEquals(AddCommand.MESSAGE_USAGE, helpFor("add"));
        assertEquals(EditCommand.MESSAGE_USAGE, helpFor("edit"));
        assertEquals(DeleteCommand.MESSAGE_USAGE, helpFor("delete"));
        assertEquals(ListCommand.MESSAGE_USAGE, helpFor("list"));
        assertEquals(FindCommand.MESSAGE_USAGE, helpFor("find"));
        assertEquals(ViewCommand.MESSAGE_USAGE, helpFor("view"));
        assertEquals(SortCommand.MESSAGE_USAGE, helpFor("sort"));
        assertEquals(RemarkCommand.MESSAGE_USAGE, helpFor("remark"));
        assertEquals(LessonAddCommand.MESSAGE_USAGE, helpFor("lesson add"));
        assertEquals(LessonListCommand.MESSAGE_USAGE, helpFor("lesson list"));
        assertEquals(LessonMoveCommand.MESSAGE_USAGE, helpFor("lesson move"));
        assertEquals(LessonCancelCommand.MESSAGE_USAGE, helpFor("lesson cancel"));
        assertEquals(LessonDoneCommand.MESSAGE_USAGE, helpFor("lesson done"));
        assertEquals(LessonMissedCommand.MESSAGE_USAGE, helpFor("lesson missed"));
        assertEquals(AgendaCommand.MESSAGE_USAGE, helpFor("agenda"));
        assertEquals(TodayCommand.MESSAGE_USAGE, helpFor("today"));
        assertEquals(ClearCommand.MESSAGE_USAGE, helpFor("clear"));
        assertEquals(HelpCommand.MESSAGE_USAGE, helpFor("help"));
        assertEquals(ExitCommand.MESSAGE_USAGE, helpFor("exit"));
    }

    @Test
    public void execute_extraSpacesInCommandName_stillFindsCommand() throws Exception {
        assertEquals(LessonAddCommand.MESSAGE_USAGE, helpFor("  lesson    add  "));
    }

    @Test
    public void execute_partialCommandName_listsMatchingSubCommands() throws Exception {
        String message = helpFor("lesson");

        assertTrue(message.startsWith(String.format(HelpCommand.MESSAGE_COMMANDS_STARTING_WITH, "lesson")));
        assertTrue(message.contains("lesson add"));
        assertTrue(message.contains("lesson done"));
        assertFalse(message.contains("agenda"));
    }

    @Test
    public void execute_unknownCommandName_throwsCommandException() {
        assertCommandFailure(new HelpCommand("foo"), model, String.format(HelpCommand.MESSAGE_NO_HELP, "foo"));
        // case matters, as it does for every command
        assertCommandFailure(new HelpCommand("ADD"), model, String.format(HelpCommand.MESSAGE_NO_HELP, "ADD"));
        // a prefix of a command word is not a command
        assertCommandFailure(new HelpCommand("les"), model, String.format(HelpCommand.MESSAGE_NO_HELP, "les"));
    }

    @Test
    public void equals() {
        HelpCommand command = new HelpCommand("add");

        assertTrue(command.equals(command)); // same object
        assertTrue(command.equals(new HelpCommand(" add ")));
        assertTrue(new HelpCommand().equals(new HelpCommand("")));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new HelpCommand("edit")));
    }

    @Test
    public void toStringMethod() {
        String expected = HelpCommand.class.getCanonicalName() + "{commandName=lesson add}";
        assertEquals(expected, new HelpCommand("lesson   add").toString());
    }

    private String helpFor(String commandName) throws Exception {
        return new HelpCommand(commandName).execute(model).getFeedbackToUser();
    }
}
