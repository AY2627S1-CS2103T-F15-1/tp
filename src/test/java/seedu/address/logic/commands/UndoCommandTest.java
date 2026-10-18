package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UndoSnapshot;
import seedu.address.testutil.TypicalPersons;

public class UndoCommandTest {
    private final ModelManager model = new ModelManager();

    @Test
    public void execute_noSnapshot_isRejectedWithoutChange() {
        model.addPerson(TypicalPersons.ALICE);
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException rejection = assertThrows(CommandException.class, () -> new UndoCommand().execute(model));
        assertTrue(rejection.getMessage().startsWith("Nothing to undo"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_snapshot_restoresDataAndIsConsumed() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BOB);
        AddressBook original = new AddressBook(model.getAddressBook());
        model.setUndoSnapshot(new UndoSnapshot("student deletion", original));
        model.deletePerson(TypicalPersons.ALICE);
        model.setAddressBook(new AddressBook());

        CommandResult result = new UndoCommand().execute(model);

        assertEquals(original, model.getAddressBook());
        assertTrue(result.getFeedbackToUser().contains("student deletion"));
        assertTrue(model.getUndoSnapshot().isEmpty());
        assertThrows(CommandException.class, () -> new UndoCommand().execute(model));
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void snapshot_isIndependentOfLaterChangesToTheLiveData() {
        model.addPerson(TypicalPersons.ALICE);
        AddressBook original = new AddressBook(model.getAddressBook());
        UndoSnapshot snapshot = new UndoSnapshot("clear", original);
        model.setUndoSnapshot(snapshot);
        model.deletePerson(TypicalPersons.ALICE);
        assertEquals(1, model.getUndoSnapshot().orElseThrow().data().getPersonList().size());
        model.clearUndoSnapshot();
        assertFalse(model.getUndoSnapshot().isPresent());
    }

    @Test
    public void parser_undoTakesNoParameters() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertEquals(new UndoCommand(), parser.parseCommand("undo"));
        assertEquals(new UndoCommand(), parser.parseCommand("  undo  "));
        assertThrows(ParseException.class, () -> parser.parseCommand("undo 1"));
        assertThrows(ParseException.class, () -> parser.parseCommand("undo now"));
    }
}
