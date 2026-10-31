package seedu.address.model.finance;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * An immutable charge, payment, withdrawal or reversal. Amounts are captured when recorded, independently of later
 * rate changes. A mistake is never deleted: a reversal is a further entry that cancels the entry named by
 * {@code reverses}, so the history shows that a correction happened.
 */
public record LedgerEntry(UUID id, UUID studentId, UUID lessonId, Kind kind,
        BigDecimal amount, LocalDate date, String notes, UUID reverses) {
    /**
     * Longest amount text accepted from a command. Amounts have no business maximum, so this only stops digit
     * strings so long that they would be slow to process and could make the saved file unreadable.
     */
    public static final int MAX_AMOUNT_LENGTH = 30;
    public static final String MESSAGE_AMOUNT_TOO_LONG = "Amounts can have at most " + MAX_AMOUNT_LENGTH
            + " characters. Check that the amount has no extra digits.";
    public static final String MESSAGE_NOTES_CONSTRAINTS =
            "Payment notes must contain 1-200 printable characters on one line.";

    /** The financial effect of a record. */
    public enum Kind {
        CHARGE, PAYMENT, CHARGE_REVERSAL, PAYMENT_REVERSAL, WITHDRAWAL;

        /** Returns whether this kind cancels an earlier entry. */
        public boolean isReversal() {
            return this == CHARGE_REVERSAL || this == PAYMENT_REVERSAL;
        }

        /** Returns whether entries of this kind refer to a lesson. */
        public boolean refersToLesson() {
            return this == CHARGE || this == CHARGE_REVERSAL;
        }

        /** Returns whether entries of this kind increase what the student owes or decrease available credit. */
        public boolean increasesBalance() {
            return this == CHARGE || this == PAYMENT_REVERSAL || this == WITHDRAWAL;
        }
    }

    /** Restores records and callers that predate optional payment notes. */
    public LedgerEntry(UUID id, UUID studentId, UUID lessonId, Kind kind, BigDecimal amount, LocalDate date) {
        this(id, studentId, lessonId, kind, amount, date, null, null);
    }

    /** Restores records and callers that predate reversals. */
    public LedgerEntry(UUID id, UUID studentId, UUID lessonId, Kind kind, BigDecimal amount, LocalDate date,
            String notes) {
        this(id, studentId, lessonId, kind, amount, date, notes, null);
    }

    /** Validates required fields and stores exact amounts at two decimal places. */
    public LedgerEntry {
        requireAllNonNull(id, studentId, kind, amount, date);
        if (notes != null && !isValidNotes(notes)) {
            throw new IllegalArgumentException(MESSAGE_NOTES_CONSTRAINTS);
        }
        if (kind.refersToLesson() != (lessonId != null)) {
            throw new IllegalArgumentException("Only charges must refer to a lesson.");
        }
        if (kind.isReversal() != (reverses != null)) {
            throw new IllegalArgumentException("Only reversals must refer to the entry they reverse.");
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

    /** Creates a charge of an explicit amount, such as a discounted fee or a charge for a missed lesson. */
    public static LedgerEntry charge(Person student, Lesson lesson, BigDecimal amount) {
        if (!lesson.isWith(student)) {
            throw new IllegalArgumentException("Charge student does not match the lesson.");
        }
        return new LedgerEntry(UUID.randomUUID(), student.getId(), lesson.getId(), Kind.CHARGE, amount,
                lesson.getDate().value);
    }

    /** Creates the entry that cancels {@code original}, dated {@code date}. */
    public static LedgerEntry reversalOf(LedgerEntry original, LocalDate date) {
        if (original.kind().isReversal()) {
            throw new IllegalArgumentException("A reversal cannot itself be reversed.");
        }
        Kind kind = original.kind() == Kind.CHARGE ? Kind.CHARGE_REVERSAL : Kind.PAYMENT_REVERSAL;
        return new LedgerEntry(UUID.randomUUID(), original.studentId(), original.lessonId(), kind,
                original.amount(), date, null, original.id());
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

    /** Creates a withdrawal from a student's available credit. */
    public static LedgerEntry withdrawal(Person student, BigDecimal amount, LocalDate date) {
        return withdrawal(student, amount, date, null);
    }

    /** Creates a withdrawal from a student's available credit with optional notes. */
    public static LedgerEntry withdrawal(Person student, BigDecimal amount, LocalDate date, String notes) {
        return new LedgerEntry(UUID.randomUUID(), student.getId(), null, Kind.WITHDRAWAL, amount, date, notes);
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
