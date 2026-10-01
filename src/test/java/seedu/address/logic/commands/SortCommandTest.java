package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for SortCommand.
 */
public class SortCommandTest {

    // The mix of upper and lower case names tells a case-insensitive sort apart from a plain string sort.
    private static final Person AARON = new PersonBuilder().withName("Aaron Tan").build();
    private static final Person BELLA = new PersonBuilder().withName("bella ng").build();
    private static final Person CARL = new PersonBuilder().withName("Carl Kurz").build();
    private static final Person ZACK = new PersonBuilder().withName("zack lee").build();

    @Test
    public void execute_unsortedList_sortsAscending() {
        Model model = createModel(CARL, ZACK, AARON, BELLA);
        Model expectedModel = createModel(AARON, BELLA, CARL, ZACK);

        assertCommandSuccess(new SortCommand(false), model, SortCommand.MESSAGE_SUCCESS_ASCENDING, expectedModel);
    }

    @Test
    public void execute_unsortedList_sortsDescending() {
        Model model = createModel(CARL, ZACK, AARON, BELLA);
        Model expectedModel = createModel(ZACK, CARL, BELLA, AARON);

        assertCommandSuccess(new SortCommand(true), model, SortCommand.MESSAGE_SUCCESS_DESCENDING, expectedModel);
    }

    @Test
    public void execute_alreadySortedList_listUnchanged() {
        Model model = createModel(AARON, BELLA, CARL, ZACK);
        Model expectedModel = createModel(AARON, BELLA, CARL, ZACK);

        assertCommandSuccess(new SortCommand(false), model, SortCommand.MESSAGE_SUCCESS_ASCENDING, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = createModel();
        Model expectedModel = createModel();

        assertCommandSuccess(new SortCommand(false), model, SortCommand.MESSAGE_SUCCESS_ASCENDING, expectedModel);
    }

    @Test
    public void execute_filteredList_showsAllPersonsSorted() {
        Model model = createModel(CARL, ZACK, AARON, BELLA);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = createModel(AARON, BELLA, CARL, ZACK);

        assertCommandSuccess(new SortCommand(false), model, SortCommand.MESSAGE_SUCCESS_ASCENDING, expectedModel);
        assertEquals(4, model.getFilteredPersonList().size());
    }

    @Test
    public void equals() {
        SortCommand ascendingCommand = new SortCommand(false);
        SortCommand descendingCommand = new SortCommand(true);

        // same object -> returns true
        assertTrue(ascendingCommand.equals(ascendingCommand));

        // same values -> returns true
        assertTrue(ascendingCommand.equals(new SortCommand(false)));

        // different order -> returns false
        assertFalse(ascendingCommand.equals(descendingCommand));

        // null -> returns false
        assertFalse(ascendingCommand.equals(null));

        // different types -> returns false
        assertFalse(ascendingCommand.equals(new ClearCommand()));
    }

    @Test
    public void toStringMethod() {
        SortCommand sortCommand = new SortCommand(true);
        String expected = SortCommand.class.getCanonicalName() + "{isDescending=true}";
        assertEquals(expected, sortCommand.toString());
    }

    /**
     * Returns a model whose address book holds {@code persons} in the given order.
     */
    private static Model createModel(Person... persons) {
        AddressBookBuilder builder = new AddressBookBuilder(new AddressBook());
        for (Person person : persons) {
            builder.withPerson(person);
        }
        return new ModelManager(builder.build(), new UserPrefs());
    }
}
