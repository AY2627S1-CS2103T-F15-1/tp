package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Duration;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Student(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Level("S3"), getSubjectSet("Math", "Physics"), new Rate("50"),
                new Venue("Blk 30 Geylang Street 29, #06-40")),
            new Student(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Level("J1"), getSubjectSet("Chemistry", "Biology"), new Rate("60"),
                new Venue("Blk 30 Lorong 3 Serangoon Gardens, #07-18")),
            new Student(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Level("S4"), getSubjectSet("Math"), new Rate("45"),
                new Venue("Blk 11 Ang Mo Kio Street 74, #11-04")),
            new Student(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Level("P6"), getSubjectSet("Math", "Science"), new Rate("35"),
                new Venue("Blk 436 Serangoon Gardens Street 26, #16-43")),
            new Student(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Level("J2"), getSubjectSet("Physics", "Math"), new Rate("70"),
                new Venue("Blk 47 Tampines Street 20, #17-35")),
            new Student(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Level("S1"), getSubjectSet("English"), new Rate("40"),
                new Venue("Blk 45 Aljunied Street 85, #11-31"))
        };
    }

    public static Lesson[] getSampleLessons() {
        return new Lesson[] {
            new Lesson(new Name("Alex Yeoh"), new Phone("87438807"), new Subject("Math"),
                new LessonDate("2027-05-15"), new LessonTime("14:30"), new Duration(120),
                new Venue("Blk 30 Geylang Street 29, #06-40"), LessonStatus.SCHEDULED, null, null),
            new Lesson(new Name("Bernice Yu"), new Phone("99272758"), new Subject("Chemistry"),
                new LessonDate("2027-05-16"), new LessonTime("16:00"), new Duration(90),
                new Venue("Blk 30 Lorong 3 Serangoon Gardens, #07-18"), LessonStatus.SCHEDULED, null, null)
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        for (Lesson sampleLesson : getSampleLessons()) {
            sampleAb.addLesson(sampleLesson);
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
