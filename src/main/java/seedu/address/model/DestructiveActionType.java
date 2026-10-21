package seedu.address.model;

public enum DestructiveActionType {
    NONE(""),
    CLEAR("clear"),
    DELETE_STUDENT("student deletion"),
    DELETE_HW("homework deletion"),
    DELETE_TASK("task deletion"),
    DELETE_LESSON("lesson deletion");

    public final String description;

    DestructiveActionType(String description) {
        this.description = description;
    }
}
