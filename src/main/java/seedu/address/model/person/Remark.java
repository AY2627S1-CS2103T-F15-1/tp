package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Person's remark in the address book.
 * Guarantees: immutable; is never null.
 * Leading and trailing whitespace is removed and runs of whitespace inside the remark are collapsed into one space,
 * as for the other free-text values. An empty remark means that the student has none.
 */
public class Remark {

    public final String value;

    /**
     * Constructs a {@code Remark}.
     *
     * @param remark A remark about the person.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark.strip().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Remark otherRemark
                && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
