package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a student in TutorFlow.
 * Guarantees: details are present and not null, except for the optional email and venue,
 * field values are validated, immutable.
 */
public class Person {

    public static final int MAX_SUBJECTS = 10;
    public static final String MESSAGE_SUBJECT_COUNT = "A student must have between 1 and 10 subjects.";

    private final UUID id;
    private final boolean isArchived;

    // Identity fields
    private final Name name;
    private final Phone phone;

    // Data fields
    private final Email email; // optional, null if the student has no email
    private final Level level;
    private final Set<Subject> subjects = new HashSet<>();
    private final Rate rate;
    private final Venue venue; // optional, null if the student has no usual venue
    private final Remark remark;

    /**
     * Creates a person with an empty remark.
     * Every field must be present and not null, except for {@code email} and {@code venue}.
     */
    public Person(Name name, Phone phone, Email email, Level level, Set<Subject> subjects, Rate rate, Venue venue) {
        this(name, phone, email, level, subjects, rate, venue, new Remark(""));
    }

    /**
     * Every field must be present and not null, except for {@code email} and {@code venue}.
     */
    public Person(Name name, Phone phone, Email email, Level level, Set<Subject> subjects, Rate rate, Venue venue,
            Remark remark) {
        this(UUID.randomUUID(), false, name, phone, email, level, subjects, rate, venue, remark);
    }

    /** Creates an immutable student with a persisted identity and archive state. */
    public Person(UUID id, boolean isArchived, Name name, Phone phone, Email email, Level level,
            Set<Subject> subjects, Rate rate, Venue venue, Remark remark) {
        requireAllNonNull(id, name, phone, level, subjects, rate, remark);
        this.id = id;
        this.isArchived = isArchived;
        checkArgument(isValidSubjectCount(subjects.size()), MESSAGE_SUBJECT_COUNT);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.level = level;
        this.subjects.addAll(subjects);
        this.rate = rate;
        this.venue = venue;
        this.remark = remark;
    }

    /** Returns whether a new or replacement subject set meets the shared domain limit. */
    public static boolean isValidSubjectCount(int size) {
        return size >= 1 && size <= MAX_SUBJECTS;
    }

    public UUID getId() {
        return id;
    }

    public boolean isArchived() {
        return isArchived;
    }

    /** Returns a copy with the requested archive state, retaining the persistent identity. */
    public Person withArchived(boolean shouldBeArchived) {
        return new Person(id, shouldBeArchived, name, phone, email, level, subjects, rate, venue, remark);
    }

    /** Retains this student's details under the supplied persistent identity. */
    public Person withId(UUID value) {
        return new Person(value, isArchived, name, phone, email, level, subjects, rate, venue, remark);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    public Level getLevel() {
        return level;
    }

    /**
     * Returns an immutable subject set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Subject> getSubjects() {
        return Collections.unmodifiableSet(subjects);
    }

    public Rate getRate() {
        return rate;
    }

    public Optional<Venue> getVenue() {
        return Optional.ofNullable(venue);
    }

    public Remark getRemark() {
        return remark;
    }

    /**
     * Returns true if both persons have the same name, ignoring case, and the same phone number.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null && hasIdentity(otherPerson.getName(), otherPerson.getPhone());
    }

    /**
     * Returns true if both persons have the same name, ignoring case, whatever their phone numbers.
     * Two different students can share a name, but the user is warned when this happens.
     */
    public boolean hasSameName(Person otherPerson) {
        return otherPerson != null && otherPerson.getName().fullName.equalsIgnoreCase(name.fullName);
    }

    /**
     * Returns true if this person has the given name, ignoring case, and the given phone number.
     * This is the identity that other data, such as a lesson, uses to refer to a person.
     */
    public boolean hasIdentity(Name otherName, Phone otherPhone) {
        return otherName.fullName.equalsIgnoreCase(name.fullName) && otherPhone.equals(phone);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && Objects.equals(email, otherPerson.email)
                && level.equals(otherPerson.level)
                && subjects.equals(otherPerson.subjects)
                && rate.equals(otherPerson.rate)
                && Objects.equals(venue, otherPerson.venue)
                && remark.equals(otherPerson.remark)
                && isArchived == otherPerson.isArchived;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, level, subjects, rate, venue, remark, isArchived);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("level", level)
                .add("subjects", subjects)
                .add("rate", rate)
                .add("venue", venue)
                .add("remark", remark)
                .toString();
    }

}
