package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Level;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private static final Level S3 = new Level("S3");
    private static final Level J1 = new Level("J1");
    private static final Level P1 = new Level("P1");
    private static final Subject MATH = new Subject("Math");

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListCommand().execute(null));
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_levelFilter_showsOnlyThatLevel() throws Exception {
        CommandResult result = new ListCommand(S3, null, false, false).execute(model);

        assertEquals(List.of(ALICE), shownStudents());
        assertEquals(String.format(ListCommand.MESSAGE_FILTERED, 1), result.getFeedbackToUser());
    }

    @Test
    public void execute_subjectFilter_showsEveryStudentWhoTakesTheSubject() throws Exception {
        CommandResult result = new ListCommand(null, MATH, false, false).execute(model);

        assertEquals(List.of(ALICE, BENSON, GEORGE), shownStudents());
        assertEquals("Listed 3 matching student(s).", result.getFeedbackToUser());
    }

    @Test
    public void execute_levelAndSubject_areCombinedWithAnd() throws Exception {
        new ListCommand(J1, MATH, false, false).execute(model);

        assertEquals(List.of(BENSON), shownStudents());
    }

    @Test
    public void execute_filterMatchingNobody_showsEmptyListWithoutError() throws Exception {
        CommandResult result = new ListCommand(P1, MATH, false, false).execute(model);

        assertTrue(shownStudents().isEmpty());
        assertEquals("Listed 0 matching student(s).", result.getFeedbackToUser());
    }

    @Test
    public void execute_archiveScopes_selectActiveArchivedOrAllStudents() throws Exception {
        Person archived = model.getAddressBook().getPersonList().get(0);
        model.setPerson(archived, archived.withArchived(true));
        int total = model.getAddressBook().getPersonList().size();

        new ListCommand(null, null, true, false).execute(model);
        assertEquals(1, shownStudents().size());
        assertTrue(shownStudents().get(0).isArchived());

        new ListCommand(null, null, false, true).execute(model);
        assertEquals(total, shownStudents().size());

        CommandResult active = new ListCommand().execute(model);
        assertEquals(total - 1, shownStudents().size());
        assertEquals(ListCommand.MESSAGE_SUCCESS, active.getFeedbackToUser());
    }

    @Test
    public void execute_filtersApplyToTheChosenArchiveScope() throws Exception {
        Person archivedAlice = model.getAddressBook().getPersonList().get(0);
        model.setPerson(archivedAlice, archivedAlice.withArchived(true));

        new ListCommand(S3, null, false, false).execute(model);
        assertTrue(shownStudents().isEmpty());

        new ListCommand(S3, null, true, false).execute(model);
        assertEquals(1, shownStudents().size());
    }

    @Test
    public void equals() {
        ListCommand command = new ListCommand(S3, MATH, true, false);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new ListCommand(S3, MATH, true, false)));
        assertEquals(command.hashCode(), new ListCommand(S3, MATH, true, false).hashCode());
        assertTrue(new ListCommand().equals(new ListCommand(null, null, false, false)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertNotEquals(command, new ListCommand(J1, MATH, true, false));
        assertNotEquals(command, new ListCommand(S3, null, true, false));
        assertNotEquals(command, new ListCommand(S3, MATH, false, false));
        assertNotEquals(command, new ListCommand(S3, MATH, true, true));
    }

    @Test
    public void toStringMethod() {
        String expected = ListCommand.class.getCanonicalName() + "{level=" + S3 + ", subject=" + MATH
                + ", isArchivedOnly=true, isArchivedIncluded=false}";
        assertEquals(expected, new ListCommand(S3, MATH, true, false).toString());
    }

    private List<Person> shownStudents() {
        return List.copyOf(model.getFilteredPersonList());
    }
}
