package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.TypicalPersons;

public class HomeworkQueryTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    @Test
    public void list_historyAndOverdue_sortAndRetainShownSelection() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE.withArchived(true));
        Homework reviewed = Homework.assign(TypicalPersons.ALICE.getId(), "Reviewed", TODAY.minusDays(3))
                .withStatus(HomeworkStatus.REVIEWED).withFeedback("Remember signs");
        Homework overdue = Homework.assign(TypicalPersons.ALICE.getId(), "Late", TODAY.minusDays(1));
        book.addHomework(reviewed);
        book.addHomework(overdue);
        ModelManager model = new ModelManager(book, new UserPrefs());
        CommandResult result = new HomeworkListCommand(null, false, false, TODAY).execute(model);
        assertEquals(List.of(overdue, reviewed), result.getDisplayedHomework().orElseThrow());
        assertTrue(result.getFeedbackToUser().contains("OVERDUE"));
        assertTrue(result.getFeedbackToUser().contains("Remember signs"));
        assertTrue(result.getFeedbackToUser().contains("archived"));
        model.addHomework(Homework.assign(TypicalPersons.ALICE.getId(), "Earlier", TODAY.minusDays(2)));
        assertEquals(List.of(overdue, reviewed), model.getDisplayedHomework());
        assertEquals(List.of(overdue), new HomeworkListCommand(null, true, false, TODAY).execute(model)
                .getDisplayedHomework().orElseThrow().stream().filter(item -> item.equals(overdue)).toList());
        assertEquals(2, model.getDisplayedHomework().size());
    }

    @Test
    public void overviewAndDashboard_showOutstandingAndOverdue_withoutChangingSelection() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        Homework late = Homework.assign(TypicalPersons.ALICE.getId(), "Algebra", TODAY.minusDays(1));
        book.addHomework(late);
        ModelManager model = new ModelManager(book, new UserPrefs());
        assertTrue(new TodayCommand(TODAY).execute(model).getFeedbackToUser().contains("OVERDUE HOMEWORK (1)"));
        String overview = new ViewCommand(Index.fromOneBased(1), TODAY).execute(model).getFeedbackToUser();
        assertTrue(overview.contains("OUTSTANDING HOMEWORK (1)"));
        assertTrue(overview.contains("Algebra"));
        assertTrue(model.getDisplayedHomework().isEmpty());
        model.setHomework(late, late.withStatus(HomeworkStatus.SUBMITTED));
        assertTrue(new TodayCommand(TODAY).execute(model).getFeedbackToUser().contains("No overdue homework."));
        model.setHomework(model.getAddressBook().getHomeworkList().getFirst(),
                late.withStatus(HomeworkStatus.REVIEWED));
        assertTrue(new ViewCommand(Index.fromOneBased(1), TODAY).execute(model).getFeedbackToUser()
                .contains("No outstanding homework."));
    }
}
