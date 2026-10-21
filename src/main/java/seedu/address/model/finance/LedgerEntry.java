package seedu.address.model.finance;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** An immutable charge or payment. Amounts are captured when recorded, independently of later rate changes. */
public record LedgerEntry(UUID id, UUID studentId, UUID lessonId, Kind kind,
        BigDecimal amount, LocalDate date, String notes) {
    public static final String MESSAGE_NOTES_CONSTRAINTS =
            "Payment notes must contain 1-200 printable characters on one line.";

    /** The financial effect of a record. */
    public enum Kind { CHARGE, PAYMENT }

    /** Restores records and callers that predate optional payment notes. */
    public LedgerEntry(UUID id, UUID studentId, UUID lessonId, Kind kind, BigDecimal amount, LocalDate date) {
        this(id, studentId, lessonId, kind, amount, date, null);
    }

    /** Validates required fields and stores exact amounts at two decimal places. */
    public LedgerEntry {
        requireAllNonNull(id, studentId, kind, amount, date);
        if (notes != null && !isValidNotes(notes)) {
            throw new IllegalArgumentException(MESSAGE_NOTES_CONSTRAINTS);
        }
        if ((kind == Kind.CHARGE) != (lessonId != null)) {
            throw new IllegalArgumentException("Only charges must refer to a lesson.");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Charges and payments must be positive.");
        }
        try {
            amount = amount.setScale(2);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Amounts must have at most two decimal places.", e);
        }
    }

    /** Captures the hourly rate times duration, rounding once to cents with HALF_UP. */
    public static LedgerEntry charge(Person student, Lesson lesson) {
        if (!lesson.isWith(student)) {
            throw new IllegalArgumentException("Charge student does not match the lesson.");
        }
        return new LedgerEntry(lesson.getId(), student.getId(), lesson.getId(), Kind.CHARGE,
                lessonFee(student, lesson), lesson.getDate().value);
    }

    /** Returns the rounded fee, which may be zero for a free or very low-rate lesson. */
    public static BigDecimal lessonFee(Person student, Lesson lesson) {
        requireAllNonNull(student, lesson);
        return student.getRate().value.multiply(BigDecimal.valueOf(lesson.getDuration().value))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    /** Creates a payment against a student account. */
    public static LedgerEntry payment(Person student, BigDecimal amount, LocalDate date) {
        return payment(student, amount, date, null);
    }

    /** Creates a payment with optional one-line notes, such as a transfer reference. */
    public static LedgerEntry payment(Person student, BigDecimal amount, LocalDate date, String notes) {
        return new LedgerEntry(UUID.randomUUID(), student.getId(), null, Kind.PAYMENT, amount, date, notes);
    }

    /** Returns whether optional payment notes meet the specification's separate 200-character limit. */
    public static boolean isValidNotes(String notes) {
        return !notes.isBlank() && notes.length() <= 200 && notes.codePoints()
                .noneMatch(character -> Character.isISOControl(character) || character == '\u2028'
                        || character == '\u2029');
    }

    /** Returns whether this entry belongs to the given student's current identity. */
    public boolean isWith(Person student) {
        return studentId.equals(student.getId());
    }
}
