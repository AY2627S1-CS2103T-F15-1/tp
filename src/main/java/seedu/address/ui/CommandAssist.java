package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import seedu.address.logic.parser.CliSyntax;

/**
 * Builds command drafts for GUI shortcuts. It only formats existing command syntax; it never executes a command.
 */
public final class CommandAssist {
    private final CommandDraftTarget commandInput;
    private final Consumer<String> helpOutput;
    private String generatedDraft;
    private boolean generatedReferencesStudent;
    private Integer generatedFieldEditIndex;
    private Runnable closeActiveEdit;

    /** Creates a helper that writes generated drafts into {@code commandInput}. */
    public CommandAssist(CommandDraftTarget commandInput) {
        this(commandInput, message -> System.out.println(message));
    }

    /** Creates a helper with an explicit help output, which tests can capture. */
    CommandAssist(CommandDraftTarget commandInput, Consumer<String> helpOutput) {
        this.commandInput = requireNonNull(commandInput);
        this.helpOutput = requireNonNull(helpOutput);
    }

    /** Replaces the command input with the edit command for one field of the displayed student. */
    public void editField(int displayedIndex, Field field) {
        requireNonNull(field);
        setDraft(field.draftFor(displayedIndex));
        generatedReferencesStudent = true;
        generatedFieldEditIndex = displayedIndex;
        announceHelp(field.commandWord());
    }

    /** Replaces the command input with the archive command for the displayed student. */
    public void archive(int displayedIndex) {
        closeEditMode();
        setDraft("archive " + displayedIndex);
        generatedReferencesStudent = true;
        generatedFieldEditIndex = null;
        announceHelp("archive");
    }

    /** Replaces the command input with the delete command for the displayed student. */
    public void delete(int displayedIndex) {
        closeEditMode();
        setDraft("delete " + displayedIndex);
        generatedReferencesStudent = true;
        generatedFieldEditIndex = null;
        announceHelp("delete");
    }

    /** Replaces the command input with the beginning of the add-student command. */
    public void addStudent() {
        closeEditMode();
        setDraft("add " + CliSyntax.PREFIX_NAME);
        generatedReferencesStudent = false;
        generatedFieldEditIndex = null;
        announceHelp("add");
    }

    /**
     * Starts field-editing mode for one card and announces the edit help without changing the current draft.
     * A card that was already in field-editing mode is closed first.
     */
    public void beginEditInteraction(int displayedIndex, Runnable closeEditMode) {
        requireNonNull(closeEditMode);
        closeEditMode();
        closeActiveEdit = closeEditMode;
        announceHelp("edit");
    }

    /**
     * Cancels field-editing mode for {@code displayedIndex}. The draft is cleared only when it is still the
     * unchanged field-edit command generated for that card. A later archive, delete or add action is left intact.
     */
    public void cancelEditInteraction(int displayedIndex) {
        if (generatedFieldEditIndex != null && generatedFieldEditIndex == displayedIndex) {
            if (generatedDraft != null && generatedDraft.equals(commandInput.getText())) {
                commandInput.clearAndFocus();
            }
            generatedDraft = null;
            generatedReferencesStudent = false;
            generatedFieldEditIndex = null;
        }
        closeEditMode();
    }

    /** Cancels the active card interaction after a draft was submitted or replaced by another interaction. */
    public void finishInteraction() {
        generatedDraft = null;
        generatedReferencesStudent = false;
        generatedFieldEditIndex = null;
        closeEditMode();
    }

    private void closeEditMode() {
        Runnable closeEdit = closeActiveEdit;
        closeActiveEdit = null;
        if (closeEdit != null) {
            closeEdit.run();
        }
    }

    /**
     * Closes field-editing mode after the displayed list changes. An unchanged generated draft is cleared because
     * its numbered student could now refer to a different person; text the user has edited is left alone.
     */
    public void displayedListChanged() {
        if (generatedReferencesStudent && generatedDraft != null && generatedDraft.equals(commandInput.getText())) {
            commandInput.clearAndFocus();
        }
        finishInteraction();
    }

    private void setDraft(String draft) {
        generatedDraft = draft;
        commandInput.setTextAndFocus(draft);
    }

    private void announceHelp(String command) {
        helpOutput.accept("help " + command);
    }

    /** The student fields that can be prefilled by the existing edit command. */
    public enum Field {
        NAME("edit", CliSyntax.PREFIX_NAME),
        LEVEL("edit", CliSyntax.PREFIX_LEVEL),
        SUBJECT("edit", CliSyntax.PREFIX_SUBJECT),
        PHONE("edit", CliSyntax.PREFIX_PHONE),
        RATE("edit", CliSyntax.PREFIX_RATE),
        EMAIL("edit", CliSyntax.PREFIX_EMAIL),
        VENUE("edit", CliSyntax.PREFIX_VENUE),
        REMARK("remark", CliSyntax.PREFIX_REMARK);

        private final String commandWord;
        private final String prefix;

        Field(String commandWord, seedu.address.logic.parser.Prefix prefix) {
            this.commandWord = commandWord;
            this.prefix = prefix.getPrefix();
        }

        private String draftFor(int displayedIndex) {
            return commandWord + " " + displayedIndex + " " + prefix;
        }

        private String commandWord() {
            return commandWord;
        }
    }
}
