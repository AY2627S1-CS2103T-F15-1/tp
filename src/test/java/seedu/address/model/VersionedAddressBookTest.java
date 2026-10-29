package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalLessons.CARL_ENGLISH;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.GEORGE;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.task.Task;

/**
 * Tests for the undo functionality VersionedAddressBook builds on top of AddressBook
 */
public class VersionedAddressBookTest {
    private VersionedAddressBook book;
    private final LocalDate date = LocalDate.of(2027, 1, 31);

    @BeforeEach
    public void init() {
        book = new VersionedAddressBook();
    }

    @Test
    public void nonDestructiveCommand_successfulOrFails_noUndo() {
        book.beforeCommand(false);
        book.addPerson(BENSON);
        book.addTask(Task.create("a", date, BENSON));
        book.removePerson(BENSON);
        book.afterCommand(null, false);

        assertNull(book.undoDestructiveAction());

        book.beforeCommand(false);
        book.addPerson(CARL);
        book.addLesson(CARL_ENGLISH);
        book.afterCommand(null, true);

        assertNull(book.undoDestructiveAction());
    }

    @Test
    public void destructiveCommand_undoes() {
        book.addPerson(BENSON);
        book.addTask(Task.create("a", date, BENSON));
        AddressBook before = new AddressBook(book);

        book.beforeCommand(true);
        book.removePerson(BENSON);
        book.afterCommand("delete student", true);

        assertEquals("delete student", book.undoDestructiveAction());
        assertEquals(before, book);
    }

    @Test
    public void destructiveCommand_fails_noResetUndo() {
        book.addPerson(GEORGE);
        Task t = Task.create("a", date, GEORGE);
        book.addTask(t);
        AddressBook before = new AddressBook(book);

        book.beforeCommand(true);
        book.removeTask(t);
        book.afterCommand("delete task", true);

        book.beforeCommand(true);
        assertThrows(RuntimeException.class, () -> book.removeTask(t));
        book.afterCommand("delete task 2", false);

        assertEquals("delete task", book.undoDestructiveAction());
        assertEquals(before, book);
    }
}
