package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TravelGap;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.task.Task;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_LESSON = "Lessons list contains duplicate lesson(s).";
    public static final String MESSAGE_DUPLICATE_TASK = "Tasks list contains tasks with the same id.";
    public static final String MESSAGE_STALE_TASK_SKIPPED =
            "Skipped a task whose student, lesson or homework is not in the data.";
    public static final String MESSAGE_INVALID_STUDENT_ORDER =
            "The student order must be ASCENDING or DESCENDING.";
    public static final String MESSAGE_LESSON_WITHOUT_STUDENT =
            "Lessons list contains a lesson whose student is not in the persons list.";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private Integer schemaVersion;
    private Integer travelBuffer;
    private String studentOrder; // null in files saved before sort kept its order, or never sorted

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();
    private final List<JsonAdaptedHomework> homework = new ArrayList<>();
    private final List<JsonAdaptedLedgerEntry> ledgerEntries = new ArrayList<>();
    private final List<JsonAdaptedTask> tasks = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and lessons.
     * The {@code lessons}, {@code homework}, {@code ledgerEntries} and {@code tasks} may be null, so that a file
     * saved before they existed can still be loaded.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons,
            @JsonProperty("homework") List<JsonAdaptedHomework> homework,
            @JsonProperty("ledgerEntries") List<JsonAdaptedLedgerEntry> ledgerEntries,
            @JsonProperty("tasks") List<JsonAdaptedTask> tasks) {
        this.persons.addAll(persons);
        if (ledgerEntries != null) {
            this.ledgerEntries.addAll(ledgerEntries);
        }
        // Absent homework is the explicit default for legacy and earlier schema files.
        if (homework != null) {
            this.homework.addAll(homework);
        }
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
        if (tasks != null) {
            this.tasks.addAll(tasks);
        }
    }

    /** Loads legacy constructor calls with explicitly empty homework, finance and task domains. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedLesson> lessons) {
        this(persons, lessons, null, null, null);
    }

    /** Loads callers with homework but no finance records. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedLesson> lessons,
            List<JsonAdaptedHomework> homework) {
        this(persons, lessons, homework, null, null);
    }

    /** Loads callers with homework and finance records but no tasks. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons, List<JsonAdaptedLesson> lessons,
            List<JsonAdaptedHomework> homework, List<JsonAdaptedLedgerEntry> ledgerEntries) {
        this(persons, lessons, homework, ledgerEntries, null);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        schemaVersion = StorageSchema.CURRENT_VERSION;
        travelBuffer = source.getTravelBuffer();
        studentOrder = source.getStudentOrder().map(StudentOrder::name).orElse(null);
        source.getLedgerEntries().stream().map(JsonAdaptedLedgerEntry::new).forEach(ledgerEntries::add);
        source.getHomeworkList().stream().map(JsonAdaptedHomework::new).forEach(homework::add);
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        lessons.addAll(source.getLessonList().stream().map(lesson -> {
            Lesson linked = lesson.getStudentId().isPresent() ? lesson : source.getPersonList().stream()
                    .filter(lesson::isWith).findFirst().map(lesson::withStudent).orElse(lesson);
            return new JsonAdaptedLesson(linked);
        }).collect(Collectors.toList()));
        tasks.addAll(source.getTaskList().stream().map(JsonAdaptedTask::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        StorageSchema.requireSupported(schemaVersion);
        Set<UUID> studentIds = new HashSet<>();
        Set<UUID> lessonIds = new HashSet<>();
        AddressBook addressBook = new AddressBook();
        if (travelBuffer != null) {
            if (!TravelGap.isValidBuffer(travelBuffer)) {
                throw new IllegalValueException(TravelGap.MESSAGE_CONSTRAINTS);
            }
            addressBook.setTravelBuffer(travelBuffer);
        }
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException("Persons list contains a null student.");
            }
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (!studentIds.add(person.getId())) {
                throw new IllegalValueException("Duplicate student ID.");
            }
            addressBook.addPerson(person);
        }
        if (studentOrder != null) {
            addressBook.sortPersons(toStudentOrder(studentOrder));
        }
        for (JsonAdaptedLesson jsonAdaptedLesson : lessons) {
            if (jsonAdaptedLesson == null) {
                throw new IllegalValueException("Lessons list contains a null lesson.");
            }
            Lesson lesson = jsonAdaptedLesson.toModelType();
            if (addressBook.hasLesson(lesson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_LESSON);
            }
            if (!lessonIds.add(lesson.getId())) {
                throw new IllegalValueException("Duplicate lesson ID.");
            }
            if (lesson.getStudentId().isEmpty()) {
                Lesson unlinked = lesson;
                lesson = addressBook.getPersonList().stream().filter(unlinked::isWith).findFirst()
                        .map(unlinked::withStudent).orElse(unlinked);
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
        for (JsonAdaptedHomework item : homework) {
            if (item == null) {
                throw new IllegalValueException("Homework list contains a null item.");
            }
            try {
                addressBook.addHomework(item.toModelType());
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage());
            }
        }
        List<LedgerEntry> restoredEntries = new ArrayList<>();
        for (JsonAdaptedLedgerEntry entry : ledgerEntries) {
            if (entry == null) {
                throw new IllegalValueException("Ledger contains a null entry.");
            }
            restoredEntries.add(entry.toModelType());
        }
        try {
            addressBook.setLedgerEntries(restoredEntries);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
        addTasksTo(addressBook);
        return addressBook;
    }

    /**
     * Adds the tasks to {@code addressBook}, whose students, lessons and homework must already be added.
     *
     * A task whose student, lesson or homework item is not in {@code addressBook}, or does not belong to the
     * student of the task, is stale: it is skipped (and logged without its text) rather than rejecting the whole
     * data file, so that one dangling link never sets aside every other record.
     *
     * @throws IllegalValueException if a task is invalid or shares an id.
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
            Optional<Person> student = addressBook.getPersonList().stream().filter(task::isAbout).findFirst();
            // A lesson or homework item that a task is about must exist, and be the student's own
            boolean hasLesson = task.getLessonId().isEmpty() || student.isPresent()
                    && addressBook.getLessonList().stream()
                            .anyMatch(lesson -> task.isAbout(lesson) && lesson.isWith(student.get()));
            boolean hasHomework = task.getHomeworkId().isEmpty() || addressBook.getHomeworkList().stream()
                    .anyMatch(item -> task.isAbout(item) && item.getStudentId().equals(task.getStudentId()));
            if (student.isEmpty() || !hasLesson || !hasHomework) {
                logger.warning(MESSAGE_STALE_TASK_SKIPPED);
                continue;
            }
            addressBook.addTask(task);
        }
    }

    private static StudentOrder toStudentOrder(String value) throws IllegalValueException {
        try {
            return StudentOrder.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(MESSAGE_INVALID_STUDENT_ORDER);
        }
    }
}
