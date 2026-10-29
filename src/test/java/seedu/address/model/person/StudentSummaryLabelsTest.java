package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.testutil.PersonBuilder;

public class StudentSummaryLabelsTest {
    @Test
    public void format_uniqueName_staysUnchanged() {
        Person student = new PersonBuilder().withName("Alex").build();
        AddressBook book = new AddressBook();
        book.addPerson(student);
        assertEquals("Alex", new StudentSummaryLabels(book).format(student.getId()));
    }

    @Test
    public void format_caseInsensitiveCollisionWithArchivedStudent_masksPhonesAndCombinesArchiveMarker() {
        Person first = new PersonBuilder().withName("Alex").withPhone("91234567").build();
        Person second = new PersonBuilder().withName("alex").withPhone("92345678").build().withArchived(true);
        AddressBook book = new AddressBook();
        book.addPerson(first);
        book.addPerson(second);
        StudentSummaryLabels labels = new StudentSummaryLabels(book);
        assertEquals("Alex (phone ending 4567)", labels.format(first.getId()));
        assertEquals("alex (phone ending 5678, archived)", labels.format(second.getId()));
        assertFalse(labels.format(first.getId()).contains(first.getPhone().value));
        Person changedPhone = new Person(first.getId(), false, first.getName(), new Phone("93456789"),
                first.getEmail().orElse(null), first.getLevel(), first.getSubjects(), first.getRate(),
                first.getVenue().orElse(null), first.getRemark());
        book.setPerson(first, changedPhone);
        assertEquals("Alex (phone ending 6789)", new StudentSummaryLabels(book).format(first.getId()));
    }

    @Test
    public void format_sameNameAndPhoneSuffix_usesReferencesInsteadOfRevealingMoreDigits() {
        Person first = withId(new PersonBuilder().withName("Alex").withPhone("91234567").build(),
                "11111111-1111-4111-8111-111111111111");
        Person second = withId(new PersonBuilder().withName("Alex").withPhone("92344567").build(),
                "22222222-2222-4222-8222-222222222222");
        AddressBook book = new AddressBook();
        book.addPerson(first);
        book.addPerson(second);
        StudentSummaryLabels labels = new StudentSummaryLabels(book);
        assertEquals("Alex (phone ending 4567, ref 11111111)", labels.format(first.getId()));
        assertEquals("Alex (phone ending 4567, ref 22222222)", labels.format(second.getId()));
    }

    @Test
    public void format_collidingReferencePrefixes_usesCompleteReferences() {
        Person first = withId(new PersonBuilder().withName("Alex").withPhone("91234567").build(),
                "aaaaaaaa-1111-4111-8111-111111111111");
        Person second = withId(new PersonBuilder().withName("Alex").withPhone("92344567").build(),
                "aaaaaaaa-2222-4222-8222-222222222222");
        AddressBook book = new AddressBook();
        book.addPerson(first);
        book.addPerson(second);
        StudentSummaryLabels labels = new StudentSummaryLabels(book);
        assertEquals("Alex (phone ending 4567, ref " + first.getId() + ")", labels.format(first.getId()));
        assertEquals("Alex (phone ending 4567, ref " + second.getId() + ")", labels.format(second.getId()));
    }

    private static Person withId(Person student, String id) {
        return new Person(UUID.fromString(id), student.isArchived(), student.getName(), student.getPhone(),
                student.getEmail().orElse(null), student.getLevel(), student.getSubjects(), student.getRate(),
                student.getVenue().orElse(null), student.getRemark());
    }
}
