package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.util.Comparator;

/**
 * Represents a subject taught to a student in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidSubject(String)}
 *
 * <p>Subjects are free text (1-30 characters) using letters of any language, digits, spaces,
 * {@code &} and {@code -}. New values from commands have their first letter capitalized by
 * {@link seedu.address.logic.parser.ParserUtil#parseSubject(String)}, while values loaded from storage keep their
 * original case. Comparison is case-insensitive, so {@code math} and {@code Math} are treated as the same subject.
 * An accented letter is stored in its composed form, so it does not matter how an accent was typed.
 */
public class Subject {

    public static final String MESSAGE_CONSTRAINTS =
            "Subjects must be 1-30 characters using only letters (in any language), digits, spaces, & and -.";

    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N} &-]{0,29}";

    /**
     * The order in which a student's subjects are shown: alphabetical, ignoring case. It does not depend on the
     * computer's language settings, so the same student's subjects appear in the same order everywhere.
     */
    public static final Comparator<Subject> DISPLAY_ORDER =
            Comparator.comparing(subject -> subject.value, String.CASE_INSENSITIVE_ORDER);

    public final String value;

    /**
     * Constructs a {@code Subject}.
     *
     * @param subject A valid subject name.
     */
    public Subject(String subject) {
        requireNonNull(subject);
        String trimmed = Normalizer.normalize(subject.strip().replaceAll("\\s+", " "), Normalizer.Form.NFC);
        checkArgument(isValidSubject(subject), MESSAGE_CONSTRAINTS);
        value = trimmed;
    }

    /**
     * Returns true if a given string is a valid subject name.
     */
    public static boolean isValidSubject(String test) {
        return test.codePoints().noneMatch(character -> Character.isISOControl(character)
                || character == '\u2028' || character == '\u2029')
                && Normalizer.normalize(test.strip().replaceAll(" {2,}", " "), Normalizer.Form.NFC)
                        .matches(VALIDATION_REGEX);
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
        if (!(other instanceof Subject otherSubject)) {
            return false;
        }

        // Case-insensitive comparison per spec
        return value.equalsIgnoreCase(otherSubject.value);
    }

    @Override
    public int hashCode() {
        // Match equalsIgnoreCase's case folding, including dotless i and the Greek final sigma.
        return value.codePoints().map(point -> Character.toLowerCase(Character.toUpperCase(point)))
                .reduce(0, (hash, point) -> 31 * hash + point);
    }
}
