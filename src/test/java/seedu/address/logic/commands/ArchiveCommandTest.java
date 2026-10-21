package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class ArchiveCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ArchiveCommand(null, true));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ArchiveCommand(INDEX_FIRST_PERSON, true).execute(null));
    }

    @Test
    public void execute_activeStudent_archivesAndLeavesActiveList() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        int activeBefore = model.getFilteredPersonList().size();
        expectedModel.setPerson(student, student.withArchived(true));

        assertCommandSuccess(new ArchiveCommand(INDEX_FIRST_PERSON, true), model,
                String.format(ArchiveCommand.MESSAGE_ARCHIVED, student.getName()), expectedModel);

        assertTrue(model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getId().equals(student.getId()) && person.isArchived()));
        assertEquals(activeBefore - 1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_archivedStudent_restoresAndKeepsIdentity() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(student, student.withArchived(true));
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        Person archived = model.getFilteredPersonList().stream()
                .filter(person -> person.getId().equals(student.getId())).findFirst().orElseThrow();
        Index archivedIndex = Index.fromZeroBased(model.getFilteredPersonList().indexOf(archived));

        CommandResult result = assertDoesNotThrowResult(new ArchiveCommand(archivedIndex, false));

        assertEquals(String.format(ArchiveCommand.MESSAGE_RESTORED, student.getName()), result.getFeedbackToUser());
        Person restored = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getId().equals(student.getId())).findFirst().orElseThrow();
        assertFalse(restored.isArchived());
    }

    @Test
    public void execute_archiveAnArchivedStudent_throwsCommandException() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(student, student.withArchived(true));
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        Index archivedIndex = Index.fromZeroBased(model.getFilteredPersonList().stream()
                .filter(Person::isArchived).findFirst().map(model.getFilteredPersonList()::indexOf).orElseThrow());

        assertCommandFailure(new ArchiveCommand(archivedIndex, true), model, ArchiveCommand.MESSAGE_ALREADY_ARCHIVED);
    }

    @Test
    public void execute_restoreAnActiveStudent_throwsCommandException() {
        assertCommandFailure(new ArchiveCommand(INDEX_FIRST_PERSON, false), model,
                ArchiveCommand.MESSAGE_ALREADY_ACTIVE);
    }

    @Test
    public void execute_indexOutOfRange_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ArchiveCommand(outOfBounds, true), model,
                Messages.invalidPersonIndex(outOfBounds, model.getFilteredPersonList().size()));
        assertCommandFailure(new ArchiveCommand(outOfBounds, false), model,
                Messages.invalidPersonIndex(outOfBounds, model.getFilteredPersonList().size()));
    }

    @Test
    public void messages_matchSpecification() {
        assertEquals("This student is already active. Use student list archived/ before restoring.",
                ArchiveCommand.MESSAGE_ALREADY_ACTIVE);
        assertEquals("Archived: Chen Hao. Lessons and history are retained. The student list has refreshed.",
                String.format(ArchiveCommand.MESSAGE_ARCHIVED, "Chen Hao"));
        assertEquals("Restored: Amy Tan. Lessons and history are retained. The student list has refreshed.",
                String.format(ArchiveCommand.MESSAGE_RESTORED, "Amy Tan"));
    }

    @Test
    public void equals() {
        ArchiveCommand archiveFirst = new ArchiveCommand(INDEX_FIRST_PERSON, true);

        assertTrue(archiveFirst.equals(archiveFirst));
        assertTrue(archiveFirst.equals(new ArchiveCommand(INDEX_FIRST_PERSON, true)));
        assertEquals(archiveFirst.hashCode(), new ArchiveCommand(INDEX_FIRST_PERSON, true).hashCode());
        assertFalse(archiveFirst.equals(null));
        assertFalse(archiveFirst.equals(new ClearCommand()));
        assertNotEquals(archiveFirst, new ArchiveCommand(INDEX_SECOND_PERSON, true));
        assertNotEquals(archiveFirst, new ArchiveCommand(INDEX_FIRST_PERSON, false));
    }

    @Test
    public void toStringMethod() {
        ArchiveCommand command = new ArchiveCommand(INDEX_FIRST_PERSON, false);
        String expected = ArchiveCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", shouldArchive=false}";
        assertEquals(expected, command.toString());
    }

    private CommandResult assertDoesNotThrowResult(ArchiveCommand command) {
        try {
            return command.execute(model);
        } catch (Exception e) {
            throw new AssertionError("The command should have succeeded", e);
        }
    }
}
