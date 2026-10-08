package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonCancelCommand;
import seedu.address.logic.commands.LessonDoneCommand;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.commands.LessonMoveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertTrue(parser.parseCommand(ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased())
                instanceof ViewCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_lessonAdd() throws Exception {
        assertTrue(parser.parseCommand(LessonAddCommand.COMMAND_WORD + " st/1 d/2026-12-22 t/16:30")
                instanceof LessonAddCommand);
    }

    @Test
    public void parseCommand_lessonList() throws Exception {
        assertTrue(parser.parseCommand(LessonListCommand.COMMAND_WORD + " st/1") instanceof LessonListCommand);
    }

    @Test
    public void parseCommand_lessonMove() throws Exception {
        assertTrue(parser.parseCommand(LessonMoveCommand.COMMAND_WORD + " 1 d/2030-01-07")
                instanceof LessonMoveCommand);
    }

    @Test
    public void parseCommand_lessonCancel() throws Exception {
        assertTrue(parser.parseCommand(LessonCancelCommand.COMMAND_WORD + " 1") instanceof LessonCancelCommand);
    }

    @Test
    public void parseCommand_lessonDone() throws Exception {
        assertEquals(new LessonDoneCommand(INDEX_FIRST_PERSON, "Covered algebra"),
                parser.parseCommand("lesson done 1 n/Covered algebra"));
    }

    @Test
    public void parseCommand_agenda() throws Exception {
        assertTrue(parser.parseCommand(AgendaCommand.COMMAND_WORD) instanceof AgendaCommand);
        assertTrue(parser.parseCommand(AgendaCommand.COMMAND_WORD + " week/") instanceof AgendaCommand);
    }

    @Test
    public void parseCommand_today() throws Exception {
        assertTrue(parser.parseCommand(TodayCommand.COMMAND_WORD) instanceof TodayCommand);
        assertTrue(parser.parseCommand(TodayCommand.COMMAND_WORD + " 3") instanceof TodayCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_remark() throws Exception {
        String remarkText = "Likes baseball";
        RemarkCommand command = (RemarkCommand) parser.parseCommand(
                RemarkCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " r/" + remarkText);
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remarkText)), command);
    }

    @Test
    public void parseCommand_sort() throws Exception {
        assertEquals(new SortCommand(false), parser.parseCommand(SortCommand.COMMAND_WORD));
        assertEquals(new SortCommand(false), parser.parseCommand(SortCommand.COMMAND_WORD + " asc"));
        assertEquals(new SortCommand(true), parser.parseCommand(SortCommand.COMMAND_WORD + " desc"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
