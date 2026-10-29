package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label archived;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label level;
    @FXML
    private Label rate;
    @FXML
    private Label email;
    @FXML
    private Label venue;
    @FXML
    private Label remark;
    @FXML
    private FlowPane subjects;
    @FXML
    private Button cardEditButton;
    @FXML
    private Button cardArchiveButton;
    @FXML
    private Button cardDeleteButton;
    @FXML
    private HBox editFooter;
    @FXML
    private Button nameEditButton;
    @FXML
    private Button levelEditButton;
    @FXML
    private Button subjectEditButton;
    @FXML
    private Button phoneEditButton;
    @FXML
    private Button rateEditButton;
    @FXML
    private Button emailEditButton;
    @FXML
    private Button venueEditButton;
    @FXML
    private Button remarkEditButton;
    @FXML
    private Button cancelEditButton;

    private final int displayedIndex;
    private final CommandAssist commandAssist;
    private boolean isTouchRevealed;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, null);
    }

    /**
     * Creates a card whose shortcut controls populate commands through {@code commandAssist}. A null helper keeps
     * the card usable for presentation-only tests.
     */
    public PersonCard(Person person, int displayedIndex, CommandAssist commandAssist) {
        super(FXML);
        this.person = person;
        this.displayedIndex = displayedIndex;
        this.commandAssist = commandAssist;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        showIfPresent(archived, person.isArchived() ? "(archived)" : null);
        phone.setText(person.getPhone().value);
        level.setText(person.getLevel().value);
        rate.setText(person.getRate().toDisplayString() + "/hour");
        showIfPresent(email, person.getEmail().map(value -> value.value).orElse(null));
        showIfPresent(venue, person.getVenue().map(value -> value.value).orElse(null));
        remark.setText(person.getRemark().value);
        person.getSubjects().stream()
                .sorted(Subject.DISPLAY_ORDER)
                .forEach(subject -> subjects.getChildren().add(new Label(subject.value)));
        configureActions();
    }

    private void configureActions() {
        cardEditButton.setGraphic(UiIcon.pencil());
        cardArchiveButton.setGraphic(UiIcon.archive());
        cardDeleteButton.setGraphic(UiIcon.trash());
        nameEditButton.setGraphic(UiIcon.pencil());
        levelEditButton.setGraphic(UiIcon.pencil());
        subjectEditButton.setGraphic(UiIcon.pencil());
        phoneEditButton.setGraphic(UiIcon.pencil());
        rateEditButton.setGraphic(UiIcon.pencil());
        emailEditButton.setGraphic(UiIcon.pencil());
        venueEditButton.setGraphic(UiIcon.pencil());
        remarkEditButton.setGraphic(UiIcon.pencil());

        for (Button button : new Button[]{cardEditButton, cardArchiveButton, cardDeleteButton,
            nameEditButton, levelEditButton, subjectEditButton, phoneEditButton, rateEditButton,
            emailEditButton, venueEditButton, remarkEditButton, cancelEditButton}) {
            button.setFocusTraversable(true);
        }

        cardEditButton.setAccessibleText("Edit fields");
        cardArchiveButton.setAccessibleText("Archive student");
        cardDeleteButton.setAccessibleText("Delete student");
        nameEditButton.setAccessibleText("Edit name");
        levelEditButton.setAccessibleText("Edit level");
        subjectEditButton.setAccessibleText("Edit subjects");
        phoneEditButton.setAccessibleText("Edit phone number");
        rateEditButton.setAccessibleText("Edit rate");
        emailEditButton.setAccessibleText("Edit email");
        venueEditButton.setAccessibleText("Edit venue");
        remarkEditButton.setAccessibleText("Edit remark");
        cancelEditButton.setAccessibleText("Cancel field editing");

        cardPane.setOnMouseEntered(event -> updateActionVisibility());
        cardPane.setOnMouseExited(event -> updateActionVisibility());
        cardPane.setOnTouchPressed(event -> {
            isTouchRevealed = true;
            updateActionVisibility();
        });
        cardPane.focusedProperty().addListener((observable, wasFocused, isFocused) ->
                updateActionVisibility());
        for (Button button : new Button[]{cardEditButton, cardArchiveButton, cardDeleteButton}) {
            button.focusedProperty().addListener((observable, wasFocused, isFocused) ->
                    updateActionVisibility());
        }
    }

    private void updateActionVisibility() {
        boolean isVisible = isTouchRevealed || cardPane.isHover() || cardPane.isFocused()
                || cardEditButton.isFocused() || cardArchiveButton.isFocused() || cardDeleteButton.isFocused();
        setCardActionVisibility(isVisible);
    }

    private void setCardActionVisibility(boolean isVisible) {
        cardEditButton.setVisible(isVisible);
        cardArchiveButton.setVisible(isVisible);
        cardDeleteButton.setVisible(isVisible);
        cardEditButton.setManaged(isVisible);
        cardArchiveButton.setManaged(isVisible);
        cardDeleteButton.setManaged(isVisible);
    }

    private void showFieldEditors(boolean isVisible) {
        for (Button button : new Button[]{nameEditButton, levelEditButton, subjectEditButton, phoneEditButton,
            rateEditButton, emailEditButton, venueEditButton, remarkEditButton}) {
            button.setVisible(isVisible);
            button.setManaged(isVisible);
        }
        editFooter.setVisible(isVisible);
        editFooter.setManaged(isVisible);
        updateActionVisibility();
    }

    @FXML
    private void handleCardEdit() {
        if (commandAssist != null) {
            commandAssist.beginEditInteraction(displayedIndex, () -> showFieldEditors(false));
            showFieldEditors(true);
        } else {
            showFieldEditors(true);
        }
    }

    @FXML
    private void handleNameEdit() {
        edit(CommandAssist.Field.NAME);
    }

    @FXML
    private void handleLevelEdit() {
        edit(CommandAssist.Field.LEVEL);
    }

    @FXML
    private void handleSubjectEdit() {
        edit(CommandAssist.Field.SUBJECT);
    }

    @FXML
    private void handlePhoneEdit() {
        edit(CommandAssist.Field.PHONE);
    }

    @FXML
    private void handleRateEdit() {
        edit(CommandAssist.Field.RATE);
    }

    @FXML
    private void handleEmailEdit() {
        edit(CommandAssist.Field.EMAIL);
    }

    @FXML
    private void handleVenueEdit() {
        edit(CommandAssist.Field.VENUE);
    }

    @FXML
    private void handleRemarkEdit() {
        edit(CommandAssist.Field.REMARK);
    }

    private void edit(CommandAssist.Field field) {
        if (commandAssist != null) {
            commandAssist.editField(displayedIndex, field);
        }
    }

    @FXML
    private void handleArchive() {
        if (commandAssist != null) {
            commandAssist.archive(displayedIndex);
        }
    }

    @FXML
    private void handleDelete() {
        if (commandAssist != null) {
            commandAssist.delete(displayedIndex);
        }
    }

    @FXML
    private void handleCancelEdit() {
        if (commandAssist != null) {
            commandAssist.cancelEditInteraction(displayedIndex);
        } else {
            showFieldEditors(false);
        }
    }

    /**
     * Shows {@code label} with the given {@code text}, or hides it if {@code text} is null.
     */
    private static void showIfPresent(Label label, String text) {
        boolean isPresent = text != null;
        label.setText(isPresent ? text : "");
        label.setManaged(isPresent);
        label.setVisible(isPresent);
    }
}
