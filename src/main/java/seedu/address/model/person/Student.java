package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a Student in TutorFlow.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Student extends Person {

    private final Level level;
    private final Set<Subject> subjects = new HashSet<>();
    private final Rate rate;
    private final Venue venue;

    /**
     * Constructs a Student.
     * Name, Phone, Level, Subjects, and Rate must be present and not null.
     */
    public Student(Name name, Phone phone, Email email, Level level, Set<Subject> subjects, Rate rate, Venue venue) {
        super(name, phone, email);
        requireAllNonNull(level, subjects, rate);
        this.level = level;
        this.subjects.addAll(subjects);
        this.rate = rate;
        this.venue = venue;
    }

    public Level getLevel() {
        return level;
    }

    public Set<Subject> getSubjects() {
        return Collections.unmodifiableSet(subjects);
    }

    public Rate getRate() {
        return rate;
    }

    public Venue getVenue() {
        return venue;
    }

    /**
     * Returns true if both students have the same name and phone.
     */
    public boolean isSameStudent(Student otherStudent) {
        if (otherStudent == this) {
            return true;
        }

        return otherStudent != null
                && super.isSamePerson(otherStudent);
    }

    @Override
    public boolean isSamePerson(Person otherPerson) {
        return super.isSamePerson(otherPerson);
    }

    /**
     * Returns true if the other person has the same name but a different phone.
     */
    public boolean hasSameName(Person otherPerson) {
        if (otherPerson == null) {
            return false;
        }
        return otherPerson.getName().fullName.equalsIgnoreCase(getName().fullName)
                && !otherPerson.getPhone().equals(getPhone());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Student otherStudent)) {
            return false;
        }

        return super.equals(otherStudent)
                && level.equals(otherStudent.level)
                && subjects.equals(otherStudent.subjects)
                && rate.equals(otherStudent.rate)
                && Objects.equals(venue, otherStudent.venue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), level, subjects, rate, venue);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("name", getName())
                .add("phone", getPhone());

        if (getEmail() != null) {
            builder.add("email", getEmail());
        }

        builder.add("level", level)
                .add("subjects", subjects)
                .add("rate", rate);

        if (venue != null) {
            builder.add("venue", venue);
        }

        return builder.toString();
    }
}
