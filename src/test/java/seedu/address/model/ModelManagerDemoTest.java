package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.homework.Homework;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.LessonBuilder;

public class ModelManagerDemoTest {
    private final ModelManager model = new ModelManager();

    @Test
    public void startDemo_replacesTheDataAndEndDemoBringsItBack() {
        model.addPerson(ALICE);
        AddressBook before = new AddressBook(model.getAddressBook());

        model.startDemo(SampleDataUtil.getSampleAddressBook());

        assertTrue(model.isDemoMode());
        assertEquals(6, model.getAddressBook().getPersonList().size());
        assertFalse(model.getAddressBook().getPersonList().contains(ALICE));
        model.endDemo();
        assertFalse(model.isDemoMode());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void endDemo_discardsEverythingDoneInTheDemo() {
        model.addPerson(ALICE);
        model.startDemo(SampleDataUtil.getSampleAddressBook());
        model.addPerson(BENSON);
        model.deletePerson(model.getAddressBook().getPersonList().getFirst());

        model.endDemo();

        assertEquals(List.of(ALICE), List.copyOf(model.getAddressBook().getPersonList()));
    }

    @Test
    public void startAndEndDemo_forgetNumbering() {
        model.addPerson(ALICE);
        model.addLesson(new LessonBuilder().withStudent(ALICE).build());
        Homework homework = new HomeworkBuilder(ALICE).build();
        model.addHomework(homework);
        model.showHomework(List.of(homework));
        model.updateFilteredPersonList(person -> false);
        model.updateFilteredLessonList(unused -> true);

        model.startDemo(SampleDataUtil.getSampleAddressBook());

        assertTrue(model.getDisplayedHomework().isEmpty());
        assertEquals(6, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredLessonList().isEmpty());

        model.showHomework(List.of(homework));
        model.endDemo();

        assertTrue(model.getDisplayedHomework().isEmpty());
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void startDemo_whileOneIsRunning_throwsIllegalStateException() {
        model.startDemo(SampleDataUtil.getSampleAddressBook());

        assertThrows(IllegalStateException.class, () -> model.startDemo(new AddressBook()));
        assertEquals(6, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void endDemo_withoutADemo_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, model::endDemo);
        assertFalse(model.isDemoMode());
    }

    @Test
    public void startDemo_nullData_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> model.startDemo(null));
    }

    @Test
    public void demoCanBeStartedAgainAfterItEnded() {
        model.startDemo(SampleDataUtil.getSampleAddressBook());
        model.endDemo();

        model.startDemo(SampleDataUtil.getSampleAddressBook());

        assertTrue(model.isDemoMode());
    }
}
