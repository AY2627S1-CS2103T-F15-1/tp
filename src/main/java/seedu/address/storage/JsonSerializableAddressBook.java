package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TravelGap;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_LESSON = "Lessons list contains duplicate lesson(s).";
    public static final String MESSAGE_LESSON_WITHOUT_STUDENT =
            "Lessons list contains a lesson whose student is not in the persons list.";

    private Integer schemaVersion;
    private Integer travelBuffer;

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and lessons.
     * The {@code lessons} may be null, so that a file saved before lessons existed can still be loaded.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons) {
        this.persons.addAll(persons);
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        schemaVersion = StorageSchema.CURRENT_VERSION;
        travelBuffer = source.getTravelBuffer();
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        lessons.addAll(source.getLessonList().stream().map(lesson -> {
            Lesson linked = lesson.getStudentId().isPresent() ? lesson : source.getPersonList().stream()
                    .filter(lesson::isWith).findFirst().map(lesson::withStudent).orElse(lesson);
            return new JsonAdaptedLesson(linked);
        }).collect(Collectors.toList()));
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
        return addressBook;
    }

}
