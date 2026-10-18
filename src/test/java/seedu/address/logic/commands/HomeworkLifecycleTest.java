package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.HomeworkCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.TypicalPersons;

public class HomeworkLifecycleTest {
    private final HomeworkCommandParser parser = new HomeworkCommandParser();

    private ModelManager model() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        book.addHomework(Homework.assign(TypicalPersons.ALICE.getId(), "Algebra", LocalDate.now().minusDays(1)));
        book.addHomework(Homework.assign(TypicalPersons.ALICE.getId(), "Geometry", LocalDate.now()));
        ModelManager model = new ModelManager(book, new UserPrefs());
        parser.parse("list").execute(model);
        return model;
    }

    @Test
    public void statusAndFeedback_corrections_preserveHistoricalFeedback() throws Exception {
        ModelManager model = model();
        assertThrows(CommandException.class, () -> parser.parse("feedback 1 f/Too early").execute(model));
        parser.parse("status 1 s/SuBmItTeD").execute(model);
        parser.parse("feedback 1 f/Check signs").execute(model);
        parser.parse("status 1 s/reviewed").execute(model);
        parser.parse("feedback 1 f/Updated feedback").execute(model);
        assertThrows(CommandException.class, () -> parser.parse("status 1 s/reviewed").execute(model));
        String warning = parser.parse("status 1 s/assigned").execute(model).getFeedbackToUser();
        assertTrue(warning.contains("moved back from reviewed to assigned"));
        Homework corrected = model.getAddressBook().getHomeworkList().getFirst();
        assertEquals("Updated feedback", corrected.getFeedback().orElseThrow());
        assertTrue(corrected.isOverdue(LocalDate.now()));
        assertThrows(CommandException.class, () -> parser.parse("feedback 1 f/Still too early").execute(model));
        assertEquals("Updated feedback", corrected.getFeedback().orElseThrow());
    }

    @Test
    public void delete_withFeedback_requiresConfirmAndDoesNotRetargetSelection() throws Exception {
        ModelManager model = model();
        parser.parse("status 1 s/submitted").execute(model);
        parser.parse("feedback 1 f/Keep history").execute(model);
        assertTrue(parser.parse("delete 1").execute(model).getFeedbackToUser().contains("hw delete 1 confirm"));
        assertEquals(2, model.getAddressBook().getHomeworkList().size());
        parser.parse("delete 1 confirm").execute(model);
        assertThrows(CommandException.class, () -> parser.parse("status 1 s/reviewed").execute(model));
        parser.parse("status 2 s/submitted").execute(model);
        assertEquals("Geometry", model.getAddressBook().getHomeworkList().getFirst().getDescription());
        assertEquals(HomeworkStatus.SUBMITTED, model.getAddressBook().getHomeworkList().getFirst().getStatus());
    }

    @Test
    public void studentDelete_andClear_warnAndCascadeHomework() throws Exception {
        ModelManager model = model();
        String prompt = new DeleteCommand(Index.fromOneBased(1)).execute(model).getFeedbackToUser();
        assertTrue(prompt.contains("2 homework"));
        assertTrue(prompt.contains("1 upcoming homework"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(new ClearCommand().execute(model).getFeedbackToUser().contains("homework"));
        assertEquals(2, model.getAddressBook().getHomeworkList().size());
        new DeleteCommand(Index.fromOneBased(1), true).execute(model);
        assertTrue(model.getAddressBook().getHomeworkList().isEmpty());
        assertThrows(CommandException.class, () -> parser.parse("delete 2 confirm").execute(model));
        ModelManager secondModel = model();
        new ClearCommand(true).execute(secondModel);
        assertTrue(secondModel.getAddressBook().getHomeworkList().isEmpty());
    }

    @Test
    public void parse_statusFeedbackAndDelete_invalidInputsReject() {
        for (String args : new String[] {"status 1 s/overdue", "status 1 s/", "status 0 s/reviewed",
            "feedback 1 f/", "feedback 1 f/" + "x".repeat(1001), "feedback 1 f/A\tB",
            "delete 1 extra", "delete 1 confirm extra", "status 1 s/assigned s/reviewed"}) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }
}
