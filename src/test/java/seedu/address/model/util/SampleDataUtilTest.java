package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_containsAllSamplePersons() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();

        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertEquals(samplePersons.length, sampleAddressBook.getPersonList().size());
        for (Person samplePerson : samplePersons) {
            assertTrue(sampleAddressBook.getPersonList().contains(samplePerson));
        }
    }

    @Test
    public void getSamplePersons_everyPersonHasSubjects() {
        for (Person samplePerson : SampleDataUtil.getSamplePersons()) {
            assertTrue(!samplePerson.getSubjects().isEmpty());
        }
    }

    @Test
    public void getSamplePersons_includesPersonWithoutOptionalFields() {
        boolean hasPersonWithoutOptionalFields = false;
        for (Person samplePerson : SampleDataUtil.getSamplePersons()) {
            hasPersonWithoutOptionalFields |= samplePerson.getEmail().isEmpty() && samplePerson.getVenue().isEmpty();
        }
        assertTrue(hasPersonWithoutOptionalFields);
    }

    @Test
    public void getSubjectSet_sameSubjectInDifferentCase_returnsOneSubject() {
        assertEquals(1, SampleDataUtil.getSubjectSet("Math", "math").size());
        assertTrue(SampleDataUtil.getSubjectSet("Math", "Physics").contains(new Subject("Physics")));
    }
}
