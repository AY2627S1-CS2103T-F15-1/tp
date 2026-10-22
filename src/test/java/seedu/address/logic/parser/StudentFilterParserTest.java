package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class StudentFilterParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_filtersAndFlags_rejectsEmptyDuplicateOrConflictingArguments() {
        for (String arguments : new String[]{"l/", "s/", "archived/yes", "all/yes", "archived/ all/",
            "all/ all/", "l/P1 l/P2", "s/Math s/Science", "unknown", "unsupported/"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("student list " + arguments));
        }
    }

    @Test
    public void parse_validCombinedFilters_acceptsEachArchiveScope() throws Exception {
        assertTrue(parser.parseCommand("student list l/P1 s/Math") instanceof ListCommand);
        assertTrue(parser.parseCommand("student list l/P1 s/Math archived/") instanceof ListCommand);
        assertTrue(parser.parseCommand("student list l/P1 s/Math all/") instanceof ListCommand);
    }

    @Test
    public void parse_studentNamespace_rejectsUnrelatedCommands() {
        assertThrows(ParseException.class, () -> parser.parseCommand("student clear confirm"));
        assertThrows(ParseException.class, () -> parser.parseCommand("student student list"));
        assertThrows(ParseException.class, () -> parser.parseCommand("student"));
    }
}
