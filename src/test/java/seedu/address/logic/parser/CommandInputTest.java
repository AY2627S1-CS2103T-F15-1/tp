package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandInputTest {

    @Test
    public void split_wordOnly_hasEmptyArguments() {
        CommandInput input = CommandInput.split("  list  ").get();
        assertEquals("list", input.getWord());
        assertEquals("", input.getArguments());
    }

    @Test
    public void split_wordWithArguments_keepsSeparatingWhitespaceInArguments() {
        CommandInput input = CommandInput.split("add n/Amy p/91234567").get();
        assertEquals("add", input.getWord());
        assertEquals(" n/Amy p/91234567", input.getArguments());
    }

    @Test
    public void split_blank_returnsEmpty() {
        assertEquals(Optional.empty(), CommandInput.split(""));
        assertTrue(CommandInput.split("   ").isEmpty());
    }
}
