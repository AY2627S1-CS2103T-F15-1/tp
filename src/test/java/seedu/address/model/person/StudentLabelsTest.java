package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.testutil.PersonBuilder;

public class StudentLabelsTest {
    @Test
    public void format_uniqueName_staysUnchanged() {
        Person student = new PersonBuilder().withName("Alex").build();
        AddressBook book = new AddressBook();
        book.addPerson(student);
        assertEquals("Alex", StudentLabels.format(book, student));
    }

    @Test
    public void format_caseInsensitiveCollisionWithArchivedStudent_usesCurrentPhones() {
        Person first = new PersonBuilder().withName("Alex").withPhone("91234567").build();
        Person second = new PersonBuilder().withName("alex").withPhone("92345678").build().withArchived(true);
        AddressBook book = new AddressBook();
        book.addPerson(first);
        book.addPerson(second);
        assertEquals("Alex (91234567)", StudentLabels.format(book, first));
        assertEquals("alex (92345678)", StudentLabels.format(book, second));
        Person changedPhone = new PersonBuilder(first).withPhone("93456789").build();
        book.setPerson(first, changedPhone);
        assertEquals("Alex (93456789)", StudentLabels.format(book, changedPhone));
    }
}
