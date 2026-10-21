package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 *
 * <p>Leading and trailing whitespace is removed and runs of spaces inside the name are collapsed into one,
 * so {@code John  Tan} is stored, and compared, as {@code John Tan}. An accented letter is stored in its composed form,
 * so {@code Jos\u00e9} typed with one character and with a letter and a separate accent is the same name.
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must be 1-100 characters long and should only contain letters (in any language), "
                    + "numbers, spaces and the characters . , ' ( ) / - @, and contain at least one letter.";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    private static final String LEGACY_VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N} .,'()/-]{0,99}";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        this(name, false);
    }

    /** Constructs a checked new or grandfathered stored name. */
    private Name(String name, boolean isStored) {
        requireNonNull(name);
        checkArgument(isStored ? isValidStoredName(name) : isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = Normalizer.normalize(name.strip().replaceAll("\\s+", " "), Normalizer.Form.NFC);
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        if (test.codePoints().anyMatch(character -> Character.isISOControl(character)
                || character == '\u2028' || character == '\u2029')) {
            return false;
        }
        String normalized = Normalizer.normalize(test.strip().replaceAll(" {2,}", " "), Normalizer.Form.NFC);
        return normalized.matches("[\\p{L}\\p{N}. ,'()/@-][\\p{L}\\p{M}\\p{N} . ,'()/@-]{0,99}")
                && normalized.codePoints().anyMatch(Character::isLetter);
    }


    /** Returns whether a stored name meets the current or previously accepted v1.3 policy. */
    public static boolean isValidStoredName(String value) {
        return isValidName(value) || value.matches(LEGACY_VALIDATION_REGEX);
    }

    /** Restores accepted legacy names without making them valid new command input. */
    public static Name fromStored(String value) {
        return new Name(value, true);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
