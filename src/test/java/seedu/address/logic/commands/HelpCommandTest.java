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
            "lesson repeat",
            "lesson add", "lesson list", "lesson move", "lesson cancel", "lesson done", "lesson missed",
            "agenda", "today", "clear", "help", "exit"}) {
            assertTrue(message.contains("\n  " + command + " "), command + " is missing from the list");
        }
        assertTrue(message.contains("Schedules a lesson"));
    }

    @Test
    public void execute_overviewAndHelpUsage_pointToFullOfflineGuide() throws Exception {
        assertTrue(helpFor("").contains("Help menu or F1"));
        assertTrue(helpFor("help").contains("Help menu or F1"));
        assertTrue(helpFor("lesson").contains("Help menu or F1"));
        assertTrue(helpFor("lesson done").contains("Help menu or F1"));
    }

    @Test
    public void execute_everyListedCommand_showsItsUsage() throws Exception {
        assertHelpUsage(AddCommand.MESSAGE_USAGE, "add");
        assertHelpUsage(EditCommand.MESSAGE_USAGE, "edit");
        assertHelpUsage(DeleteCommand.MESSAGE_USAGE, "delete");
        assertHelpUsage(ListCommand.MESSAGE_USAGE, "list");
        assertHelpUsage(FindCommand.MESSAGE_USAGE, "find");
        assertHelpUsage(ViewCommand.MESSAGE_USAGE, "view");
        assertHelpUsage(SortCommand.MESSAGE_USAGE, "sort");
        assertHelpUsage(RemarkCommand.MESSAGE_USAGE, "remark");
        assertHelpUsage(LessonAddCommand.MESSAGE_USAGE, "lesson add");
        assertHelpUsage(LessonRepeatCommand.MESSAGE_USAGE, "lesson repeat");
        assertHelpUsage(LessonListCommand.MESSAGE_USAGE, "lesson list");
        assertHelpUsage(LessonMoveCommand.MESSAGE_USAGE, "lesson move");
        assertHelpUsage(LessonCancelCommand.MESSAGE_USAGE, "lesson cancel");
        assertHelpUsage(LessonDoneCommand.MESSAGE_USAGE, "lesson done");
        assertHelpUsage(LessonMissedCommand.MESSAGE_USAGE, "lesson missed");
        assertHelpUsage(AgendaCommand.MESSAGE_USAGE, "agenda");
        assertHelpUsage(TodayCommand.MESSAGE_USAGE, "today");
        assertHelpUsage(ClearCommand.MESSAGE_USAGE, "clear");
        assertHelpUsage(HelpCommand.MESSAGE_USAGE, "help");
        assertHelpUsage(ExitCommand.MESSAGE_USAGE, "exit");
    }

    @Test
    public void execute_extraSpacesInCommandName_stillFindsCommand() throws Exception {
        assertHelpUsage(LessonAddCommand.MESSAGE_USAGE, "  lesson    add  ");
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

    private void assertHelpUsage(String usage, String commandName) throws Exception {
        assertEquals(usage + "\n" + HelpCommand.MESSAGE_USER_GUIDE, helpFor(commandName));
    }

    private String helpFor(String commandName) throws Exception {
        return new HelpCommand(commandName).execute(model).getFeedbackToUser();
    }
}
