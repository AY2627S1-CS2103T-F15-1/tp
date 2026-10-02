package seedu.address.testutil;

import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * A utility class containing a list of {@code Lesson} objects to be used in tests.
 */
public class TypicalLessons {

    // The lessons of ALICE and BENSON overlap, while the lesson of CARL is on another day
    public static final Lesson ALICE_MATH = new LessonBuilder().withStudent(ALICE).withSubject("Math")
            .withDate("2026-10-12").withTime("16:30").withDuration(90).withVenue("123, Jurong West Ave 6, #08-111")
            .build();
    public static final Lesson BENSON_PHYSICS = new LessonBuilder().withStudent(BENSON).withSubject("Physics")
            .withDate("2026-10-12").withTime("17:00").withDuration(60).build();
    public static final Lesson CARL_ENGLISH = new LessonBuilder().withStudent(CARL).withSubject("English")
            .withDate("2026-10-13").withTime("10:00").withDuration(45).build();

    private TypicalLessons() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical persons and all the typical lessons.
     */
    public static AddressBook getTypicalAddressBookWithLessons() {
        AddressBook ab = new AddressBook();
        for (Person person : TypicalPersons.getTypicalPersons()) {
            ab.addPerson(person);
        }
        for (Lesson lesson : getTypicalLessons()) {
            ab.addLesson(lesson);
        }
        return ab;
    }

    public static List<Lesson> getTypicalLessons() {
        return new ArrayList<>(Arrays.asList(ALICE_MATH, BENSON_PHYSICS, CARL_ENGLISH));
    }
}
