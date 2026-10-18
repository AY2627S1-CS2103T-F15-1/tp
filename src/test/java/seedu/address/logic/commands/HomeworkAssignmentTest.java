package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.HomeworkCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.TypicalPersons;

public class HomeworkAssignmentTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);
    private final HomeworkCommandParser parser = new HomeworkCommandParser(
            Clock.fixed(Instant.parse("2030-01-10T00:00:00Z"), ZoneOffset.UTC));

    private ModelManager model() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        return new ModelManager(book, new UserPrefs());
    }

    @Test
    public void add_validBoundariesAndDuplicate_checkBeforeMutation() throws Exception {
        ModelManager model = model();
        String message = parser.parse("add st/1 desc/  Algebra  practice  due/today").execute(model)
                .getFeedbackToUser();
        assertTrue(message.contains("no upcoming lessons"));
        Homework item = model.getAddressBook().getHomeworkList().getFirst();
        assertEquals("Algebra practice", item.getDescription());
        assertEquals(TODAY, item.getDueDate());
        assertEquals(HomeworkStatus.ASSIGNED, item.getStatus());
        assertThrows(CommandException.class, () -> parser.parse(
                "add st/1 desc/Algebra practice due/2030-01-10").execute(model));
        parser.parse("add st/1 desc/Boundary due/2031-01-10").execute(model);
        for (String date : new String[] {"2030-01-09", "2031-01-11"}) {
            assertThrows(CommandException.class, () -> parser.parse("add st/1 desc/Invalid due/" + date)
                    .execute(model));
        }
        assertEquals(2, model.getAddressBook().getHomeworkList().size());
    }

    @Test
    public void edit_historicalDueAndFeedback_preservesIdentityAndSelection() throws Exception {
        ModelManager model = model();
        Homework historical = Homework.assign(TypicalPersons.ALICE.getId(), "Old", TODAY.minusDays(3))
                .withStatus(HomeworkStatus.REVIEWED).withFeedback("Keep me");
        model.addHomework(historical);
        parser.parse("list").execute(model);
        parser.parse("edit 1 desc/New due/2030-01-07").execute(model);
        Homework edited = model.getAddressBook().getHomeworkList().getFirst();
        assertEquals(historical.getId(), edited.getId());
        assertEquals("Keep me", edited.getFeedback().orElseThrow());
        assertEquals(HomeworkStatus.REVIEWED, edited.getStatus());
        parser.parse("add st/1 desc/Other due/today").execute(model);
        parser.parse("edit 1 due/tmr").execute(model);
        assertEquals(TODAY.plusDays(1), model.getAddressBook().getHomeworkList().getFirst().getDueDate());
        assertThrows(CommandException.class, () -> parser.parse("edit 1 due/2030-01-08").execute(model));
        assertThrows(CommandException.class, () -> parser.parse("edit 1 desc/Other due/today").execute(model));
    }

    @Test
    public void parse_invalidValuesAndEmptyRecognizedPrefixes_rejects() {
        for (String args : new String[] {"add st/1 desc/ due/today", "add st/ desc/A due/today",
            "add st/1 desc/A due/", "add st/1 desc/A due/today due/tmr", "edit 1", "edit 1 desc/",
            "add st/1 desc/" + "x".repeat(201) + " due/today", "add st/1 desc/A\nB due/today",
            "add st/1 desc/\nA due/today", "feedback 1 f/\tA", "list\n",
            "list overdue/yes", "list overdue/ outstanding/", "list st/", "list st/1 st/1"}) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
