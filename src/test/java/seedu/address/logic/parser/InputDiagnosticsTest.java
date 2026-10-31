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
        for (String command : new String[] {"add n/ p/91234567 l/J1 s/Math r/100", "edit 1 p/",
            "lesson cancel 1 r/"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertTrue(error.getMessage().contains("Empty value"), error.getMessage());
            String prefix = command.startsWith("add") ? "n/" : command.startsWith("edit") ? "p/" : "r/";
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

    @Test
    public void parse_retiredAddressPrefix_namesTheRetiredPrefixInsteadOfMisreportingTheEmail() {
        String command = "add n/josh p/78787878 l/S3 s/Math r/120 e/josh@gmail.com a/2902 Yishun";

        ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));

        assertEquals("Unknown prefix for this command: a/. Use v/ for a student's venue.", error.getMessage());
    }

    @Test
    public void parse_retiredAddressPrefixInEdit_namesTheRetiredPrefix() {
        ParseException error = assertThrows(ParseException.class, () ->
                parser.parseCommand("edit 1 e/josh@gmail.com a/2902 Yishun"));

        assertEquals("Unknown prefix for this command: a/. Use v/ for a student's venue.", error.getMessage());
    }

    @Test
    public void parse_retiredAddressPrefixInNonPrefixSubcommands_namesTheRetiredPrefix() {
        String[] commands = {
            "pay reverse 1 a/Home",
            "PAY REVERSE 1 a/Home",
            "hw delete 1 a/Home",
            "lesson missed 1 a/Home",
            "lesson series cancel 1 confirm a/Home",
            "task done 1 a/Home",
            "task delete 1 confirm a/Home",
        };

        for (String command : commands) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertEquals("Unknown prefix for this command: a/. Use v/ for a student's venue.",
                    error.getMessage(), command);
        }
    }

    @Test
    public void parse_addressLikeTextInsideAValue_isNotMistakenForTheRetiredPrefix() throws Exception {
        parser.parseCommand("add n/Priya s/o Kumar p/91234567 l/S3 s/Math r/50 v/Room c/o Guard");
        parser.parseCommand("task add st/1 desc/Read n/a notes due/tmr");
        parser.parseCommand("find a/Home");
    }

    @Test
    public void parse_everyAddPrefixReplacedByTypo_namesTheUnknownPrefix() {
        String base = " n/josh p/78787878 l/S3 s/Math r/120 e/josh@gmail.com v/2902 Yishun";
        String[] validPrefixes = {"n/", "p/", "l/", "s/", "r/", "e/", "v/"};
        String[] typoPrefixes = {"name/", "phone/", "level/", "subject/", "sub/", "rate/", "email/", "venue/",
            "address/", "a/"};

        for (String validPrefix : validPrefixes) {
            for (String typoPrefix : typoPrefixes) {
                String command = "add" + base.replace(validPrefix, typoPrefix);
                ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
                assertTrue(error.getMessage().startsWith("Unknown prefix for this command: "),
                        command + " -> " + error.getMessage());
            }
        }
    }

    @Test
    public void parse_mistypedPrefixAcrossCommands_namesTheUnknownPrefix() {
        String[] commands = {
            "edit 1 name/Alice",
            "edit 1 sub/Math",
            "lesson add student/1 d/tmr t/10:00",
            "lesson add st/1 date/tmr t/10:00",
            "lesson add st/1 d/tmr time/10:00",
            "lesson repeat st/1 d/tmr t/10:00 count/4 week/2",
            "lesson move 1 date/tmr",
            "lesson cancel 1 reason/Unwell",
            "lesson done 1 notes/Covered",
            "lesson prep 1 notes/Bring",
            "pay student/1 amt/20",
            "pay st/1 amount/20",
            "pay st/1 amt/20 date/tmr",
            "withdraw st/1 amt/20 date/tmr",
            "charge l/1 amount/20",
            "balance student/1",
            "agenda date/tmr",
            "travel buffer/30 minutes/10",
            "travel buffer/30 weeks/",
            "list level/S3",
            "remark 1 text/hello",
            "hw add student/1 desc/Read due/tmr",
            "hw edit 1 description/Read",
            "hw status 1 status/submitted",
            "hw feedback 1 feedback/Good",
            "hw list student/1",
            "task add student/1 desc/Call",
            "task edit 1 description/Call",
            "task list student/1",
            "archive 1 name/Alice",
            "restore student/1",
            "sort name/asc",
        };

        for (String command : commands) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertTrue(error.getMessage().startsWith("Unknown prefix for this command: "),
                    command + " -> " + error.getMessage());
        }
    }

    @Test
    public void parse_realPrefixUsedByAnotherCommand_namesTheUnknownPrefix() {
        String[] commands = {
            "lesson add st/1 d/tmr t/10:00 amt/20",
            "task add amt/1 desc/Call",
            "hw add st/1 desc/Read due/tmr count/2",
            "pay st/1 amt/20 due/tmr",
            "travel buffer/30 week/",
            "charge reverse l/1 amt/20",
        };

        for (String command : commands) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertTrue(error.getMessage().startsWith("Unknown prefix for this command: "),
                    command + " -> " + error.getMessage());
        }
    }

    @Test
    public void parse_freeTextAndOptionalFields_doNotProducePrefixErrors() throws Exception {
        String[] commands = {
            "add n/Priya s/o Kumar p/91234567 l/S3 s/Math r/50",
            "add n/Alice p/91234567 l/S3 s/Math r/50 v/Room c/o Guard",
            "add n/Alice p/91234567 l/S3 s/Math r/50 v/n/a",
            "add n/Alice p/91234567 l/S3 s/Math r/50 v/Room w/o chairs",
            "add n/Alice p/91234567 l/S3 s/Math r/50 v/Room d/o Staff",
            "add n/Alice p/91234567 l/S3 s/Math r/50 v/Blk/123",
            "edit 1 v/Blk/123",
            "edit 1 n/Priya s/o Kumar",
            "task add st/1 desc/Read n/a notes due/tmr",
            "task add st/1 desc/Read p/3 notes due/tmr",
            "hw add st/1 desc/Read n/a notes and s/o p/3 due/tmr",
            "hw edit 1 desc/Read p/3 today",
            "hw feedback 1 f/Quote add n/Alice a/Home",
            "lesson done 1 n/Quote add st/1 a/Home",
            "remark 1 r/Quote add n/Alice a/Home",
            "lesson cancel 1 r/Quote add n/Alice a/Home",
            "hw feedback 1 f/Check st/1 and desc/x",
            "lesson done 1 n/s/p/d/f orbitals",
            "lesson done 1 n/Review unknown/ prefix",
            "remark 1 r/See c/o the office",
        };

        for (String command : commands) {
            parser.parseCommand(command);
        }
    }

    @Test
    public void parse_mistypedOptionalFieldInsideDescription_namesTheUnknownPrefix() {
        String[] commands = {
            "hw add st/1 desc/Read name/tmr",
            "hw add st/1 desc/Read a/Home due/tmr",
            "hw edit 1 desc/Read name/tmr",
            "hw edit 1 desc/Read a/Home",
            "task add st/1 desc/Call name/tmr",
            "task add st/1 desc/Call a/Home",
            "task edit 1 desc/Call name/tmr",
            "task edit 1 desc/Call a/Home",
        };

        for (String command : commands) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(command));
            assertTrue(error.getMessage().startsWith("Unknown prefix for this command: "),
                    command + " -> " + error.getMessage());
        }
    }

}
