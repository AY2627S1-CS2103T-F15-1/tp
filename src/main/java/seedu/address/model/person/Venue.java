package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a lesson venue in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidVenue(String)}
 *
 * <p>A venue is a specific kind of address used for tutoring locations.
 * It accepts 1-100 printable characters with no line breaks. Internal
 * whitespace is collapsed to single spaces.
 */
public class Venue extends Address {

    public static final String MESSAGE_CONSTRAINTS =
            "Venue must be 1-100 characters and cannot contain line breaks.";

    private static final int MAX_LENGTH = 100;

    /**
     * Constructs a {@code Venue}.
     *
     * @param venue A valid venue string.
     */
    public Venue(String venue) {
        super(normalizeVenue(venue));
        requireNonNull(venue);
        checkArgument(isValidVenue(venue), MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns true if a given string is a valid venue.
     */
    public static boolean isValidVenue(String test) {
        if (test == null) {
            return false;
        }
        String normalized = normalizeVenue(test);
        return !normalized.isEmpty()
                && normalized.length() <= MAX_LENGTH
                && !normalized.contains("\n")
                && !normalized.contains("\r")
                && Address.isValidAddress(normalized);
    }

    private static String normalizeVenue(String venue) {
        return venue.strip().replaceAll("\\s+", " ");
    }
}
