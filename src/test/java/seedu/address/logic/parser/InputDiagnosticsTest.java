package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/** Tests the semantic information users need to correct their input. */
public class InputDiagnosticsTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_slashBearingVenueAndNotes_keepsNonemptyValues() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        parser.parseCommand("edit 1 v/blk/123").execute(model);
        assertEquals("blk/123", model.getFilteredPersonList().getFirst().getVenue().orElseThrow().value);
        model.addLesson(new LessonBuilder().withStudent(model.getFilteredPersonList().getFirst())
                .withDate(LocalDate.now().minusDays(1)).build());
        parser.parseCommand("lesson list st/1 all/").execute(model);
        parser.parseCommand("lesson done 1 n/s/p/d/f orbitals").execute(model);
        assertEquals("s/p/d/f orbitals", model.getAddressBook().getLessonList().getFirst().getNotes().orElseThrow());
        parser.parseCommand("lesson done 1 n/Review unknown/ prefix").execute(model);
        assertEquals("Review unknown/ prefix", model.getAddressBook().getLessonList().getFirst()
                .getNotes().orElseThrow());
    }

    @Test
    public void parse_missingRequiredFields_namesEachMissingPrefix() {
        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand("add r/100"));
        for (String prefix : new String[] {"n/", "p/", "l/", "s/"}) {
            assertTrue(error.getMessage().contains(prefix));
        }
        assertTrue(error.getMessage().contains("Missing required"));
    }

    @Test
    public void parse_emptyField_identifiesItsPrefix() {
        for (String command : new String[] {"add n/ p/91234567 l/J1 s/Math r/100", "edit 1 e/",
            "lesson cancel 1 r/"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertTrue(error.getMessage().contains("Empty value"), error.getMessage());
            String prefix = command.startsWith("add") ? "n/" : command.startsWith("edit") ? "e/" : "r/";
            assertTrue(error.getMessage().contains(prefix));
        }
    }

    @Test
    public void execute_invalidIndex_namesRequestedNumberAndVisibleCount() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        CommandException error = assertThrows(CommandException.class, () -> {
            parser.parseCommand("view 99").execute(model);
        });
        assertTrue(error.getMessage().contains("99"));
        assertTrue(error.getMessage().contains("1 item"));
        model.updateFilteredPersonList(person -> false);
        error = assertThrows(CommandException.class, () -> parser.parseCommand("view 99").execute(model));
        assertTrue(error.getMessage().contains("0 item"));
    }

    @Test
    public void parse_controlBearingCommand_rejectsBeforeTrimming() {
        for (String control : new String[] {"\n", "\t", "\u0000", "\u2028"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit 1 v/Home" + control));
        }
    }
}
