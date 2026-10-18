package seedu.address.model.finance;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * An immutable financial event. Corrections append a reversal pointing at the original event.
 * Charges retain their original rate, duration, amount, and description even after profile or lesson edits.
 */
public record LedgerEntry(UUID id, UUID studentId, Kind kind, LocalDate date, BigDecimal amount,
        UUID lessonId, UUID reversalOf, BigDecimal hourlyRate, Integer durationMinutes, String description,
        boolean overridden) {
    /** Financial event types; reversals obtain their sign from their original event. */
    public enum Kind { CHARGE, PAYMENT, REVERSAL }

    /** Validates the event's local shape; root-level links and uniqueness are checked on append. */
    public LedgerEntry {
        requireNonNull(id);
        requireNonNull(studentId);
        requireNonNull(kind);
        requireNonNull(date);
        amount = Money.positive(amount);
        if (description != null && (description.isBlank() || description.length() > 200
                || description.codePoints().anyMatch(character -> Character.isISOControl(character)
                || character == '\u2028' || character == '\u2029'))) {
            throw new IllegalArgumentException("Payment notes and descriptions must be 1-200 printable characters.");
        }
        if (kind == Kind.CHARGE) {
            if (lessonId == null || reversalOf != null || hourlyRate == null || hourlyRate.signum() < 0
                    || hourlyRate.scale() > 2 || hourlyRate.compareTo(new BigDecimal("9999.99")) > 0
                    || durationMinutes == null || durationMinutes <= 0 || description == null) {
                throw new IllegalArgumentException("Invalid charge snapshot.");
            }
            hourlyRate = hourlyRate.setScale(2);
            if (!overridden && !amount.equals(Money.charge(hourlyRate, durationMinutes))) {
                throw new IllegalArgumentException("Charge amount does not match its rate and duration snapshot.");
            }
        } else if (lessonId != null || hourlyRate != null || durationMinutes != null || overridden
                || (kind == Kind.PAYMENT && reversalOf != null)
                || (kind == Kind.REVERSAL && reversalOf == null)) {
            throw new IllegalArgumentException("Invalid payment or reversal fields.");
        }
    }

    /** Creates a charge using the current rate or an explicit positive override. */
    public static LedgerEntry charge(Lesson lesson, Person student, BigDecimal override) {
        return new LedgerEntry(UUID.randomUUID(), student.getId(), Kind.CHARGE, lesson.getDate().value,
                override == null ? Money.charge(student.getRate().value, lesson.getDuration().value) : override,
                lesson.getId(), null, student.getRate().value, lesson.getDuration().value,
                lesson.getSubject() + " lesson", override != null);
    }

    /** Creates a payment; identical transfers have distinct identities and are legitimate. */
    public static LedgerEntry payment(UUID studentId, BigDecimal amount, LocalDate date, String notes) {
        return new LedgerEntry(UUID.randomUUID(), studentId, Kind.PAYMENT, date, amount,
                null, null, null, null, notes, false);
    }

    /** Creates a compensating event without changing the original record. */
    public static LedgerEntry reverse(LedgerEntry original, LocalDate date) {
        if (original.kind == Kind.REVERSAL) {
            throw new IllegalArgumentException("A reversal cannot itself be reversed.");
        }
        return new LedgerEntry(UUID.randomUUID(), original.studentId, Kind.REVERSAL, date, original.amount,
                null, original.id, null, null, null, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public LocalDate getDate() {
        return date;
    }
}
