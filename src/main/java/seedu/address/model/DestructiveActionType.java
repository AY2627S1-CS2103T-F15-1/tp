package seedu.address.model;

/**
 * The kind of destructive action performed on the model.
 */
public enum DestructiveActionType {
    NONE(""),
    CLEAR("clear data"),
    DELETE_STUDENT("delete student"),
    DELETE_HW("delete homework"),
    DELETE_TASK("delete task"),
    DELETE_LESSON("delete lesson");

    public final String description;

    DestructiveActionType(String description) {
        this.description = description;
    }
}
