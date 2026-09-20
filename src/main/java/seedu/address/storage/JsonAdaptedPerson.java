package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
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
 * Jackson-friendly version of {@link Person} / {@link Student}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String level;
    private final List<JsonAdaptedSubject> subjects = new ArrayList<>();
    private final String rate;
    private final String venue;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("level") String level,
            @JsonProperty("subjects") List<JsonAdaptedSubject> subjects, @JsonProperty("rate") String rate,
            @JsonProperty("venue") String venue) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.level = level;
        if (subjects != null) {
            this.subjects.addAll(subjects);
        }
        this.rate = rate;
        this.venue = venue;
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail() != null ? source.getEmail().value : null;

        if (source instanceof Student student) {
            level = student.getLevel().value;
            subjects.addAll(student.getSubjects().stream()
                    .map(JsonAdaptedSubject::new)
                    .collect(Collectors.toList()));
            rate = student.getRate().toString();
            venue = student.getVenue() != null ? student.getVenue().value : null;
        } else {
            level = null;
            rate = null;
            venue = null;
        }
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Student} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Subject> personSubjects = new ArrayList<>();
        for (JsonAdaptedSubject subject : subjects) {
            personSubjects.add(subject.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        Email modelEmail = null;
        if (email != null) {
            if (!Email.isValidEmail(email)) {
                throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
            }
            modelEmail = new Email(email);
        }

        if (level == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Level.class.getSimpleName()));
        }
        if (!Level.isValidLevel(level)) {
            throw new IllegalValueException(Level.MESSAGE_CONSTRAINTS);
        }
        final Level modelLevel = new Level(level);

        if (rate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Rate.class.getSimpleName()));
        }
        if (!Rate.isValidRate(rate)) {
            throw new IllegalValueException(Rate.MESSAGE_CONSTRAINTS);
        }
        final Rate modelRate = new Rate(rate);

        Venue modelVenue = null;
        if (venue != null) {
            if (!Venue.isValidVenue(venue)) {
                throw new IllegalValueException(Venue.MESSAGE_CONSTRAINTS);
            }
            modelVenue = new Venue(venue);
        }

        final Set<Subject> modelSubjects = new HashSet<>(personSubjects);
        return new Student(modelName, modelPhone, modelEmail, modelLevel, modelSubjects, modelRate, modelVenue);
    }
}
