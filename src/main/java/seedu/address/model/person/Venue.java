package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the place where a student is usually taught in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidVenue(String)}
 *
 * <p>A venue is free text of 1-100 characters. Leading and trailing whitespace is removed
 * and runs of whitespace inside the venue are collapsed into a single space.
 */
public class Venue {

    public static final String MESSAGE_CONSTRAINTS =
            "Venue must be 1-100 printable characters on one line and cannot be blank.";

    public static final String VALIDATION_REGEX = "[^\\s].{0,99}";

    public final String value;

    /**
     * Constructs a {@code Venue}.
     *
     * @param venue A valid venue.
     */
    public Venue(String venue) {
        this(venue, false);
    }

    /** Constructs a checked new or grandfathered stored venue. */
    private Venue(String venue, boolean stored) {
        requireNonNull(venue);
        String normalized = normalize(venue);
        checkArgument(stored ? isValidStoredVenue(venue) : isValidVenue(venue), MESSAGE_CONSTRAINTS);
        value = normalized;
    }

    /**
     * Returns true if a given string is a valid venue.
     */
    public static boolean isValidVenue(String test) {
        requireNonNull(test);
        return normalize(test).matches(VALIDATION_REGEX) && test.codePoints().noneMatch(character ->
                Character.isISOControl(character) || character == '\u2028' || character == '\u2029');
    }

    /** Accepts only previously accepted normalized venues when restoring older data. */
    public static boolean isValidStoredVenue(String value) {
        return normalize(value).matches(VALIDATION_REGEX);
    }

    /** Restores a legacy venue, retaining the v1.3 whitespace normalization. */
    public static Venue fromStored(String value) {
        return new Venue(value, true);
    }

    /** Collapses spaces for storage and comparison. */
    private static String normalize(String venue) {
        return venue.strip().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Venue otherVenue)) {
            return false;
        }

        return value.equals(otherVenue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
