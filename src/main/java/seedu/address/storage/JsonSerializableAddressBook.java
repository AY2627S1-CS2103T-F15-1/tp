package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.task.Task;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_LESSON = "Lessons list contains duplicate lesson(s).";
    public static final String MESSAGE_DUPLICATE_TASK = "Tasks list contains tasks with the same id.";
    public static final String MESSAGE_TASK_WITHOUT_TARGET =
            "Tasks list contains a task whose student or lesson is not in the data.";
    public static final String MESSAGE_LESSON_WITHOUT_STUDENT =
            "Lessons list contains a lesson whose student is not in the persons list.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();
    private final List<JsonAdaptedTask> tasks = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and lessons, and no tasks.
     */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedLesson> lessons) {
        this(persons, lessons, null);
    }

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and lessons.
     * The {@code lessons} and {@code tasks} may be null, so that a file saved before they existed can still be
     * loaded.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons,
            @JsonProperty("tasks") List<JsonAdaptedTask> tasks) {
        this.persons.addAll(persons);
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
        if (tasks != null) {
            this.tasks.addAll(tasks);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        lessons.addAll(source.getLessonList().stream().map(JsonAdaptedLesson::new).collect(Collectors.toList()));
        tasks.addAll(source.getTaskList().stream().map(JsonAdaptedTask::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException("Persons list contains a null student.");
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(person);
        }
        for (JsonAdaptedLesson jsonAdaptedLesson : lessons) {
            if (jsonAdaptedLesson == null) {
                throw new IllegalValueException("Lessons list contains a null lesson.");
            }
            Lesson lesson = jsonAdaptedLesson.toModelType();
            if (addressBook.hasLesson(lesson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_LESSON);
            }
            addressBook.addLesson(lesson);
        }
        // Commands always delete or update the lessons of a student together with the student, so a lesson without
        // its student can only come from editing the data file by hand
        boolean hasLessonWithoutStudent = addressBook.getLessonList().stream()
                .anyMatch(lesson -> addressBook.getPersonList().stream().noneMatch(lesson::isWith));
        if (hasLessonWithoutStudent) {
            throw new IllegalValueException(MESSAGE_LESSON_WITHOUT_STUDENT);
        }
        addTasksTo(addressBook);
        return addressBook;
    }

    /**
     * Adds the tasks to {@code addressBook}, whose students and lessons must already be added.
     *
     * @throws IllegalValueException if a task is invalid, shares an id, or is about a student or lesson that is
     *     not in {@code addressBook}.
     */
    private void addTasksTo(AddressBook addressBook) throws IllegalValueException {
        for (JsonAdaptedTask jsonAdaptedTask : tasks) {
            if (jsonAdaptedTask == null) {
                throw new IllegalValueException("Tasks list contains a null task.");
            }
            Task task = jsonAdaptedTask.toModelType();
            if (addressBook.findTask(task.getId()).isPresent()) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_TASK);
            }
            boolean hasStudent = addressBook.getPersonList().stream().anyMatch(task::isAbout);
            boolean hasLesson = task.getLessonLink().isEmpty()
                    || addressBook.getLessonList().stream().anyMatch(task::isAbout);
            if (!hasStudent || !hasLesson) {
                throw new IllegalValueException(MESSAGE_TASK_WITHOUT_TARGET);
            }
            addressBook.addTask(task);
        }
    }

}
