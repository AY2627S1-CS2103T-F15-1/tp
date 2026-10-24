package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;

/** Checks that the commands of the student records feature keep equals and hashCode consistent. */
public class StudentCommandContractTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    @Test
    public void equalCommands_haveEqualHashCodes() {
        assertConsistent(new AddCommand(ALICE), new AddCommand(ALICE));
        assertConsistent(new DeleteCommand(INDEX_FIRST_PERSON), new DeleteCommand(INDEX_FIRST_PERSON));
        assertConsistent(new DeleteCommand(INDEX_FIRST_PERSON, true), new DeleteCommand(INDEX_FIRST_PERSON, true));
        assertConsistent(
                new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withName("Amy").build()),
                new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withName("Amy").build()));
        assertConsistent(new FindCommand(new NameContainsKeywordsPredicate(List.of("alice", "bob"))),
                new FindCommand(new NameContainsKeywordsPredicate(List.of("alice", "bob"))));
        assertConsistent(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes maths")),
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes maths")));
        assertConsistent(new SortCommand(true), new SortCommand(true));
        assertConsistent(new ViewCommand(INDEX_FIRST_PERSON, TODAY), new ViewCommand(INDEX_FIRST_PERSON, TODAY));
    }

    @Test
    public void differentCommands_areNotEqual() {
        assertNotEquals(new AddCommand(ALICE), new AddCommand(BOB));
        assertNotEquals(new DeleteCommand(INDEX_FIRST_PERSON), new DeleteCommand(INDEX_SECOND_PERSON));
        assertNotEquals(new DeleteCommand(INDEX_FIRST_PERSON, true), new DeleteCommand(INDEX_FIRST_PERSON, false));
        assertNotEquals(new SortCommand(true), new SortCommand(false));
        assertNotEquals(new ViewCommand(INDEX_FIRST_PERSON, TODAY),
                new ViewCommand(INDEX_FIRST_PERSON, TODAY.plusDays(1)));
    }

    private static void assertConsistent(Object first, Object second) {
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
