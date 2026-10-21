package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

public class BalanceCommandTest {
    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BalanceCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        BalanceCommand command = new BalanceCommand(INDEX_FIRST_PERSON);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals() {
        BalanceCommand command = new BalanceCommand(INDEX_FIRST_PERSON);
        BalanceCommand sameIndex = new BalanceCommand(INDEX_FIRST_PERSON);
        BalanceCommand differentIndex = new BalanceCommand(INDEX_SECOND_PERSON);

        // Same instance and equivalent commands are equal in both directions.
        assertTrue(command.equals(command));
        assertTrue(command.equals(sameIndex));
        assertTrue(sameIndex.equals(command));

        // A different target, null, or another command type cannot represent the same request.
        assertFalse(command.equals(differentIndex));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new DuesCommand()));
    }
}
