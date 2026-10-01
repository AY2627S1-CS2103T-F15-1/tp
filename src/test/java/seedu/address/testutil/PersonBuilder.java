package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;

import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Remark;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_LEVEL = "S3";
    public static final String DEFAULT_SUBJECT = "Math";
    public static final String DEFAULT_RATE = "50";
    public static final String DEFAULT_VENUE = "123, Jurong West Ave 6, #08-111";
    public static final String DEFAULT_REMARK = "";

    private Name name;
    private Phone phone;
    private Email email;
    private Level level;
    private Set<Subject> subjects;
    private Rate rate;
    private Venue venue;
    private Remark remark;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        level = new Level(DEFAULT_LEVEL);
        subjects = new HashSet<>();
        subjects.add(new Subject(DEFAULT_SUBJECT));
        rate = new Rate(DEFAULT_RATE);
        venue = new Venue(DEFAULT_VENUE);
        remark = new Remark(DEFAULT_REMARK);
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail().orElse(null);
        level = personToCopy.getLevel();
        subjects = new HashSet<>(personToCopy.getSubjects());
        rate = personToCopy.getRate();
        venue = personToCopy.getVenue().orElse(null);
        remark = personToCopy.getRemark();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Removes the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withoutEmail() {
        this.email = null;
        return this;
    }

    /**
     * Sets the {@code Level} of the {@code Person} that we are building.
     */
    public PersonBuilder withLevel(String level) {
        this.level = new Level(level);
        return this;
    }

    /**
     * Parses the {@code subjects} into a {@code Set<Subject>} and sets it to the {@code Person}
     * that we are building.
     */
    public PersonBuilder withSubjects(String... subjects) {
        this.subjects = SampleDataUtil.getSubjectSet(subjects);
        return this;
    }

    /**
     * Sets the {@code Rate} of the {@code Person} that we are building.
     */
    public PersonBuilder withRate(String rate) {
        this.rate = new Rate(rate);
        return this;
    }

    /**
     * Sets the {@code Venue} of the {@code Person} that we are building.
     */
    public PersonBuilder withVenue(String venue) {
        this.venue = new Venue(venue);
        return this;
    }

    /**
     * Removes the {@code Venue} of the {@code Person} that we are building.
     */
    public PersonBuilder withoutVenue() {
        this.venue = null;
        return this;
    }

    /**
     * Sets the {@code Remark} of the {@code Person} that we are building.
     */
    public PersonBuilder withRemark(String remark) {
        this.remark = new Remark(remark);
        return this;
    }

    public Person build() {
        return new Person(name, phone, email, level, subjects, rate, venue, remark);
    }

}
