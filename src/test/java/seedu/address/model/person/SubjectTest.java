package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
    }

    @Test
    public void constructor_invalidSubjectName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Subject(""));
        assertThrows(IllegalArgumentException.class, () -> new Subject("Math!"));
    }

    @Test
    public void constructor_extraWhitespace_whitespaceNormalized() {
        assertEquals("Additional Math", new Subject("  Additional   Math ").value);
    }

    @Test
    public void isValidSubject() {
        // null subject name
        assertThrows(NullPointerException.class, () -> Subject.isValidSubject(null));

        // invalid subject names
        assertFalse(Subject.isValidSubject("")); // empty string
        assertFalse(Subject.isValidSubject(" ")); // spaces only
        assertFalse(Subject.isValidSubject("^")); // only non-alphanumeric characters
        assertFalse(Subject.isValidSubject("Math*")); // contains non-alphanumeric characters
        assertFalse(Subject.isValidSubject("-Math")); // starts with a symbol
        assertFalse(Subject.isValidSubject("a".repeat(31))); // too long

        // valid subject names
        assertTrue(Subject.isValidSubject("Math"));
        assertTrue(Subject.isValidSubject("Math 2")); // alphanumeric characters
        assertTrue(Subject.isValidSubject("English & Literature")); // ampersand
        assertTrue(Subject.isValidSubject("Chinese-Higher")); // hyphen
        assertTrue(Subject.isValidSubject("a".repeat(30))); // longest allowed
        assertTrue(Subject.isValidSubject("\u534e\u6587")); // Chinese characters
        assertTrue(Subject.isValidSubject("Fran\u00e7ais")); // accented letter
        assertTrue(Subject.isValidSubject("\u534e".repeat(30))); // longest allowed, in Chinese
        assertFalse(Subject.isValidSubject("\u534e".repeat(31))); // too long, in Chinese
    }

    @Test
    public void toStringMethod() {
        assertEquals("Math", new Subject("Math").toString());
    }

    @Test
    public void equals() {
        Subject subject = new Subject("Math");

        // same values -> returns true
        assertTrue(subject.equals(new Subject("Math")));

        // same values with different case -> returns true
        assertTrue(subject.equals(new Subject("math")));

        // same object -> returns true
        assertTrue(subject.equals(subject));

        // null -> returns false
        assertFalse(subject.equals(null));

        // different types -> returns false
        assertFalse(subject.equals(5.0f));

        // different values -> returns false
        assertFalse(subject.equals(new Subject("Physics")));
    }

    @Test
    public void hashCode_equalSubjects_sameHashCode() {
        assertEquals(new Subject("Math").hashCode(), new Subject("MATH").hashCode());
    }

    @Test
    public void constructor_accentTypedAsSeparateCharacter_isSameSubjectAsComposedAccent() {
        Subject composed = new Subject("Fran\u00e7ais");
        Subject decomposed = new Subject("Franc\u0327ais");

        assertEquals(composed, decomposed);
        assertEquals(composed.hashCode(), decomposed.hashCode());
        assertEquals("Fran\u00e7ais", decomposed.toString());
    }

    @Test
    public void hashCode_equalSubjectsInTurkishLocale_sameHashCode() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals(new Subject("PHYSICS I").hashCode(), new Subject("physics i").hashCode());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
