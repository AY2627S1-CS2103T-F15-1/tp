package seedu.address.model.person;

import java.util.Comparator;

/**
 * The order of the student list chosen with {@code sort}. Once chosen, it is kept: students that are added or
 * renamed later take their place in it.
 */
public enum StudentOrder {
    ASCENDING, DESCENDING;

    private static final Comparator<Person> BY_NAME =
            Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);

    /** Returns the comparator that puts students in this order: by name, ignoring case. */
    public Comparator<Person> comparator() {
        return this == ASCENDING ? BY_NAME : BY_NAME.reversed();
    }
}
