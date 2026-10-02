package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

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

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        level.setText(person.getLevel().value);
        rate.setText(person.getRate().toDisplayString() + "/lesson");
        showIfPresent(email, person.getEmail().map(value -> value.value).orElse(null));
        showIfPresent(venue, person.getVenue().map(value -> value.value).orElse(null));
        remark.setText(person.getRemark().value);
        person.getSubjects().stream()
                .sorted(Comparator.comparing(subject -> subject.value.toLowerCase()))
                .forEach(subject -> subjects.getChildren().add(new Label(subject.value)));
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
