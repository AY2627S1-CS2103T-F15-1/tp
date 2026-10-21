package seedu.address.model.homework;

import java.util.Locale;

/** Persisted homework lifecycle; overdue is calculated from the due date. */
public enum HomeworkStatus {
    ASSIGNED, SUBMITTED, REVIEWED;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
