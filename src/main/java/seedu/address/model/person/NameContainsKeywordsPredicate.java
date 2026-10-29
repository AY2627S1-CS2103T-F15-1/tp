package seedu.address.model.person;

import java.text.Normalizer;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Name} contains any of the keywords given.
 * A keyword matches any part of the name and ignores letter case, so {@code wei} matches {@code Tan Wei Ming}
 * and {@code Weiling Lim}. A blank keyword matches nothing. An accented letter in a keyword matches however the
 * accent was typed.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public NameContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        String foldedName = foldCase(person.getName().fullName);
        return keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .map(NameContainsKeywordsPredicate::foldCase)
                .anyMatch(foldedName::contains);
    }

    /** Uses String.equalsIgnoreCase's per-code-point case equivalence, retaining accent normalization. */
    private static String foldCase(String text) {
        int[] points = Normalizer.normalize(text, Normalizer.Form.NFC).codePoints()
                .map(point -> Character.toLowerCase(Character.toUpperCase(point))).toArray();
        return new String(points, 0, points.length);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherNameContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherNameContainsKeywordsPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
