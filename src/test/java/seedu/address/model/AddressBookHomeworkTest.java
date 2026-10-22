package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

public class AddressBookHomeworkTest {

    private static final LocalDate DUE = LocalDate.of(2026, 10, 20);

    private AddressBook book;

    @BeforeEach
    public void setUp() {
        book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
    }

    private Homework forAlice(String description, LocalDate dueDate) {
        return Homework.assign(ALICE.getId(), description, dueDate);
    }

    @Test
    public void addHomework_sameDescriptionIgnoringCase_isRejectedWithTheExistingText() {
        book.addHomework(forAlice("Algebra Q1-10", DUE));

        Executable duplicate = () -> book.addHomework(forAlice("ALGEBRA q1-10", DUE));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, duplicate);

        assertEquals("This student already has homework 'Algebra Q1-10' due 2026-10-20.", error.getMessage());
        assertEquals(1, book.getHomeworkList().size());
    }

    @Test
    public void addHomework_duplicateOfReviewedHomework_isRejected() {
        book.addHomework(forAlice("Algebra", DUE).withStatus(HomeworkStatus.REVIEWED));

        assertThrows(IllegalArgumentException.class, () -> book.addHomework(forAlice("algebra", DUE)));
    }

    @Test
    public void addHomework_differentDateOrStudent_isAccepted() {
        book.addHomework(forAlice("Algebra", DUE));

        book.addHomework(forAlice("Algebra", DUE.plusDays(1)));
        book.addHomework(Homework.assign(BENSON.getId(), "Algebra", DUE));

        assertEquals(3, book.getHomeworkList().size());
    }

    @Test
    public void addHomework_unknownStudentOrRepeatedIdentity_isRejected() {
        Executable unknownStudent = () -> book.addHomework(Homework.assign(UUID.randomUUID(), "Algebra", DUE));
        assertEquals("Homework student does not exist.",
                assertThrows(IllegalArgumentException.class, unknownStudent).getMessage());
        Homework first = forAlice("Algebra", DUE);
        book.addHomework(first);

        Executable repeatedIdentity = () -> book.addHomework(first.edit("Different", DUE.plusDays(2)));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, repeatedIdentity);

        assertEquals("Homework identity already exists.", error.getMessage());
    }

    @Test
    public void setHomework_editThatCreatesADuplicateIgnoringCase_isRejected() {
        Homework first = forAlice("Algebra", DUE);
        Homework second = forAlice("Trigonometry", DUE);
        book.addHomework(first);
        book.addHomework(second);

        assertThrows(IllegalArgumentException.class, () -> book.setHomework(second, second.edit("ALGEBRA", DUE)));
        book.setHomework(second, second.edit("Trigonometry worksheet", DUE));

        assertTrue(book.getHomeworkList().stream().anyMatch(item -> item.getDescription().equals(
                "Trigonometry worksheet")));
    }

    @Test
    public void setHomework_changeOfStudentOrIdentity_isRejected() {
        Homework first = forAlice("Algebra", DUE);
        book.addHomework(first);

        assertThrows(IllegalArgumentException.class, () -> book.setHomework(first,
                Homework.assign(ALICE.getId(), "Algebra", DUE)));
        assertThrows(IllegalArgumentException.class, () -> book.setHomework(first,
                new Homework(first.getId(), BENSON.getId(), "Algebra", DUE, HomeworkStatus.ASSIGNED, null)));
    }

    @Test
    public void removeHomework_unknownItem_isRejectedAndRemovingAStudentRemovesTheirHomework() {
        Homework first = forAlice("Algebra", DUE);
        book.addHomework(first);
        book.removeHomework(first);

        assertThrows(IllegalArgumentException.class, () -> book.removeHomework(first));

        book.addHomework(forAlice("Trigonometry", DUE));
        book.removePerson(ALICE);
        assertTrue(book.getHomeworkList().isEmpty());
    }
}
