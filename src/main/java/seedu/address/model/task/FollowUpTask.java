package seedu.address.model.task;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.lesson.Lesson;

/** Immutable follow-up with exactly one stable target and its owning student. */
public record FollowUpTask(UUID id, UUID studentId, LinkType linkType, UUID targetId,
        String description, LocalDate dueDate, boolean done) {
    /** Supported roots of a follow-up; PAYMENT always references the original payment event. */
    public enum LinkType { STUDENT, LESSON, HOMEWORK, PAYMENT }

    /** Validates persisted fields; the address-book root validates target existence and ownership. */
    public FollowUpTask {
        requireNonNull(id);
        requireNonNull(studentId);
        requireNonNull(linkType);
        requireNonNull(targetId);
        description = normalizeDescription(description);
    }

    /** Validates and normalizes a printable one-line description. */
    public static String normalizeDescription(String text) {
        requireNonNull(text);
        if (!Lesson.isValidCancelReason(text)) {
            throw new IllegalArgumentException("Task description must be 1-200 printable one-line characters.");
        }
        return text.strip().replaceAll(" {2,}", " ");
    }

    /** Edits content while retaining identity, link, and completion state. */
    public FollowUpTask edit(String description, LocalDate dueDate) {
        return new FollowUpTask(id, studentId, linkType, targetId, description, dueDate, done);
    }

    /** Marks this follow-up complete without deleting its history. */
    public FollowUpTask complete() {
        return new FollowUpTask(id, studentId, linkType, targetId, description, dueDate, true);
    }
}
