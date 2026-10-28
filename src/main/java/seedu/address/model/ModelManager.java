package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final FilteredList<Lesson> filteredLessons;
    private final SortedList<Lesson> sortedLessons;
    private List<Task> shownTasks = new ArrayList<>();
    private List<Homework> displayedHomework = List.of();

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing model");

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList(), PREDICATE_SHOW_ACTIVE_PERSONS);
        filteredLessons = new FilteredList<>(this.addressBook.getLessonList());
        sortedLessons = new SortedList<>(filteredLessons, Lesson.CHRONOLOGICAL);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public void setTravelBuffer(int minutes) {
        addressBook.setTravelBuffer(minutes);
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ACTIVE_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public void sortPersons(StudentOrder order) {
        requireNonNull(order);

        addressBook.sortPersons(order);
    }

    @Override
    public void addHomework(Homework homework) {
        addressBook.addHomework(homework);
    }

    @Override
    public void setHomework(Homework target, Homework replacement) {
        addressBook.setHomework(target, replacement);
    }

    @Override
    public void deleteHomework(Homework homework) {
        addressBook.removeHomework(homework);
    }

    @Override
    public List<Homework> getDisplayedHomework() {
        return displayedHomework;
    }

    @Override
    public void showHomework(List<Homework> homework) {
        displayedHomework = List.copyOf(homework);
    }

    //=========== Lessons ====================================================================================

    @Override
    public void addLedgerEntry(LedgerEntry entry) {
        requireNonNull(entry);
        addressBook.addLedgerEntry(entry);
    }

    @Override
    public Optional<LedgerEntry> chargeLesson(Lesson target, Lesson completedLesson) {
        requireAllNonNull(target, completedLesson);
        return addressBook.chargeLesson(target, completedLesson);
    }

    @Override
    public boolean hasLesson(Lesson lesson) {
        requireNonNull(lesson);
        return addressBook.hasLesson(lesson);
    }

    @Override
    public void addLesson(Lesson lesson) {
        addressBook.addLesson(lesson);
        logger.fine("Added lesson");
    }

    @Override
    public void deleteLesson(Lesson target) {
        addressBook.removeLesson(target);
    }

    @Override
    public void setLesson(Lesson target, Lesson editedLesson) {
        requireAllNonNull(target, editedLesson);

        addressBook.setLesson(target, editedLesson);
    }

    @Override
    public List<Lesson> getConflictingLessons(Lesson lesson) {
        requireNonNull(lesson);
        return addressBook.getConflictingLessons(lesson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    //=========== Filtered Lesson List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Lesson} backed by the internal list of
     * {@code addressBook}, in the order of the agenda. Lesson indices refer to this list.
     */
    @Override
    public ObservableList<Lesson> getFilteredLessonList() {
        return sortedLessons;
    }

    @Override
    public void updateFilteredLessonList(Predicate<Lesson> predicate) {
        requireNonNull(predicate);
        filteredLessons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && filteredLessons.equals(otherModelManager.filteredLessons);
    }

    //=========== Tasks ======================================================================================

    @Override
    public void addTask(Task task) {
        requireNonNull(task);
        addressBook.addTask(task);
        logger.fine("Added task");
    }

    @Override
    public Optional<Task> findTask(UUID id) {
        requireNonNull(id);
        return addressBook.findTask(id);
    }

    @Override
    public void setTask(Task target, Task editedTask) {
        requireAllNonNull(target, editedTask);
        addressBook.setTask(target, editedTask);
    }

    @Override
    public void deleteTask(Task target) {
        requireNonNull(target);
        addressBook.removeTask(target);
    }

    @Override
    public List<Task> getShownTasks() {
        return List.copyOf(shownTasks);
    }

    @Override
    public void setShownTasks(List<Task> tasks) {
        requireNonNull(tasks);
        shownTasks = new ArrayList<>(tasks);
    }
}
