package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * An immutable, optional remark about a person. Any non-null string is accepted.
 */
public class Remark {
    public final String value;

    /**
     * Creates a remark; an empty string represents no remark.
     */
    public Remark(String value) {
        this.value = requireNonNull(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Remark otherRemark && value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
