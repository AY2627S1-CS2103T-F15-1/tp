package seedu.address.model;

/**
 * The kind of destructive action performed on the model.
 */
public enum DestructiveActionType {
    NONE(""),
    CLEAR("clear"),
    DELETE_STUDENT("student deletion"),
    DELETE_HW("homework deletion"),
    DELETE_LESSON("lesson deletion");

    public final String description;

    DestructiveActionType(String description) {
        this.description = description;
    }
}
