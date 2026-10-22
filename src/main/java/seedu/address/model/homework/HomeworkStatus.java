package seedu.address.model.homework;

import java.util.Locale;
import java.util.Optional;

/** Persisted homework lifecycle; overdue is calculated from the due date. */
public enum HomeworkStatus {
    ASSIGNED, SUBMITTED, REVIEWED;

    public static final String MESSAGE_CONSTRAINTS = "Status must be one of: assigned, submitted, reviewed.";

    /**
     * Returns the status that {@code text} names, ignoring letter case and surrounding spaces, or an empty
     * {@code Optional} if the text names none. {@code overdue} is derived and so never names a status.
     */
    public static Optional<HomeworkStatus> fromText(String text) {
        String normalized = text.strip().toUpperCase(Locale.ENGLISH);
        for (HomeworkStatus status : values()) {
            if (status.name().equals(normalized)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
