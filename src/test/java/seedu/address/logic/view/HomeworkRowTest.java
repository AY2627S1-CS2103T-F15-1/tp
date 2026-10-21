package seedu.address.logic.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.PersonBuilder;

public class HomeworkRowTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    private final AddressBook book = new AddressBook();

    @Test
    public void listOf_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> HomeworkRow.listOf(null, List.of(), TODAY));
        assertThrows(NullPointerException.class, () -> HomeworkRow.listOf(book, null, TODAY));
        assertThrows(NullPointerException.class, () -> HomeworkRow.listOf(book, List.of(), null));
    }

    @Test
    public void listOf_noHomework_hasNoRows() {
        assertEquals(List.of(), HomeworkRow.listOf(book, List.of(), TODAY));
    }

    @Test
    public void listOf_numbersTheRowsFromOneAndDescribesEachAsItIsNow() {
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        Homework late = new HomeworkBuilder(ALICE).withDescription("Late essay")
                .withDueDate(TODAY.minusDays(2)).build();
        Homework fresh = new HomeworkBuilder(BENSON).withDescription("Fractions")
                .withDueDate(TODAY.plusDays(3)).withStatus(HomeworkStatus.SUBMITTED).withFeedback("Neat").build();
        book.addHomework(late);
        book.addHomework(fresh);

        List<HomeworkRow> rows = HomeworkRow.listOf(book, List.of(late, fresh), TODAY);

        assertEquals(new HomeworkRow(1, "Alice Pauline", "Late essay", TODAY.minusDays(2), "OVERDUE", true,
                Optional.empty(), false), rows.get(0));
        assertEquals(new HomeworkRow(2, "Benson Meier", "Fractions", TODAY.plusDays(3), "submitted", false,
                Optional.of("Neat"), false), rows.get(1));
    }

    @Test
    public void listOf_readsTheStatusFromTheDataAndNotFromTheListedCopy() {
        book.addPerson(ALICE);
        Homework listed = new HomeworkBuilder(ALICE).withDueDate(TODAY.plusDays(1)).build();
        book.addHomework(listed);
        book.setHomework(listed, listed.withStatus(HomeworkStatus.SUBMITTED));

        HomeworkRow row = HomeworkRow.listOf(book, List.of(listed), TODAY).get(0);

        assertEquals("submitted", row.status());
    }

    @Test
    public void listOf_archivedStudent_isNamedAsArchived() {
        Person archived = new PersonBuilder(ALICE).withArchived(true).build();
        book.addPerson(archived);
        Homework item = new HomeworkBuilder(archived).build();
        book.addHomework(item);

        assertEquals("Alice Pauline (archived)", HomeworkRow.listOf(book, List.of(item), TODAY).get(0).student());
    }

    @Test
    public void listOf_deletedHomework_staysInPlaceMarkedAsDeleted() {
        book.addPerson(ALICE);
        Homework kept = new HomeworkBuilder(ALICE).withDescription("Kept").build();
        Homework gone = new HomeworkBuilder(ALICE).withDescription("Gone").build();
        book.addHomework(kept);
        book.addHomework(gone);
        book.removeHomework(gone);

        List<HomeworkRow> rows = HomeworkRow.listOf(book, List.of(gone, kept), TODAY);

        assertEquals(2, rows.size());
        assertTrue(rows.get(0).isDeleted());
        assertEquals(1, rows.get(0).number());
        assertEquals("Gone", rows.get(0).description());
        assertEquals(HomeworkRow.STATUS_DELETED, rows.get(0).status());
        assertFalse(rows.get(0).isOverdue());
        assertFalse(rows.get(1).isDeleted());
        assertEquals(2, rows.get(1).number());
    }

    @Test
    public void constructor_nullPart_throwsNullPointerException() {
        LocalDate due = TODAY;
        assertThrows(NullPointerException.class, () -> new HomeworkRow(1, null, "d", due, "s", false,
                Optional.empty(), false));
        assertThrows(NullPointerException.class, () -> new HomeworkRow(1, "n", null, due, "s", false,
                Optional.empty(), false));
        assertThrows(NullPointerException.class, () -> new HomeworkRow(1, "n", "d", null, "s", false,
                Optional.empty(), false));
        assertThrows(NullPointerException.class, () -> new HomeworkRow(1, "n", "d", due, null, false,
                Optional.empty(), false));
        assertThrows(NullPointerException.class, () -> new HomeworkRow(1, "n", "d", due, "s", false, null, false));
    }
}
