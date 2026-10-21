package seedu.address.model.homework;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import seedu.address.model.lesson.Lesson;

/** Immutable assignment linked to a persistent student identity. */
public final class Homework {
    public static final String DESCRIPTION_CONSTRAINTS =
            "Description must contain 1-200 printable one-line characters.";

    private final UUID id;
    private final UUID studentId;
    private final String description;
    private final LocalDate dueDate;
    private final HomeworkStatus status;
    private final String feedback;

    /** Restores a historical assignment without applying today's assignment-date bounds. */
    public Homework(UUID id, UUID studentId, String description, LocalDate dueDate,
            HomeworkStatus status, String feedback) {
        this.id = requireNonNull(id);
        this.studentId = requireNonNull(studentId);
        this.description = normalizeDescription(description);
        this.dueDate = requireNonNull(dueDate);
        this.status = requireNonNull(status);
        checkArgument(feedback == null || Lesson.isValidNotes(feedback), Lesson.MESSAGE_NOTES_CONSTRAINTS);
        this.feedback = feedback == null ? null : feedback.strip().replaceAll(" {2,}", " ");
    }

    /** Creates a new assignment; callers validate the assignment date against their clock. */
    public static Homework assign(UUID studentId, String description, LocalDate dueDate) {
        return new Homework(UUID.randomUUID(), studentId, description, dueDate, HomeworkStatus.ASSIGNED, null);
    }

    /** Validates printable input before trimming and normalizes repeated spaces. */
    public static String normalizeDescription(String description) {
        requireNonNull(description);
        checkArgument(Lesson.isValidNotes(description), DESCRIPTION_CONSTRAINTS);
        String normalized = description.strip().replaceAll(" {2,}", " ");
        checkArgument(normalized.length() <= 200, DESCRIPTION_CONSTRAINTS);
        return normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public HomeworkStatus getStatus() {
        return status;
    }

    public Optional<String> getFeedback() {
        return Optional.ofNullable(feedback);
    }

    /** Returns whether this assignment remains unsubmitted after its due date. */
    public boolean isOverdue(LocalDate today) {
        return status == HomeworkStatus.ASSIGNED && dueDate.isBefore(today);
    }

    /** Returns whether the assignment still needs submission or review. */
    public boolean isOutstanding() {
        return status != HomeworkStatus.REVIEWED;
    }

    /** Returns an edited assignment preserving lifecycle, identity, and feedback. */
    public Homework edit(String description, LocalDate dueDate) {
        return new Homework(id, studentId, description, dueDate, status, feedback);
    }

    /** Returns a lifecycle correction preserving all recorded feedback. */
    public Homework withStatus(HomeworkStatus status) {
        return new Homework(id, studentId, description, dueDate, status, feedback);
    }

    /** Records or replaces feedback only after submission. */
    public Homework withFeedback(String feedback) {
        checkArgument(status != HomeworkStatus.ASSIGNED,
                "Cannot record feedback on homework that has not been submitted.");
        return new Homework(id, studentId, description, dueDate, status, requireNonNull(feedback));
    }

    /** Returns whether another item duplicates this student's assignment. */
    public boolean isSameAssignment(Homework other) {
        return studentId.equals(other.studentId) && description.equals(other.description)
                && dueDate.equals(other.dueDate);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Homework homework && id.equals(homework.id)
                && studentId.equals(homework.studentId) && description.equals(homework.description)
                && dueDate.equals(homework.dueDate) && status == homework.status
                && Objects.equals(feedback, homework.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, description, dueDate, status, feedback);
    }
}
