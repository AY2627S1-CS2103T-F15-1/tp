package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import seedu.address.model.ReadOnlyAddressBook;

/** Human-readable student labels for summaries that do not show the full contact details. */
public final class StudentLabels {
    private StudentLabels() {}

    /** Adds the phone when another active or archived student has the same case-insensitive name. */
    public static String format(ReadOnlyAddressBook book, Person student) {
        requireNonNull(book);
        requireNonNull(student);
        boolean hasSameName = book.getPersonList().stream()
                .anyMatch(other -> !other.getId().equals(student.getId()) && student.hasSameName(other));
        return student.getName() + (hasSameName ? " (" + student.getPhone() + ")" : "");
    }
}
