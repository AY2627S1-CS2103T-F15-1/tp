package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Level("S3"), getSubjectSet("Math", "Physics"), new Rate("50"),
                new Venue("Blk 30 Geylang Street 29, #06-40")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Level("J1"), getSubjectSet("Chemistry", "Biology"), new Rate("60"),
                new Venue("Blk 30 Lorong 3 Serangoon Gardens, #07-18")),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Level("S4"), getSubjectSet("Math"), new Rate("45"),
                new Venue("Blk 11 Ang Mo Kio Street 74, #11-04")),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Level("P6"), getSubjectSet("Math", "Science"), new Rate("35"),
                new Venue("Blk 436 Serangoon Gardens Street 26, #16-43")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Level("J2"), getSubjectSet("Physics", "Math"), new Rate("70"),
                new Venue("Blk 47 Tampines Street 20, #17-35")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), null,
                new Level("S1"), getSubjectSet("English"), new Rate("40"), null)
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a subject set containing the list of strings given.
     */
    public static Set<Subject> getSubjectSet(String... strings) {
        return Arrays.stream(strings)
                .map(Subject::new)
                .collect(Collectors.toSet());
    }

}
